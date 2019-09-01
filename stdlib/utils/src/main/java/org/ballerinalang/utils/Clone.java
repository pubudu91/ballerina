/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p>
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 **/

package org.ballerinalang.utils;

import org.ballerinalang.jvm.BallerinaErrors;
import org.ballerinalang.jvm.TypeChecker;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.util.exceptions.BLangExceptionHelper;
import org.ballerinalang.jvm.util.exceptions.RuntimeErrors;
import org.ballerinalang.jvm.values.RefValue;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;
import org.wso2.ballerinalang.compiler.util.TypeTags;

import java.util.HashMap;

import static org.ballerinalang.jvm.util.exceptions.BallerinaErrorReasons.BALLERINA_PREFIXED_CLONE_ERROR;

/**
 * Performs a deep copy, recursively copying all structural values and their members.
 *
 * @since 0.990.4
 */
@BallerinaFunction(
        orgName = "ballerina",
        packageName = "utils",
        functionName = "clone",
        args = {@Argument(name = "value", type = TypeKind.ANY)},
        returnType = { @ReturnType(type = TypeKind.ANYDATA), @ReturnType(type = TypeKind.ERROR) }
)
public class Clone {

    public static Object clone(Strand strand, Object value) {
        
        if (value == null) {
            return null;
        }
        if (!(value instanceof RefValue)) {
            return value;
        }
        RefValue refValue = (RefValue) value;
        if (refValue.getType().getTag() == TypeTags.ERROR || !TypeChecker.checkIsLikeType(refValue, org
                .ballerinalang.jvm.types.BTypes.typePureType)) {
            return BallerinaErrors.createError(BALLERINA_PREFIXED_CLONE_ERROR, BLangExceptionHelper.getErrorMessage(
                    RuntimeErrors.UNSUPPORTED_CLONE_OPERATION, refValue.getType()));
        }
        return refValue.copy(new HashMap<>());
    }
}
