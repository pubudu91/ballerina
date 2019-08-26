/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.stdlib.file.nativeimpl;

import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.stdlib.file.utils.FileConstants;
import org.ballerinalang.stdlib.file.utils.FileUtils;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Extern function ballerina.file:rename.
 *
 * @since 0.995.0
 */
@BallerinaFunction(
        orgName = FileConstants.ORG_NAME,
        packageName = FileConstants.PACKAGE_NAME,
        functionName = "rename",
        isPublic = true
)
public class Rename {

    public static Object rename(Strand strand, String oldPath, String newPath) {
        Path oldFilePath = Paths.get(oldPath);
        Path newFilePath = Paths.get(newPath);

        if (Files.notExists(oldFilePath)) {
            return FileUtils.getBallerinaError(FileConstants.FILE_NOT_FOUND_ERROR,
                    "File not found: " + oldFilePath.toAbsolutePath());
        }

        try {
            Files.move(oldFilePath.toAbsolutePath(), newFilePath.toAbsolutePath());
            return null;
        } catch (FileAlreadyExistsException e) {
            return FileUtils.getBallerinaError(FileConstants.INVALID_OPERATION_ERROR,
                    "File already exists in the new path " + newFilePath);
        } catch (IOException e) {
            return FileUtils.getBallerinaError(FileConstants.FILE_SYSTEM_ERROR, e);
        } catch (SecurityException e) {
            return FileUtils.getBallerinaError(FileConstants.PERMISSION_ERROR, e);
        }
    }
}
