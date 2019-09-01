/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.stdlib.internal.builtin;

import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.util.exceptions.BallerinaException;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Extern function ballerina.model.strings:matches.
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "internal",
        functionName = "matches",
        args = {@Argument(name = "src", type = TypeKind.STRING),
                @Argument(name = "regex", type = TypeKind.STRING)},
        returnType = {@ReturnType(type = TypeKind.BOOLEAN)},
        isPublic = true
)
public class Matches {

    public static boolean matches(Strand strand, String value, String regex) {

        StringUtils.checkForNull(value, regex);
        try {
            Pattern pattern = AbstractRegexFunction.validatePattern(regex);
            Matcher matcher = pattern.matcher(value);
            return matcher.matches();
        } catch (PatternSyntaxException e) {
            throw new BallerinaException("{ballerina/internal}StringOperationError", e.getMessage());
        }
    }
}
