/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package io.ballerina.plugins.idea.extensions.client;

import com.google.common.base.Strings;
import com.google.gson.JsonObject;
import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.lookup.AutoCompletionPolicy;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.editor.event.EditorMouseListener;
import com.intellij.openapi.editor.event.EditorMouseMotionListener;
import com.intellij.openapi.util.TextRange;
import io.ballerina.plugins.idea.extensions.server.BallerinaASTDidChange;
import io.ballerina.plugins.idea.extensions.server.BallerinaASTDidChangeResponse;
import io.ballerina.plugins.idea.extensions.server.BallerinaASTRequest;
import io.ballerina.plugins.idea.extensions.server.BallerinaASTResponse;
import io.ballerina.plugins.idea.extensions.server.BallerinaEndpointsResponse;
import io.ballerina.plugins.idea.extensions.server.ModulesRequest;
import io.ballerina.plugins.idea.extensions.server.ModulesResponse;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.CompletionParams;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.eclipse.lsp4j.jsonrpc.JsonRpcException;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.jetbrains.annotations.Nullable;
import org.wso2.lsp4intellij.client.languageserver.ServerOptions;
import org.wso2.lsp4intellij.client.languageserver.requestmanager.RequestManager;
import org.wso2.lsp4intellij.client.languageserver.wrapper.LanguageServerWrapper;
import org.wso2.lsp4intellij.contributors.icon.LSPIconProvider;
import org.wso2.lsp4intellij.editor.EditorEventManager;
import org.wso2.lsp4intellij.requests.Timeouts;
import org.wso2.lsp4intellij.utils.ApplicationUtils;
import org.wso2.lsp4intellij.utils.DocumentUtils;
import org.wso2.lsp4intellij.utils.GUIUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.swing.Icon;

import static org.wso2.lsp4intellij.requests.Timeout.getTimeout;
import static org.wso2.lsp4intellij.requests.Timeouts.COMPLETION;
import static org.wso2.lsp4intellij.utils.ApplicationUtils.computableReadAction;

/**
 * Editor event manager extension for Ballerina language.
 */
public class BallerinaEditorEventManager extends EditorEventManager {

    private static final Logger LOG = Logger.getInstance(BallerinaEditorEventManager.class);
    private static final int TIMEOUT_AST = 3000;
    private static final int TIMEOUT_PROJECT_AST = 5000;
    private static final int TIMEOUT_ENDPOINTS = 2000;

    public BallerinaEditorEventManager(Editor editor, DocumentListener documentListener,
                                       EditorMouseListener mouseListener, EditorMouseMotionListener mouseMotionListener,
                                       RequestManager requestManager, ServerOptions serverOptions,
                                       LanguageServerWrapper wrapper) {
        super(editor, documentListener, mouseListener, mouseMotionListener, requestManager, serverOptions, wrapper);
    }

    @Nullable
    public BallerinaASTResponse getAST() throws TimeoutException, ExecutionException, InterruptedException {
        BallerinaRequestManager ballerinaRequestManager = (BallerinaRequestManager) getRequestManager();
        BallerinaASTRequest astRequest = new BallerinaASTRequest();
        astRequest.setDocumentIdentifier(getIdentifier());
        CompletableFuture<BallerinaASTResponse> future = ballerinaRequestManager.ast(astRequest);
        if (future != null) {
            try {
                return future.get(TIMEOUT_AST, TimeUnit.MILLISECONDS);
            } catch (TimeoutException e) {
                throw e;
            } catch (InterruptedException | JsonRpcException | ExecutionException e) {
                // Todo - Enable after fixing
                // wrapper.crashed(e);
                throw e;
            }
        }
        return null;
    }

    @Nullable
    public ModulesResponse getProjectAST(String sourceRoot) throws TimeoutException, ExecutionException,
            InterruptedException {
        BallerinaRequestManager ballerinaRequestManager = (BallerinaRequestManager) getRequestManager();
        ModulesRequest ballerinaModuleRequest = new ModulesRequest();
        ballerinaModuleRequest.setSourceRoot(sourceRoot);
        CompletableFuture<ModulesResponse> future = ballerinaRequestManager.modules(ballerinaModuleRequest);
        if (future != null) {
            try {
                return future.get(TIMEOUT_PROJECT_AST, TimeUnit.MILLISECONDS);
            } catch (TimeoutException e) {
                throw e;
            } catch (InterruptedException | JsonRpcException | ExecutionException e) {
                // Todo - Enable after fixing
                // wrapper.crashed(e);
                throw e;
            }
        }
        return null;
    }

    @Nullable
    public BallerinaASTDidChangeResponse astDidChange(JsonObject ast, String uri) throws
            TimeoutException, ExecutionException, InterruptedException {

        BallerinaRequestManager ballerinaRequestManager = (BallerinaRequestManager) getRequestManager();
        BallerinaASTDidChange didChangeNotification = new BallerinaASTDidChange();
        didChangeNotification.setTextDocumentIdentifier(new VersionedTextDocumentIdentifier(uri, Integer.MAX_VALUE));
        didChangeNotification.setAst(ast);
        CompletableFuture<BallerinaASTDidChangeResponse> future = ballerinaRequestManager
                .astDidChange(didChangeNotification);
        if (future != null) {
            try {
                return future.get(TIMEOUT_AST, TimeUnit.MILLISECONDS);
            } catch (TimeoutException e) {
                throw e;
            } catch (InterruptedException | JsonRpcException | ExecutionException e) {
                // Todo - Enable after fixing
                // wrapper.crashed(e);
                throw e;
            }
        }
        return null;
    }

    public BallerinaEndpointsResponse getEndpoints() {
        BallerinaRequestManager ballerinaRequestManager = (BallerinaRequestManager) getRequestManager();
        CompletableFuture<BallerinaEndpointsResponse> future = ballerinaRequestManager
                .endpoints();
        if (future != null) {
            try {
                return future.get(TIMEOUT_ENDPOINTS, TimeUnit.MILLISECONDS);
            } catch (TimeoutException e) {
                LOG.warn(e);
                return null;
            } catch (InterruptedException | JsonRpcException | ExecutionException e) {
                LOG.warn(e);
                // Todo - Enable after fixing
                // wrapper.crashed(e);
                return null;
            }
        }
        return null;
    }

    @Override
    public Iterable<? extends LookupElement> completion(Position pos) {
        List<LookupElement> lookupItems = new ArrayList<>();
        CompletableFuture<Either<List<CompletionItem>, CompletionList>> request = getRequestManager()
                .completion(new CompletionParams(getIdentifier(), pos));
        if (request == null) {
            return lookupItems;
        }

        try {
            Either<List<CompletionItem>, CompletionList> res = request
                    .get(getTimeout(COMPLETION), TimeUnit.MILLISECONDS);
            wrapper.notifySuccess(Timeouts.COMPLETION);
            if (res == null) {
                return lookupItems;
            }
            if (res.getLeft() != null) {
                for (CompletionItem item : res.getLeft()) {
                    LookupElement lookupElement = createLookupItem(item, pos);
                    if (lookupElement != null) {
                        lookupItems.add(lookupElement);
                    }
                }
            } else if (res.getRight() != null) {
                for (CompletionItem item : res.getRight().getItems()) {
                    LookupElement lookupElement = createLookupItem(item, pos);
                    if (lookupElement != null) {
                        lookupItems.add(lookupElement);
                    }
                }
            }
        } catch (InterruptedException ignored) {
            // can be ignored
        } catch (TimeoutException e) {
            wrapper.notifyFailure(COMPLETION);
        } catch (JsonRpcException | ExecutionException e) {
            LOG.warn(e);
            wrapper.crashed(e);
        } finally {
            return lookupItems;
        }
    }

    public LookupElement createLookupItem(CompletionItem item, Position position) {
        Command command = item.getCommand();
        String detail = item.getDetail();
        String insertText = item.getInsertText();
        // Hack to avoid potential NPE since lang client does not handle null completion kinds.
        // Todo - Remove after adding a proper fix to language client library.
        CompletionItemKind kind = item.getKind() != null ? item.getKind() : CompletionItemKind.Property;
        String label = item.getLabel();
        TextEdit textEdit = item.getTextEdit();
        List<TextEdit> addTextEdits = item.getAdditionalTextEdits();
        String presentableText = !Strings.isNullOrEmpty(label) ? label : (insertText != null) ? insertText : "";
        String tailText = (detail != null) ? detail : "";
        LSPIconProvider iconProvider = GUIUtils.getIconProviderFor(wrapper.getServerDefinition());
        Icon icon = iconProvider.getCompletionIcon(kind);

        // Todo - Remove after improving language client icons.
        if (icon == null) {
            icon = AllIcons.Nodes.Property;
        }
        LookupElementBuilder lookupElementBuilder;

        String lookupString = null;
        if (textEdit != null) {
            lookupString = textEdit.getNewText();
        } else if (!Strings.isNullOrEmpty(insertText)) {
            lookupString = insertText;

        } else if (!Strings.isNullOrEmpty(label)) {
            lookupString = label;
        }
        if (Strings.isNullOrEmpty(lookupString)) {
            return null;
        }

        // Fixes IDEA internal assertion failure in windows.
        lookupString = lookupString.replace(DocumentUtils.WIN_SEPARATOR, DocumentUtils.LINUX_SEPARATOR);

        if (lookupString.contains(DocumentUtils.LINUX_SEPARATOR)) {
            lookupString = insertIndents(lookupString, position);
        }
        lookupElementBuilder = LookupElementBuilder.create(lookupString);

        if (textEdit != null) {
            if (addTextEdits != null) {
                lookupElementBuilder = setInsertHandler(lookupElementBuilder, addTextEdits, command, label);
            }
        } else if (addTextEdits != null) {
            lookupElementBuilder = setInsertHandler(lookupElementBuilder, addTextEdits, command, label);
        } else if (command != null) {
            if (command.getCommand().equals("editor.action.triggerParameterHints")) {
                // This is a vscode internal command to trigger signature help after completion. This needs to be done
                // manually since intellij language client does not have that support.
                String finalLookupString = lookupString;
                lookupElementBuilder = lookupElementBuilder
                        .withInsertHandler((InsertionContext context, LookupElement lookupElement) -> {
                            context.commitDocument();
                            Caret currentCaret = context.getEditor().getCaretModel().getCurrentCaret();
                            currentCaret.moveCaretRelatively(finalLookupString.lastIndexOf(")") -
                                    finalLookupString.length(), 0, false, false);
                            signatureHelp();
                        });
            } else {
                lookupElementBuilder = lookupElementBuilder
                        .withInsertHandler((InsertionContext context, LookupElement lookupElement) -> {
                            context.commitDocument();
                            executeCommands(Collections.singletonList(command));
                        });
            }
        }

        if (kind == CompletionItemKind.Keyword) {
            lookupElementBuilder = lookupElementBuilder.withBoldness(true);
        }

        return lookupElementBuilder.withPresentableText(presentableText).withTypeText(tailText, true).withIcon(icon)
                .withAutoCompletionPolicy(AutoCompletionPolicy.SETTINGS_DEPENDENT);
    }

    private String insertIndents(String lookupString, Position position) {
        return computableReadAction(() -> {
            try {
                Document doc = editor.getDocument();
                int lineStartOff = doc.getLineStartOffset(position.getLine());
                int lineEndOff = doc.getLineEndOffset(position.getLine());
                String line = doc.getText(new TextRange(lineStartOff, lineEndOff)).replace("\t", "    ");
                String trimmed = line.trim();

                int startOffsetInLine;
                if (trimmed.isEmpty()) {
                    startOffsetInLine = line.length();
                } else {
                    startOffsetInLine = line.indexOf(trimmed);
                }

                int tabs = startOffsetInLine / 4;
                int spaces = startOffsetInLine % 4;
                return lookupString.replaceAll(DocumentUtils.LINUX_SEPARATOR, DocumentUtils.LINUX_SEPARATOR +
                        StringUtils.repeat("\t", tabs) + StringUtils.repeat(" ", spaces));
            } catch (Exception e) {
                LOG.warn("Error occurred when processing completion lookup string");
                return lookupString;
            }
        });
    }

    @Override
    public void documentChanged(DocumentEvent event) {
        if (editor.isDisposed()) {
            return;
        }
        if (event.getDocument() == editor.getDocument()) {
            DidChangeTextDocumentParams changeParams = getChangesParams();
            changeParams.getContentChanges().get(0).setText(editor.getDocument().getText());
            this.getRequestManager().didChange(changeParams);
        } else {
            LOG.error("Wrong document for the EditorEventManager");
        }
    }

    public void setFocus(JsonObject start, JsonObject end) {
        try {
            Position startPos = new Position(start.get("line").getAsInt() - 1, start.get("character").getAsInt() - 1);
            Position endPos = new Position(end.get("line").getAsInt() - 1, end.get("character").getAsInt() - 1);
            int startOffset = DocumentUtils.LSPPosToOffset(editor, startPos);
            int endOffset = DocumentUtils.LSPPosToOffset(editor, endPos);
            ApplicationUtils.invokeLater(() -> {
                // Scrolls to the given offset.
                editor.getScrollingModel().scrollTo(new LogicalPosition(startPos.getLine(), startPos.getCharacter()),
                        ScrollType.CENTER);
                // Highlights selected range.
                editor.getSelectionModel().setSelection(startOffset, endOffset);
            });
        } catch (Exception e) {
            LOG.warn("Couldn't process source focus request from diagram editor.", e);
        }
    }
}
