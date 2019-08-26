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
 *
 */
package org.ballerinalang.compiler.backend.jvm;

import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.util.exceptions.BallerinaException;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.ballerinalang.model.types.TypeKind.STRING;

/**
 * @since 0.995.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "compiler_backend_jvm",
        functionName = "readFileFully",
        args = {
                @Argument(name = "path", type = STRING),
        }
)
public class ReadFileFully {


    public static ArrayValue readFileFully(Strand strand, String path) {
        try {
            return new ArrayValue(Files.readAllBytes(Paths.get(path)));
        } catch (IOException e) {
            throw new BallerinaException(e);
        }
    }

}
