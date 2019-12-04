/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.langserver.extensions.ballerina.syntaxhighlighter;

import org.ballerinalang.langserver.common.LSNodeVisitor;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.compiler.LSContext;
import org.ballerinalang.model.tree.TopLevelNode;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForeach;
import org.wso2.ballerinalang.compiler.tree.statements.BLangIf;
import org.wso2.ballerinalang.compiler.tree.statements.BLangMatch;
import org.wso2.ballerinalang.compiler.tree.statements.BLangSimpleVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWhile;
import org.wso2.ballerinalang.compiler.tree.types.BLangObjectTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangRecordTypeNode;

import java.util.List;
/**
 * Finds the symbols for Semantic Syntax Highlighting.
 *
 * @since 1.0.2
 */
public class SemanticHighlightingVisitor extends LSNodeVisitor {

    private List<SemanticHighlightProvider.HighlightInfo> highlights;

    public SemanticHighlightingVisitor(LSContext context) {
        this.highlights = context.get(SemanticHighlightKeys.SEMANTIC_HIGHLIGHTING_KEY);
    }

    @Override
    public void visit(BLangPackage pkgNode) {
        List<TopLevelNode> topLevelNodes = pkgNode.topLevelNodes;
        topLevelNodes.forEach(topLevelNode -> this.acceptNode((BLangNode) topLevelNode)); //TODO filter worker lambdas
    }

    @Override
    public void visit(BLangFunction funcNode) {
        this.acceptNode(funcNode.body);
    }

    @Override
    public void visit(BLangRecordTypeNode recordTypeNode) {
        if (recordTypeNode.fields != null) {
            recordTypeNode.fields.forEach(this::acceptNode);
        }
    }

    @Override
    public void visit(BLangIf ifNode) {
        this.acceptNode(ifNode.body);

        if (ifNode.elseStmt != null) {
            this.acceptNode(ifNode.elseStmt);
        }
    }

    @Override
    public void visit(BLangMatch matchNode) {
        matchNode.patternClauses.forEach(patternClause -> {
            this.acceptNode(patternClause.body);
        });
    }

    @Override
    public void visit(BLangForeach foreach) {
        this.acceptNode(foreach.body);
    }

    @Override
    public void visit(BLangWhile whileNode) {
        this.acceptNode(whileNode.body);
    }

    @Override
    public void visit(BLangBlockStmt blockNode) {
        blockNode.stmts.forEach(stmt-> {
            this.acceptNode(stmt);
        });
    }

    @Override
    public void visit(BLangTypeDefinition typeDefinition) {
        if (typeDefinition.typeNode != null) {
            this.acceptNode(typeDefinition.typeNode);
        }
    }

    @Override
    public void visit(BLangObjectTypeNode objectTypeNode) {
        if (objectTypeNode.fields != null) {
            objectTypeNode.fields.forEach(this::acceptNode);
        }

        if (objectTypeNode.functions != null) {
            objectTypeNode.functions.forEach(this::acceptNode);
        }

        if (objectTypeNode.initFunction != null) {
            this.acceptNode(objectTypeNode.initFunction);
        }
    }

    @Override
    public void visit(BLangSimpleVariableDef varDefNode) {
        this.acceptNode(varDefNode.var);
    }

    @Override
    public void visit(BLangSimpleVariable varNode) {
        if (CommonUtil.isClientObject(varNode.symbol)) {
            SemanticHighlightProvider.HighlightInfo highlightInfo =
                    new SemanticHighlightProvider.HighlightInfo(ScopeEnum.ENDPOINT, varNode.name);
            highlights.add(highlightInfo);
        }
    }

    private void acceptNode(BLangNode node) {
        node.accept(this);
    }

}
