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
package org.ballerina.compiler.api.model;

import org.ballerina.compiler.api.semantic.SymbolFactory;
import org.ballerina.compiler.api.semantic.TypesFactory;
import org.ballerina.compiler.api.types.TypeDescriptor;
import org.ballerinalang.model.elements.PackageID;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.util.Flags;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Represent Function Symbol.
 * 
 * @since 1.3.0
 */
public class BallerinaFunctionSymbol extends BallerinaVariable {
    private List<BallerinaParameter> parameters;
    private BallerinaParameter restParam;
    private TypeDescriptor returnType;
    
    private BallerinaFunctionSymbol(String name,
                                    PackageID moduleID,
                                    BallerinaSymbolKind symbolKind,
                                    List<AccessModifier> accessModifiers,
                                    TypeDescriptor typeDescriptor,
                                    BInvokableSymbol invokableSymbol) {
        super(name, moduleID, symbolKind, accessModifiers, typeDescriptor, invokableSymbol);
        this.parameters = invokableSymbol.params.stream()
                .map(SymbolFactory::createBallerinaParameter)
                .collect(Collectors.toList());
        this.restParam = SymbolFactory.createBallerinaParameter(invokableSymbol.restParam);
        this.returnType = TypesFactory.getTypeDescriptor(invokableSymbol.retType);
    }

    /**
     * Get the Required parameters.
     * 
     * @return {@link List} of return parameters
     */
    public List<BallerinaParameter> getParams() {
        return this.parameters;
    }

    /**
     * Get the rest parameter.
     * 
     * @return {@link Optional} rest parameter
     */
    public Optional<BallerinaParameter> getRestParam() {
        return Optional.ofNullable(restParam);
    }

    /**
     * Get the return parameter.
     * 
     * @return {@link Optional} return type descriptor
     */
    public Optional<TypeDescriptor> getReturnType() {
        return Optional.ofNullable(returnType);
    }

    /**
     * Represents Ballerina XML Namespace Symbol Builder.
     */
    public static class FunctionSymbolBuilder extends BallerinaVariable.VariableSymbolBuilder {
        
        public FunctionSymbolBuilder(String name, PackageID moduleID, BInvokableSymbol bSymbol) {
            super(name, moduleID, bSymbol);
        }

        public BallerinaFunctionSymbol build() {
            BallerinaSymbolKind symbolKind;
            if ((this.bSymbol.flags & Flags.REMOTE) == Flags.REMOTE) {
                symbolKind = BallerinaSymbolKind.REMOTE_FUNCTION;
            } else {
                symbolKind = BallerinaSymbolKind.FUNCTION;
            }
            return new BallerinaFunctionSymbol(this.name,
                    this.moduleID,
                    symbolKind,
                    this.accessModifiers,
                    this.typeDescriptor,
                    (BInvokableSymbol) this.bSymbol);
        }

    }
}
