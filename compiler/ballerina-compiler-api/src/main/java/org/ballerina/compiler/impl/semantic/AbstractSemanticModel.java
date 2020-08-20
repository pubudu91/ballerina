/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerina.compiler.impl.semantic;

import io.ballerinalang.compiler.text.LinePosition;
import io.ballerinalang.compiler.text.TextRange;
import org.ballerina.compiler.api.symbol.BCompiledSymbol;
import org.ballerina.compiler.impl.symbol.BallerinaSymbolImpl;
import org.ballerinalang.util.diagnostic.Diagnostic;
import org.wso2.ballerinalang.compiler.tree.BLangCompilationUnit;

import java.util.List;
import java.util.Optional;

/**
 * Represents an Abstract semantic model.
 *
 * @since 2.0.0
 */
public abstract class AbstractSemanticModel {

    protected BLangCompilationUnit compilationUnit;
    // TODO: Private field to capture the syntax tree associated to the semantic model

    public AbstractSemanticModel(BLangCompilationUnit compilationUnit) {
        this.compilationUnit = compilationUnit;
    }

    /**
     * Lookup the visible symbols at the given location.
     *
     * @param position text position in the source
     * @return {@link List} of visible symbols in the given location
     */
    public abstract List<BCompiledSymbol> lookupSymbols(LinePosition position);

    /**
     * Lookup the symbol at the given location.
     *
     * @param position text position in the source
     * @return {@link BallerinaSymbolImpl} in the given location
     */
    public abstract Optional<BCompiledSymbol> lookupSymbol(int position);

    /**
     * Get the list of symbols visible to the given location, with the given name.
     *
     * @param textPosition text position
     * @param name         symbol name to match
     * @return {@link List} of symbols visible to the position, with the name
     */
    public abstract List<BCompiledSymbol> symbolByName(LinePosition textPosition, String name);

    /**
     * Get the diagnostics within the given text Span.
     *
     * @param textRange Text range to filter the diagnostics
     * @param kind      Diagnostic kind, can be Syntactic, Semantic or both
     * @return {@link List} of extracted diagnostics
     */
    public abstract List<Diagnostic> diagnostics(TextRange textRange, DiagnosticKind kind);
}