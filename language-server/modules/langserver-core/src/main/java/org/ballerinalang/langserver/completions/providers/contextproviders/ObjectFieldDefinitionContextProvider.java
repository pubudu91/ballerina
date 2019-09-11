/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.langserver.completions.providers.contextproviders;

import org.antlr.v4.runtime.CommonToken;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.CommonKeys;
import org.ballerinalang.langserver.compiler.LSContext;
import org.ballerinalang.langserver.completions.CompletionKeys;
import org.ballerinalang.langserver.completions.SymbolInfo;
import org.ballerinalang.langserver.completions.spi.LSCompletionProvider;
import org.ballerinalang.langserver.completions.util.Snippet;
import org.ballerinalang.langserver.completions.util.sorters.ActionAndFieldAccessContextItemSorter;
import org.eclipse.lsp4j.CompletionItem;
import org.wso2.ballerinalang.compiler.parser.antlr4.BallerinaParser;

import java.util.ArrayList;
import java.util.List;

/**
 * Parser rule based variable definition statement context resolver.
 */
@JavaSPIService("org.ballerinalang.langserver.completions.spi.LSCompletionProvider")
public class ObjectFieldDefinitionContextProvider extends LSCompletionProvider {
    public ObjectFieldDefinitionContextProvider() {
        this.attachmentPoints.add(BallerinaParser.ObjectFieldDefinitionContext.class);
    }

    @Override
    public List<CompletionItem> getCompletions(LSContext ctx) {
        ArrayList<CompletionItem> completionItems = new ArrayList<>();
        List<CommonToken> lhsTokens = ctx.get(CompletionKeys.LHS_DEFAULT_TOKENS_KEY);
        List<Integer> lhsTokenTypes = ctx.get(CompletionKeys.LHS_DEFAULT_TOKEN_TYPES_KEY);
        List<SymbolInfo> visibleSymbols = new ArrayList<>(ctx.get(CommonKeys.VISIBLE_SYMBOLS_KEY));
        int invocationOrDelimiterTokenType = ctx.get(CompletionKeys.INVOCATION_TOKEN_TYPE_KEY);

        if (lhsTokenTypes.contains(BallerinaParser.ASSIGN)) {
            return this.getProvider(BallerinaParser.VariableDefinitionStatementContext.class).getCompletions(ctx);
        }

        if (invocationOrDelimiterTokenType == BallerinaParser.COLON) {
            String pkgName = lhsTokens.get(lhsTokenTypes.indexOf(invocationOrDelimiterTokenType) - 1).getText();
            completionItems.addAll(this.getTypesInPackage(visibleSymbols, pkgName, ctx));
            ctx.put(CompletionKeys.ITEM_SORTER_KEY, ActionAndFieldAccessContextItemSorter.class);
            return completionItems;
        }

        completionItems.addAll(this.getBasicTypes(visibleSymbols));
        completionItems.addAll(this.getPackagesCompletionItems(ctx));

        completionItems.add(Snippet.DEF_FUNCTION_SIGNATURE.get().build(ctx));
        completionItems.add(Snippet.DEF_FUNCTION.get().build(ctx));
        completionItems.add(Snippet.DEF_REMOTE_FUNCTION.get().build(ctx));
        completionItems.add(Snippet.DEF_INIT_FUNCTION.get().build(ctx));
        completionItems.add(Snippet.DEF_ATTACH_FUNCTION.get().build(ctx));
        completionItems.add(Snippet.DEF_DETACH_FUNCTION.get().build(ctx));
        completionItems.add(Snippet.DEF_START_FUNCTION.get().build(ctx));
        completionItems.add(Snippet.DEF_GRACEFUL_STOP_FUNCTION.get().build(ctx));
        completionItems.add(Snippet.DEF_IMMEDIATE_STOP_FUNCTION.get().build(ctx));
        completionItems.add(Snippet.KW_PUBLIC.get().build(ctx));
        completionItems.add(Snippet.KW_PRIVATE.get().build(ctx));

        return completionItems;
    }
}
