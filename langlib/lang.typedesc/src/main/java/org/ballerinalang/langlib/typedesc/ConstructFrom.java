/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.langlib.typedesc;

import org.ballerinalang.jvm.TypeChecker;
import org.ballerinalang.jvm.TypeConverter;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.types.BTypedescType;
import org.ballerinalang.jvm.types.TypeTags;
import org.ballerinalang.jvm.util.exceptions.BLangExceptionHelper;
import org.ballerinalang.jvm.util.exceptions.BallerinaException;
import org.ballerinalang.jvm.util.exceptions.RuntimeErrors;
import org.ballerinalang.jvm.values.ErrorValue;
import org.ballerinalang.jvm.values.RefValue;
import org.ballerinalang.jvm.values.TypedescValue;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.ballerinalang.jvm.BallerinaErrors.createError;
import static org.ballerinalang.jvm.TypeConverter.getConvertibleTypes;
import static org.ballerinalang.jvm.util.exceptions.BallerinaErrorReasons.CONSTRUCT_FROM_CONVERSION_ERROR;
import static org.ballerinalang.jvm.util.exceptions.RuntimeErrors.INCOMPATIBLE_CONVERT_OPERATION;

/**
 * Extern function lang.typedesc:constructFrom.
 *
 * @since 1.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "lang.typedesc", functionName = "constructFrom",
        args = {
                @Argument(name = "t", type = TypeKind.TYPEDESC),
                @Argument(name = "v", type = TypeKind.ANYDATA)
        },
        returnType = {
                @ReturnType(type = TypeKind.ANYDATA),
                @ReturnType(type = TypeKind.ERROR)
        },
        isPublic = true
)
public class ConstructFrom {

    private static final String AMBIGUOUS_TARGET = "ambiguous target type";

    public static Object constructFrom(Strand strand, TypedescValue t, Object v) {
        BType describingType = t.getDescribingType();
        // typedesc<json>.constructFrom like usage
        if (describingType.getTag() == TypeTags.TYPEDESC_TAG) {
            return convert(((BTypedescType) t.getDescribingType()).getConstraint(), v);
        }
        // json.constructFrom like usage
        return convert(describingType, v);
    }

    public static Object convert(BType convertType, Object inputValue) {
        if (inputValue == null) {
            if (convertType.isNilable()) {
                return null;
            }
            return createError(CONSTRUCT_FROM_CONVERSION_ERROR,
                               BLangExceptionHelper.getErrorMessage(RuntimeErrors.CANNOT_CONVERT_NIL, convertType));
        }

        BType inputValType = TypeChecker.getType(inputValue);

        List<BType> convertibleTypes = getConvertibleTypes(inputValue, convertType);

        if (convertibleTypes.size() == 0) {
            return createConversionError(inputValue, convertType);
        } else if (convertibleTypes.size() > 1) {
            return createConversionError(inputValue, convertType, AMBIGUOUS_TARGET);
        }

        BType targetType = convertibleTypes.get(0);
        if (inputValType.getTag() < TypeTags.JSON_TAG) {
            // If input value is a value-type, perform a numeric conversion if required.
            if (!TypeChecker.checkIsType(inputValue, convertType)) {
                return TypeConverter.convertValues(targetType, inputValue);
            }

            return inputValue;
        }

        try {
            RefValue refValue = (RefValue) inputValue;

            if (targetType.equals(inputValType)) {
                return refValue.copy(new HashMap<>());
            }

            RefValue convertedValue = (RefValue) refValue.copy(new HashMap<>());
            convertedValue.stamp(targetType, new ArrayList<>());
            return convertedValue;
        } catch (BallerinaException e) {
            return createError(CONSTRUCT_FROM_CONVERSION_ERROR, e.getDetail());
        }
    }

    private static ErrorValue createConversionError(Object inputValue, BType targetType) {
        return createError(CONSTRUCT_FROM_CONVERSION_ERROR,
                           BLangExceptionHelper.getErrorMessage(INCOMPATIBLE_CONVERT_OPERATION,
                                                                TypeChecker.getType(inputValue), targetType));
    }

    private static ErrorValue createConversionError(Object inputValue, BType targetType, String detailMessage) {
        return createError(CONSTRUCT_FROM_CONVERSION_ERROR,
                           BLangExceptionHelper.getErrorMessage(INCOMPATIBLE_CONVERT_OPERATION,
                                                                TypeChecker.getType(inputValue), targetType)
                                   .concat(": ".concat(detailMessage)));
    }
}
