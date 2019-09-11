/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.ballerinalang.stdlib.jsonutils;

import org.ballerinalang.jvm.JSONUtils;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.values.TableValue;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.ReturnType;

/**
 * Converts a given table to its JSON representation.
 *
 * @since 1.0.0
 */
@BallerinaFunction(
        orgName = "ballerina",
        packageName = "jsonutils",
        functionName = "fromTable",
        args = {
                @Argument(name = "tbl", type = TypeKind.TABLE)
        },
        returnType = {@ReturnType(type = TypeKind.JSON)},
        isPublic = true
)
public class FromTable {
    public static Object fromTable(Strand strand, TableValue tableValue) {
        return JSONUtils.toJSON(tableValue);
    }
}
