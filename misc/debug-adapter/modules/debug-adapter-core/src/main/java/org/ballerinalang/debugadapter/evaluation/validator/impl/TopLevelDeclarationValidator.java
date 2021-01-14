/*
 * Copyright (c) 2021, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ballerinalang.debugadapter.evaluation.validator.impl;

import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import org.ballerinalang.debugadapter.evaluation.parser.DebugParser;
import org.ballerinalang.debugadapter.evaluation.validator.Validator;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Validator implementation for ballerina top-level declarations(i.e. imports, services, etc.).
 *
 * @since 2.0.0
 */
public class TopLevelDeclarationValidator extends Validator {

    public TopLevelDeclarationValidator(DebugParser parser) {
        super(parser);
    }

    @Override
    public void validate(String source) throws Exception {
        SyntaxTree syntaxTree = debugParser.getSyntaxTreeFor(source);
        ModulePartNode moduleNode = syntaxTree.rootNode();

        // Checks for import declarations in user input.
        NodeList<ImportDeclarationNode> imports = moduleNode.imports();
        failIf(imports.size() > 0, "Import declaration evaluation is not supported.");

        // Checks for top-level declarations in user input.
        NodeList<ModuleMemberDeclarationNode> memberNodes = moduleNode.members();
        // Needs to filter out module variable declarations, since variable assignment statements can be parsed into
        // module-level variable declaration during this validation phase.
        List<ModuleMemberDeclarationNode> members = memberNodes.stream()
                .filter(node -> node.kind() != SyntaxKind.MODULE_VAR_DECL).collect(Collectors.toList());
        failIf(members.size() > 0, "Top-level declaration evaluation is not supported.");
    }
}
