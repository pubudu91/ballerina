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
package io.ballerina.compiler.api.impl.types;

import io.ballerina.compiler.api.ModuleID;
import io.ballerina.compiler.api.symbols.MethodSymbol;
import io.ballerina.compiler.api.types.BallerinaTypeDescriptor;
import io.ballerina.compiler.api.types.TypeReferenceTypeDescriptor;
import io.ballerina.compiler.api.types.util.TypeDescKind;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;

import java.util.List;

/**
 * Represents a TypeReference type descriptor.
 *
 * @since 2.0.0
 */
public class BallerinaTypeReferenceTypeDescriptor extends AbstractTypeDescriptor
        implements TypeReferenceTypeDescriptor {

    private static final String ANON_ORG = "$anon";
    private final String definitionName;
    private BallerinaTypeDescriptor typeDescriptorImpl;

    public BallerinaTypeReferenceTypeDescriptor(ModuleID moduleID, BType bType, String definitionName) {
        super(TypeDescKind.TYPE_REFERENCE, moduleID, bType);
        this.definitionName = definitionName;
    }

    public BallerinaTypeReferenceTypeDescriptor(ModuleID moduleID, BallerinaTypeDescriptor type, BType bType,
                                                String definitionName) {
        super(TypeDescKind.TYPE_REFERENCE, moduleID, bType);
        this.definitionName = definitionName;
        this.typeDescriptorImpl = type;
    }

    @Override
    public BallerinaTypeDescriptor typeDescriptor() {
        return this.typeDescriptorImpl;
    }

    @Override
    public String name() {
        return this.definitionName;
    }

    @Override
    public List<MethodSymbol> langLibMethods() {
        return typeDescriptorImpl.langLibMethods();
    }

    @Override
    public String signature() {
        if (this.moduleID() == null || (this.moduleID().moduleName().equals("lang.annotations") &&
                this.moduleID().orgName().equals("ballerina"))) {
            return this.definitionName;
        }
        return !this.isAnonOrg(this.moduleID()) ? this.moduleID().modulePrefix() + ":" + this.definitionName
                : this.definitionName;
    }

    private boolean isAnonOrg(ModuleID moduleID) {
        return ANON_ORG.equals(moduleID.orgName());
    }
}