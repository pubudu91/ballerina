/**
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
import * as fs from 'fs';
import * as os from 'os';
import * as path from 'path';
import * as _ from 'lodash';

import { BallerinaExtension, ExtendedLangClient, ConstructIdentifier, ballerinaExtInstance } from '../core';
import { ExtensionContext, commands, window, Uri, ViewColumn, TextDocumentChangeEvent, 
	workspace, WebviewPanel } from 'vscode';

import {render} from './renderer';
import { WebViewRPCHandler, getCommonWebViewOptions } from '../utils';
import { TM_EVENT_OPEN_PROJECT_OVERVIEW_VIA_CMD, CMP_PROJECT_OVERVIEW } from '../telemetry';

const DEBOUNCE_WAIT = 500;

let overviewPanel: WebviewPanel | undefined;
let rpcHandler: WebViewRPCHandler;

function updateWebView(docUri: Uri): void {
	if (rpcHandler) {
		rpcHandler.invokeRemoteMethod("updateAST", [docUri.toString()], () => {});
	}
}

export function activate(ballerinaExtInstance: BallerinaExtension) {
	const reporter = ballerinaExtInstance.telemetryReporter;
    const context = <ExtensionContext> ballerinaExtInstance.context;
	const langClient = <ExtendedLangClient> ballerinaExtInstance.langClient;

	function updateSelectedConstruct(construct: ConstructIdentifier) {
		// If Project Overview is already showing update it to show the selected construct
		if (overviewPanel) {
			if (rpcHandler) {
				const { moduleName, constructName, subConstructName } = construct;
				rpcHandler.invokeRemoteMethod("selectConstruct", [moduleName, constructName, subConstructName], () => {});
			}
			overviewPanel.reveal();
		} else {
			// If Project Overview is not yet opened open it and show the selected construct
			openWebView(context, langClient, construct);
		}
	}

	ballerinaExtInstance.onProjectTreeElementClicked((construct) => {
		updateSelectedConstruct(construct);
	});

	const projectOverviewDisposable = commands.registerCommand('ballerina.showProjectOverview', () => {
		reporter.sendTelemetryEvent(TM_EVENT_OPEN_PROJECT_OVERVIEW_VIA_CMD, { component: CMP_PROJECT_OVERVIEW });
		return ballerinaExtInstance.onReady()
		.then(() => {
			openWebView(context, langClient);
		}).catch((e) => {
			reporter.sendTelemetryException(e, { component: CMP_PROJECT_OVERVIEW });
		});
	});
    context.subscriptions.push(projectOverviewDisposable);
}

function openWebView(context: ExtensionContext, langClient: ExtendedLangClient, construct?: ConstructIdentifier) {
	let sourceRoot: string|undefined;
	let currentFilePath: string|undefined;

	const openFolders = workspace.workspaceFolders;
	if (openFolders) {
		if (fs.existsSync(path.join(openFolders[0].uri.path, "Ballerina.toml"))) {
			sourceRoot = openFolders[0].uri.path;
		}
	}

	if (!sourceRoot) {
		if (window.activeTextEditor) {
			currentFilePath = window.activeTextEditor.document.fileName;
			sourceRoot = getSourceRoot(currentFilePath, path.parse(currentFilePath).root);
		}
	}

	if (!currentFilePath && !sourceRoot) {
		return;
	}

	const options : {
		currentUri?: string,
		sourceRootUri?: string,
		construct?: ConstructIdentifier
	} = {
		construct,
	};

	if (sourceRoot) {
		options.sourceRootUri = Uri.file(sourceRoot).toString();
	}

	if (currentFilePath) {
		options.currentUri = Uri.file(currentFilePath).toString();
	}

	const didChangeDisposable = workspace.onDidChangeTextDocument(
		_.debounce((e: TextDocumentChangeEvent) => {
		updateWebView( e.document.uri);
	}, DEBOUNCE_WAIT));

	const didChangeActiveEditorDisposable = window.onDidChangeActiveTextEditor((activeEditor) => {
		if (!(activeEditor && activeEditor.document && activeEditor.document.languageId === "ballerina")) {
			return;
		}

		const newCurrentFilePath = activeEditor.document.fileName;
		const newSourceRoot = getSourceRoot(newCurrentFilePath, path.parse(newCurrentFilePath).root);
	
		const newOptions : {
			currentUri: string,
			sourceRootUri?: string,
			construct?: ConstructIdentifier
		} = {
			currentUri: Uri.file(newCurrentFilePath).toString(),
		};

		let shouldRerender = false;
		if (newSourceRoot) {
			shouldRerender = sourceRoot !== newSourceRoot;
			newOptions.sourceRootUri = Uri.file(newSourceRoot).toString();
		} else {
			shouldRerender = currentFilePath !== newCurrentFilePath;
		}

		if (shouldRerender) {
			currentFilePath = newCurrentFilePath;
			sourceRoot = newSourceRoot;
			const html = render(context, langClient, newOptions);
			if (overviewPanel && html) {
				overviewPanel.webview.html = html;
			}
		}
	});

	if (!overviewPanel) {
		overviewPanel = window.createWebviewPanel(
			'projectOverview',
			'Project Overview',
			{ viewColumn: ViewColumn.One, preserveFocus: true },
			getCommonWebViewOptions()
		);

		ballerinaExtInstance.addWebviewPanel("overview", overviewPanel);
	}

	rpcHandler = WebViewRPCHandler.create(overviewPanel, langClient);
	const html = render(context, langClient, options);
	if (overviewPanel && html) {
		overviewPanel.webview.html = html;
	}

	overviewPanel.onDidDispose(() => {
		overviewPanel = undefined;
		didChangeDisposable.dispose();
		didChangeActiveEditorDisposable.dispose();
	});
}

function getSourceRoot(currentPath: string, root: string): string|undefined {
	if (fs.existsSync(path.join(currentPath, 'Ballerina.toml'))) {
		if (currentPath !== os.homedir()) {
			return currentPath;
		}
	}

	if (currentPath === root) {
		return;
	}

	return getSourceRoot(path.dirname(currentPath), root);
}
