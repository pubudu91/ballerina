/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.antlr.v4.runtime.Token;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.common.CommonKeys;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.compiler.LSContext;
import org.ballerinalang.langserver.completions.CompletionKeys;
import org.ballerinalang.langserver.completions.SymbolInfo;
import org.ballerinalang.langserver.completions.spi.LSCompletionProvider;
import org.ballerinalang.langserver.completions.util.Snippet;
import org.eclipse.lsp4j.CompletionItem;
import org.wso2.ballerinalang.compiler.parser.antlr4.BallerinaParser;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Completion Item Resolver for the Definition Context.
 *
 * @since v0.982.0
 */
@JavaSPIService("org.ballerinalang.langserver.completions.spi.LSCompletionProvider")
public class ServiceDefinitionContextProvider extends LSCompletionProvider {

    public ServiceDefinitionContextProvider() {
        this.attachmentPoints.add(BallerinaParser.ServiceDefinitionContext.class);
    }

    @Override
    public List<CompletionItem> getCompletions(LSContext context) {
        List<CompletionItem> completionItems = new ArrayList<>();
        List<CommonToken> lhsTokens = context.get(CompletionKeys.LHS_TOKENS_KEY);
        List<CommonToken> lhsDefaultTokens = lhsTokens.stream()
                .filter(commonToken -> commonToken.getChannel() == Token.DEFAULT_CHANNEL)
                .collect(Collectors.toList());
        int startIndex = lhsDefaultTokens.size() - 1;
        // Backtrack the tokens from the head of the popped tokens in order determine the cursor position
        tokenScanner:
        while (true) {
            if (startIndex < 0) {
                return completionItems;
            }
            CommonToken token = lhsDefaultTokens.get(startIndex);
            int tokenType = token.getType();
            switch (tokenType) {
                case BallerinaParser.SERVICE:
                case BallerinaParser.ON:
                case BallerinaParser.NEW:
                case BallerinaParser.COLON:
                    break tokenScanner;
                default:
                    break;
            }

            startIndex--;
        }

        switch (lhsDefaultTokens.get(startIndex).getType()) {
            case BallerinaParser.ON : {
                // suggest all the visible, defined listeners
                completionItems.addAll(this.getCompletionItemsAfterOnKeyword(context));
                break;
            }
            case BallerinaParser.NEW: {
                /*
                    service helloService on new <cursor> {
                    }
                    Ideally this should be a syntax error and current grammar do not support it
                    Also Issue #18729 is also broken
                 */
                List<SymbolInfo> visibleSymbols = new ArrayList<>(context.get(CommonKeys.VISIBLE_SYMBOLS_KEY));
                List<SymbolInfo> filteredSymbols = this.filterListenerTypes(visibleSymbols);
                completionItems.addAll(this.getCompletionItemList(filteredSymbols, context));
                completionItems.addAll(this.getPackagesCompletionItems(context));
                break;
            }
            case BallerinaParser.COLON: {
                List<SymbolInfo> listeners = this.filterListenersFromPackage(context);
                completionItems.addAll(this.getCompletionItemList(listeners, context));
                break;
            }
            default: {
                // Fill the on keyword completion item
                completionItems.add(Snippet.KW_ON.get().build(context));
                break;
            }
        }
        return completionItems;
    }

    private List<SymbolInfo> filterListenerTypes(List<SymbolInfo> symbolInfos) {
        return symbolInfos.stream()
                .filter(symbolInfo -> CommonUtil.isListenerObject(symbolInfo.getScopeEntry().symbol))
                .collect(Collectors.toList());
    }
    
    private List<SymbolInfo> filterListenersFromPackage(LSContext context) {
        List<CommonToken> defaultTokens = context.get(CompletionKeys.LHS_DEFAULT_TOKENS_KEY);
        List<Integer> tokenTypes = context.get(CompletionKeys.LHS_DEFAULT_TOKEN_TYPES_KEY);
        List<SymbolInfo> visibleSymbols = new ArrayList<>(context.get(CommonKeys.VISIBLE_SYMBOLS_KEY));
        if (tokenTypes == null) {
            return new ArrayList<>();
        }

        int colonIndex = tokenTypes.indexOf(BallerinaParser.COLON);
        if (colonIndex < 0) {
            return new ArrayList<>();
        }
        String pkgName = defaultTokens.get(colonIndex - 1).getText();

        Optional<SymbolInfo> symbolWithName = visibleSymbols.stream()
                .filter(symbolInfo -> symbolInfo.getSymbolName().equals(pkgName))
                .findAny();
        if (!symbolWithName.isPresent() || !(symbolWithName.get().getScopeEntry().symbol instanceof BPackageSymbol)) {
            return new ArrayList<>();
        }
        BPackageSymbol pkgSymbol = ((BPackageSymbol) symbolWithName.get().getScopeEntry().symbol);

        return pkgSymbol.scope.entries.values().stream()
                .filter(scopeEntry -> CommonUtil.isListenerObject(scopeEntry.symbol))
                .map(scopeEntry -> new SymbolInfo(scopeEntry.symbol.getName().getValue(), scopeEntry))
                .collect(Collectors.toList());
    }
}
