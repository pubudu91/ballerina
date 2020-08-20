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
package org.ballerina.compiler.impl.types;

import org.ballerina.compiler.api.element.ModuleID;
import org.ballerina.compiler.api.type.BallerinaTypeDescriptor;
import org.ballerina.compiler.api.type.TypeDescKind;
import org.ballerina.compiler.impl.semantic.BallerinaTypeDesc;
import org.ballerina.compiler.impl.semantic.TypesFactory;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;

/**
 * Represents an array type descriptor.
 *
 * @since 1.3.0
 */
public class ArrayTypeDescriptor extends BallerinaTypeDesc {

    private BallerinaTypeDescriptor memberTypeDesc;
    
    public ArrayTypeDescriptor(ModuleID moduleID,
                               BArrayType arrayType) {
        super(TypeDescKind.ARRAY, moduleID, arrayType);
    }

    public BallerinaTypeDescriptor getMemberTypeDescriptor() {
        if (this.memberTypeDesc == null) {
            this.memberTypeDesc = TypesFactory.getTypeDescriptor(((BArrayType) this.getBType()).eType);
        }
        return memberTypeDesc;
    }

    @Override
    public String signature() {
        return this.getMemberTypeDescriptor().signature() + "[]";
    }
}