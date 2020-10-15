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

package io.ballerina.compiler.api.impl;

import io.ballerina.compiler.api.ModuleID;
import io.ballerina.compiler.api.impl.types.BallerinaArrayTypeDescriptor;
import io.ballerina.compiler.api.impl.types.BallerinaErrorTypeDescriptor;
import io.ballerina.compiler.api.impl.types.BallerinaFieldDescriptor;
import io.ballerina.compiler.api.impl.types.BallerinaFunctionTypeDescriptor;
import io.ballerina.compiler.api.impl.types.BallerinaFutureTypeDescriptor;
import io.ballerina.compiler.api.impl.types.BallerinaMapTypeDescriptor;
import io.ballerina.compiler.api.impl.types.BallerinaObjectTypeDescriptor;
import io.ballerina.compiler.api.impl.types.BallerinaParameter;
import io.ballerina.compiler.api.impl.types.BallerinaRecordTypeDescriptor;
import io.ballerina.compiler.api.impl.types.BallerinaSimpleTypeDescriptor;
import io.ballerina.compiler.api.impl.types.BallerinaStreamTypeDescriptor;
import io.ballerina.compiler.api.impl.types.BallerinaTupleTypeDescriptor;
import io.ballerina.compiler.api.impl.types.BallerinaTypeDescTypeDescriptor;
import io.ballerina.compiler.api.impl.types.BallerinaTypeReferenceTypeDescriptor;
import io.ballerina.compiler.api.impl.types.BallerinaUnionTypeDescriptor;
import io.ballerina.compiler.api.impl.types.util.BallerinaMethodDeclaration;
import io.ballerina.compiler.api.symbols.Qualifier;
import io.ballerina.compiler.api.types.BallerinaTypeDescriptor;
import io.ballerina.compiler.api.types.FieldDescriptor;
import io.ballerina.compiler.api.types.Parameter;
import io.ballerina.compiler.api.types.ParameterKind;
import io.ballerina.compiler.api.types.util.MethodDeclaration;
import org.ballerinalang.model.types.TypeKind;
import org.wso2.ballerinalang.compiler.semantics.analyzer.Types;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAttachedFunction;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BObjectTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.semantics.model.types.BAnyType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BAnydataType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BBuiltInRefType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BErrorType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BField;
import org.wso2.ballerinalang.compiler.semantics.model.types.BFiniteType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BFutureType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BIntersectionType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BJSONType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BMapType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BObjectType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BParameterizedType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BRecordType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BServiceType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BStreamType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTupleType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTypeVisitor;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTypedescType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BXMLType;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.util.Flags;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.ballerina.compiler.api.types.ParameterKind.DEFAULTABLE;
import static io.ballerina.compiler.api.types.ParameterKind.REQUIRED;
import static io.ballerina.compiler.api.types.ParameterKind.REST;
import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toList;
import static org.ballerinalang.model.types.TypeKind.OBJECT;
import static org.ballerinalang.model.types.TypeKind.RECORD;

/**
 * A type visitor to build the public type for a given type.
 *
 * @since 2.0.0
 */
public class TypeBuilder implements BTypeVisitor<BType, BallerinaTypeDescriptor> {

    private ModuleID moduleID;
    private final Types types;

    public TypeBuilder(Types types) {
        this.types = types;
    }

    public BallerinaTypeDescriptor build(BType internalType) {
        if (internalType == null || internalType.tag == TypeTags.NONE) {
            return null;
        }

        moduleID = internalType.tsymbol == null ? null : new BallerinaModuleID(internalType.tsymbol.pkgID);
        BallerinaTypeDescriptor publicType = internalType.accept(this, null);

        if (isTypeReference(internalType)) {
            return new BallerinaTypeReferenceTypeDescriptor(moduleID, publicType, internalType,
                                                            internalType.tsymbol.getName().getValue());
        }

        return publicType;
    }

    @Override
    public BallerinaTypeDescriptor visit(BType internalType, BType s) {
        if (types.isSimpleBasicType(internalType.tag)) {
            return new BallerinaSimpleTypeDescriptor(moduleID, internalType);
        }

        return null;
    }

    @Override
    public BallerinaTypeDescriptor visit(BBuiltInRefType internalType, BType s) {
        return null;
    }

    @Override
    public BallerinaTypeDescriptor visit(BAnyType internalType, BType s) {
        return null;
    }

    @Override
    public BallerinaTypeDescriptor visit(BAnydataType internalType, BType s) {
        return null;
    }

    @Override
    public BallerinaTypeDescriptor visit(BMapType internalType, BType s) {
        BallerinaTypeDescriptor memberType = build(internalType.constraint);
        return new BallerinaMapTypeDescriptor(moduleID, memberType, internalType);
    }

    @Override
    public BallerinaTypeDescriptor visit(BXMLType internalType, BType s) {
        return null;
    }

    @Override
    public BallerinaTypeDescriptor visit(BJSONType internalType, BType s) {
        return null;
    }

    @Override
    public BallerinaTypeDescriptor visit(BArrayType internalType, BType s) {
        BallerinaTypeDescriptor memberType = build(internalType.eType);
        return new BallerinaArrayTypeDescriptor(moduleID, memberType, internalType);
    }

    @Override
    public BallerinaTypeDescriptor visit(BObjectType internalType, BType s) {
        List<FieldDescriptor> fields = new ArrayList<>();

        for (Map.Entry<String, BField> entry : internalType.fields.entrySet()) {
            BField bField = entry.getValue();
            BallerinaTypeDescriptor fieldType = build(bField.type);
            BallerinaFieldDescriptor field = new BallerinaFieldDescriptor(fieldType, bField);
            fields.add(field);
        }

        BObjectTypeSymbol typeSymbol = (BObjectTypeSymbol) internalType.tsymbol;
        List<MethodDeclaration> methods = new ArrayList<>();

        for (BAttachedFunction func : typeSymbol.attachedFuncs) {
            BallerinaFunctionTypeDescriptor methodType = (BallerinaFunctionTypeDescriptor) build(func.type);
            BallerinaMethodDeclaration methodDecl = new BallerinaMethodDeclaration(func.funcName.value, new HashSet<>(),
                                                                                   methodType);
            methods.add(methodDecl);
        }

        return new BallerinaObjectTypeDescriptor(moduleID, unmodifiableList(fields), unmodifiableList(methods),
                                                 internalType);
    }

    @Override
    public BallerinaTypeDescriptor visit(BRecordType internalType, BType s) {
        List<FieldDescriptor> fields = new ArrayList<>();

        for (Map.Entry<String, BField> entry : internalType.fields.entrySet()) {
            BField bField = entry.getValue();
            BallerinaTypeDescriptor fieldType = build(bField.type);
            BallerinaFieldDescriptor field = new BallerinaFieldDescriptor(fieldType, bField);
            fields.add(field);
        }

        BallerinaTypeDescriptor restType =
                internalType.restFieldType.tag == TypeTags.NONE ? null : build(internalType.restFieldType);

        return new BallerinaRecordTypeDescriptor(moduleID, unmodifiableList(fields), restType, internalType);
    }

    @Override
    public BallerinaTypeDescriptor visit(BTupleType internalType, BType s) {
        List<BallerinaTypeDescriptor> memberTypes = new ArrayList<>();

        for (BType memberType : internalType.tupleTypes) {
            BallerinaTypeDescriptor type = build(memberType);
            memberTypes.add(type);
        }

        BallerinaTypeDescriptor restType = build(internalType.restType);

        return new BallerinaTupleTypeDescriptor(moduleID, unmodifiableList(memberTypes), restType, internalType);
    }

    @Override
    public BallerinaTypeDescriptor visit(BStreamType internalType, BType s) {
        BallerinaTypeDescriptor typeParameter = build(internalType.constraint);
        return new BallerinaStreamTypeDescriptor(moduleID, Collections.singletonList(typeParameter), internalType);
    }

    @Override
    public BallerinaTypeDescriptor visit(BTableType internalType, BType s) {
        return null;
    }

    @Override
    public BallerinaTypeDescriptor visit(BInvokableType internalType, BType s) {
        BInvokableTypeSymbol typeSymbol = (BInvokableTypeSymbol) internalType.tsymbol;

        List<Parameter> requiredParams = typeSymbol.params.stream()
                .filter(param -> !param.defaultableParam)
                .map(symbol -> createBallerinaParameter(symbol, REQUIRED))
                .collect(Collectors.collectingAndThen(toList(), Collections::unmodifiableList));

        List<Parameter> defaultableParams = typeSymbol.params.stream()
                .filter(param -> param.defaultableParam)
                .map(symbol -> createBallerinaParameter(symbol, DEFAULTABLE))
                .collect(Collectors.collectingAndThen(toList(), Collections::unmodifiableList));

        Parameter restParam = createBallerinaParameter(typeSymbol.restParam, REST);
        BallerinaTypeDescriptor returnType = build(internalType.retType);

        return new BallerinaFunctionTypeDescriptor(moduleID, requiredParams, defaultableParams, restParam, returnType,
                                                   typeSymbol);
    }

    @Override
    public BallerinaTypeDescriptor visit(BUnionType internalType, BType s) {
        List<BallerinaTypeDescriptor> members = new ArrayList<>();

        for (BType member : internalType.getMemberTypes()) {
            BallerinaTypeDescriptor type = build(member);
            members.add(type);
        }

        return new BallerinaUnionTypeDescriptor(moduleID, unmodifiableList(members), internalType);
    }

    @Override
    public BallerinaTypeDescriptor visit(BIntersectionType internalType, BType s) {
        return null;
    }

    @Override
    public BallerinaTypeDescriptor visit(BErrorType internalType, BType s) {
        BallerinaTypeDescriptor detailType = build(internalType.detailType);
        return new BallerinaErrorTypeDescriptor(moduleID, detailType, internalType);
    }

    @Override
    public BallerinaTypeDescriptor visit(BFutureType internalType, BType s) {
        BallerinaTypeDescriptor typeParameter = build(internalType.constraint);
        return new BallerinaFutureTypeDescriptor(moduleID, typeParameter, internalType);
    }

    @Override
    public BallerinaTypeDescriptor visit(BFiniteType internalType, BType s) {
        return null;
    }

    @Override
    public BallerinaTypeDescriptor visit(BServiceType internalType, BType s) {
        return null;
    }

    @Override
    public BallerinaTypeDescriptor visit(BTypedescType internalType, BType s) {
        BallerinaTypeDescriptor typeParameter = build(internalType.constraint);
        return new BallerinaTypeDescTypeDescriptor(moduleID, typeParameter, internalType);
    }

    @Override
    public BallerinaTypeDescriptor visit(BParameterizedType internalType, BType s) {
        return null;
    }

    private Parameter createBallerinaParameter(BVarSymbol symbol, ParameterKind kind) {
        if (symbol == null) {
            return null;
        }

        String name = symbol.getName().getValue();
        BallerinaTypeDescriptor typeDescriptor = build(symbol.type);

        List<Qualifier> qualifiers = new ArrayList<>();
        if ((symbol.flags & Flags.PUBLIC) == Flags.PUBLIC) {
            qualifiers.add(Qualifier.PUBLIC);
        }

        return new BallerinaParameter(name, typeDescriptor, unmodifiableList(qualifiers), kind);
    }

    private static boolean isTypeReference(BType bType) {
        if (bType.tsymbol == null || Symbols.isFlagOn(bType.tsymbol.flags, Flags.ANONYMOUS)) {
            return false;
        }

        TypeKind kind = bType.getKind();
        return kind == RECORD || kind == OBJECT || bType.tsymbol.isLabel;
    }
}
