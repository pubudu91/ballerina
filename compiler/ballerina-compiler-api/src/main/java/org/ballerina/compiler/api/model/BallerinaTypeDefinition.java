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

import org.ballerina.compiler.api.types.TypeDescriptor;
import org.ballerinalang.model.elements.PackageID;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;

import java.util.List;

/**
 * Represents a ballerina type definition.
 * 
 * @since 1.3.0
 */
public class BallerinaTypeDefinition extends BallerinaVariable {
    
    protected BallerinaTypeDefinition(String name,
                                      PackageID moduleID,
                                      List<AccessModifier> accessModifiers,
                                      TypeDescriptor typeDescriptor,
                                      BSymbol bSymbol) {
        this(name, moduleID, accessModifiers, BallerinaSymbolKind.TYPE_DEF, typeDescriptor, bSymbol);
    }
    
    protected BallerinaTypeDefinition(String name,
                                      PackageID moduleID,
                                      List<AccessModifier> accessModifiers,
                                      BallerinaSymbolKind symbolKind,
                                      TypeDescriptor typeDescriptor,
                                      BSymbol bSymbol) {
        super(name, moduleID, symbolKind, accessModifiers, typeDescriptor, bSymbol);
    }
    
    public String getModuleQualifiedName() {
        return this.getModuleID().getModuleName() + ":" + this.getName();
    }

    /**
     * Represents a type definition symbol builder.
     * 
     * @since 1.3.0
     */
    public static class TypeDefSymbolBuilder extends BallerinaVariable.VariableSymbolBuilder {
        
        public TypeDefSymbolBuilder(String name, PackageID moduleID, BSymbol symbol) {
            super(name, moduleID, symbol);
        }

        @Override
        public TypeDefSymbolBuilder withTypeDescriptor(TypeDescriptor typeDescriptor) {
            return (TypeDefSymbolBuilder) super.withTypeDescriptor(typeDescriptor);
        }

        @Override
        public TypeDefSymbolBuilder withAccessModifier(AccessModifier accessModifier) {
            return (TypeDefSymbolBuilder) super.withAccessModifier(accessModifier);
        }

        @Override
        public BallerinaTypeDefinition build() {
            return new BallerinaTypeDefinition(this.name,
                    this.moduleID,
                    this.accessModifiers,
                    this.typeDescriptor,
                    this.bSymbol);
        }
    }
}
