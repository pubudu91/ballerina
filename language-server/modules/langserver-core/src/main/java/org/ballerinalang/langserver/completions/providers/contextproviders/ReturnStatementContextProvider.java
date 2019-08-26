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
package org.ballerinalang.langserver.completions.providers.contextproviders;

import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.CommonKeys;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.compiler.LSContext;
import org.ballerinalang.langserver.completions.CompletionKeys;
import org.ballerinalang.langserver.completions.SymbolInfo;
import org.ballerinalang.langserver.completions.spi.LSCompletionProvider;
import org.ballerinalang.langserver.completions.util.Snippet;
import org.ballerinalang.langserver.completions.util.filters.DelimiterBasedContentFilter;
import org.ballerinalang.langserver.completions.util.filters.SymbolFilters;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.wso2.ballerinalang.compiler.parser.antlr4.BallerinaParser;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.util.Flags;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Completion provider for the return parser rule context.
 * 
 * @since 1.0
 */
@JavaSPIService("org.ballerinalang.langserver.completions.spi.LSCompletionProvider")
public class ReturnStatementContextProvider extends LSCompletionProvider {

    public ReturnStatementContextProvider() {
        this.attachmentPoints.add(BallerinaParser.ReturnStatementContext.class);
    }

    @Override
    public List<CompletionItem> getCompletions(LSContext ctx) {
        ArrayList<CompletionItem> completionItems = new ArrayList<>();
        List<SymbolInfo> visibleSymbols = new ArrayList<>(ctx.get(CommonKeys.VISIBLE_SYMBOLS_KEY));
        Integer invocationType = ctx.get(CompletionKeys.INVOCATION_TOKEN_TYPE_KEY);
        
        if (invocationType > -1) {
            Either<List<CompletionItem>, List<SymbolInfo>> filteredItems =
                    SymbolFilters.get(DelimiterBasedContentFilter.class).filterItems(ctx);
            return this.getCompletionItemList(filteredItems, ctx);
        }
        // Remove the functions without a receiver symbol, bTypes not being packages and attached functions
        List<SymbolInfo> filteredList = visibleSymbols.stream().filter(symbolInfo -> {
            BSymbol bSymbol = symbolInfo.getScopeEntry().symbol;
            return !((bSymbol instanceof BInvokableSymbol
                    && ((BInvokableSymbol) bSymbol).receiverSymbol != null
                    && CommonUtil.isValidInvokableSymbol(bSymbol))
                    || ((bSymbol instanceof BTypeSymbol)
                    && !(bSymbol instanceof BPackageSymbol))
                    || (bSymbol instanceof BInvokableSymbol
                    && ((bSymbol.flags & Flags.ATTACHED) == Flags.ATTACHED)));
        }).collect(Collectors.toList());

        completionItems.add(Snippet.KW_CHECK_PANIC.get().build(ctx));
        completionItems.add(Snippet.KW_CHECK.get().build(ctx));
        completionItems.addAll(getCompletionItemList(filteredList, ctx));
        completionItems.addAll(this.getPackagesCompletionItems(ctx));
        
        return completionItems;
    }
}
