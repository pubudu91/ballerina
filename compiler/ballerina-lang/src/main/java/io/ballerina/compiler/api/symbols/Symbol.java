/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerina.compiler.api.symbols;

import io.ballerina.compiler.api.ModuleID;
import io.ballerina.tools.diagnostics.Location;

import java.util.Optional;

/**
 * Represents a compiled language symbol.
 *
 * @since 2.0.0
 */
public interface Symbol {

    /**
     * Retrieves the name of the symbol if it is associated with an identifier.
     *
     * @return The name of the symbol if applicable
     */
    Optional<String> getName();

    /**
     * Get the moduleID of the symbol.
     *
     * @return {@link ModuleID} of the symbol
     * @deprecated This method will be removed in a later version and be replaced with a new method which will return
     * the module symbol instead.
     */
    @Deprecated
    ModuleID moduleID();

    /**
     * Retrieves the symbol of the module this symbol belongs to.
     *
     * @return The {@link ModuleSymbol} of the module
     */
    Optional<ModuleSymbol> getModule();

    /**
     * Get the Symbol Kind.
     *
     * @return {@link SymbolKind} of the symbol
     */
    SymbolKind kind();

    /**
     * This retrieves position information of the symbol in the source code. The position given here is the location of
     * the definition of the symbol.
     *
     * @return The position information of the symbol
     * @deprecated This method will be removed in a later version and be replaced with the getLocation() method.
     */
    @Deprecated
    Location location();

    /**
     * Retrieves the location of the symbol if the symbol is associated with an identifier in a source file.
     *
     * @return The location of the symbol if applicable
     */
    Optional<Location> getLocation();
}
