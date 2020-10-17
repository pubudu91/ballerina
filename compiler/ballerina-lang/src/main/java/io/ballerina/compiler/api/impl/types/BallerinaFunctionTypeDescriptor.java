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
import io.ballerina.compiler.api.impl.SymbolFactory;
import io.ballerina.compiler.api.impl.TypesFactory;
import io.ballerina.compiler.api.types.BallerinaTypeDescriptor;
import io.ballerina.compiler.api.types.FunctionTypeDescriptor;
import io.ballerina.compiler.api.types.Parameter;
import io.ballerina.compiler.api.types.TypeDescKind;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableTypeSymbol;

import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;

/**
 * Represents a function type descriptor.
 *
 * @since 2.0.0
 */
public class BallerinaFunctionTypeDescriptor extends AbstractTypeDescriptor implements FunctionTypeDescriptor {

    private List<Parameter> requiredParams;
    private List<Parameter> defaultableParams;
    private Parameter restParam;
    private BallerinaTypeDescriptor returnType;
    // TODO: Represent the return type's annotations

    public BallerinaFunctionTypeDescriptor(ModuleID moduleID, BInvokableTypeSymbol invokableSymbol) {
        super(TypeDescKind.FUNCTION, moduleID, invokableSymbol.type);
    }

    public BallerinaFunctionTypeDescriptor(ModuleID moduleID, List<Parameter> requiredParams,
                                           List<Parameter> defaultableParams, Parameter restParam,
                                           BallerinaTypeDescriptor returnType, BInvokableTypeSymbol invokableSymbol) {
        super(TypeDescKind.FUNCTION, moduleID, invokableSymbol.type);
        this.requiredParams = requiredParams;
        this.defaultableParams = defaultableParams;
        this.restParam = restParam;
        this.returnType = returnType;
    }

    @Override
    public List<Parameter> requiredParams() {
        return this.requiredParams;
    }

    @Override
    public List<Parameter> defaultableParams() {
        return this.defaultableParams;
    }

    @Override
    public Optional<Parameter> restParam() {
        return Optional.ofNullable(this.restParam);
    }

    @Override
    public Optional<BallerinaTypeDescriptor> returnTypeDescriptor() {
        return Optional.ofNullable(this.returnType);
    }

    @Override
    public String signature() {
        StringBuilder signature = new StringBuilder("function (");
        StringJoiner joiner = new StringJoiner(",");
        for (Parameter requiredParam : this.requiredParams()) {
            String ballerinaParameterSignature = requiredParam.signature();
            joiner.add(ballerinaParameterSignature);
        }
        this.restParam().ifPresent(ballerinaParameter -> joiner.add(ballerinaParameter.signature()));
        signature.append(joiner.toString()).append(")");
        this.returnTypeDescriptor().ifPresent(typeDescriptor -> signature.append(" returns ")
                .append(typeDescriptor.signature()));
        return signature.toString();
    }
}
