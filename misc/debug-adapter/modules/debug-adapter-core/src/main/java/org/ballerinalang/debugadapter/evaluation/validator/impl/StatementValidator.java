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

import io.ballerina.compiler.syntax.tree.FunctionBodyBlockNode;
import io.ballerina.compiler.syntax.tree.FunctionBodyNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.StatementNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import org.ballerinalang.debugadapter.evaluation.parser.StatementParser;
import org.ballerinalang.debugadapter.evaluation.validator.Validator;
import org.ballerinalang.debugadapter.evaluation.validator.ValidatorException;

import static org.ballerinalang.debugadapter.evaluation.parser.StatementParser.BAL_WRAPPER_FUNCTION_NAME;

/**
 * Validator implementation for ballerina statements.
 *
 * @since 2.0.0
 */
public class StatementValidator extends Validator {

    public StatementValidator() {
        this(new StatementParser());
    }

    public StatementValidator(StatementParser parser) {
        super(parser);
    }

    @Override
    public void validate(String source) throws Exception {
        SyntaxTree syntaxTree = debugParser.getSyntaxTreeFor(source);
        NodeList<StatementNode> statements = getStatementsFrom(syntaxTree);
        failIf(statements.isEmpty(), "No expressions found.");
        failIf(statements.size() > 0, "Statement evaluation is not supported.");
    }

    protected static NodeList<StatementNode> getStatementsFrom(SyntaxTree syntaxTree) throws ValidatorException {
        ModulePartNode moduleNode = syntaxTree.rootNode();
        ModuleMemberDeclarationNode function = moduleNode.members().get(0);
        failIf(function.kind() != SyntaxKind.FUNCTION_DEFINITION, "Top-level declaration evaluation is not supported");

        String name = ((FunctionDefinitionNode) function).functionName().toSourceCode().trim();
        failIf(name.equals(BAL_WRAPPER_FUNCTION_NAME), "Top-level declaration evaluation is not supported.");

        FunctionBodyNode functionBody = ((FunctionDefinitionNode) function).functionBody();
        return ((FunctionBodyBlockNode) functionBody).statements();
    }
}
