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
package org.ballerina.compiler.api.symbol;

import org.ballerina.compiler.api.element.DocAttachment;
import org.ballerina.compiler.api.element.ModuleID;

import java.util.Optional;

/**
 * Represents a compiled language symbol.
 *
 * @since 2.0.0
 */
public interface BCompiledSymbol {
    /**
     * Get the symbol name.
     * 
     * @return {@link String} name  of the symbol
     */
    String name();

    /**
     * Get the moduleID of the symbol.
     * 
     * @return {@link ModuleID} of the symbol
     */
    ModuleID moduleID();

    /**
     * Get the Symbol Kind.
     * 
     * @return {@link BallerinaSymbolKind} of the symbol
     */
    BallerinaSymbolKind kind();

    /**
     * Get the Documentation attachment bound to the symbol.
     * 
     * @return {@link Optional} doc attachment
     */
    Optional<DocAttachment> docAttachment();
    
    // TODO: Add the annotation attachment API
}