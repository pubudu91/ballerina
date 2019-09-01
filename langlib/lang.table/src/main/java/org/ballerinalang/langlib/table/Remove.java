/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.langlib.table;

import org.ballerinalang.jvm.BallerinaErrors;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.values.FPValue;
import org.ballerinalang.jvm.values.TableValue;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;

/**
 * {@code Remove} is the function to remove data from a table.
 *
 * @since 0.970.0
 */
@BallerinaFunction(orgName = "ballerina", packageName = "lang.table",
                   functionName = "remove",
                   args = {
                           @Argument(name = "dt", type = TypeKind.TABLE),
                           @Argument(name = "func", type = TypeKind.ANY)
                   })
public class Remove {

    public static Object remove(Strand strand, TableValue table, FPValue<Object, Boolean> func) {
        try {
            return table.performRemoveOperation(strand, func);
        } catch (org.ballerinalang.jvm.util.exceptions.BLangFreezeException e) {
            throw BallerinaErrors.createError(e.getMessage(),
                                              "Failed to remove data from the table: " + e.getDetail());
        }
    }
}
