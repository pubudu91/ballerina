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
import io.ballerina.compiler.api.types.BallerinaTypeDescriptor;
import io.ballerina.compiler.api.types.FutureTypeDescriptor;
import io.ballerina.compiler.api.types.util.TypeDescKind;
import org.wso2.ballerinalang.compiler.semantics.model.types.BFutureType;

import java.util.Optional;

/**
 * Represents an future type descriptor.
 *
 * @since 2.0.0
 */
public class BallerinaFutureTypeDescriptor extends AbstractTypeDescriptor implements FutureTypeDescriptor {

    private BallerinaTypeDescriptor typeParameter;

    public BallerinaFutureTypeDescriptor(ModuleID moduleID, BFutureType futureType) {
        super(TypeDescKind.FUTURE, moduleID, futureType);
    }

    public BallerinaFutureTypeDescriptor(ModuleID moduleID, BallerinaTypeDescriptor typeParameter,
                                         BFutureType futureType) {
        super(TypeDescKind.FUTURE, moduleID, futureType);
        this.typeParameter = typeParameter;
    }

    @Override
    public Optional<BallerinaTypeDescriptor> typeParameter() {
        return Optional.ofNullable(this.typeParameter);
    }

    @Override
    public String signature() {
        String memberSignature;
        if (typeParameter().isPresent()) {
            memberSignature = typeParameter().get().signature();
        } else {
            memberSignature = "()";
        }
        return "future<" + memberSignature + ">";
    }
}