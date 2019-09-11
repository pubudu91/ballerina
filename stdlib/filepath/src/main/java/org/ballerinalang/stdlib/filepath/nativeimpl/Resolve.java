/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.stdlib.filepath.nativeimpl;

import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.stdlib.filepath.Constants;
import org.ballerinalang.stdlib.filepath.Utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.NotLinkException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * The native class to get real value of the path after evaluating any symbolic links.
 *
 * @since 0.995.0
 */
@BallerinaFunction(
        orgName = Constants.ORG_NAME,
        packageName = Constants.PACKAGE_NAME,
        functionName = "resolve",
        isPublic = true
)
public class Resolve {

    public static Object resolve(Strand strand, String inputPath) {
        try {
            Path realPath = Files.readSymbolicLink(Paths.get(inputPath).toAbsolutePath());
            return realPath.toString();
        } catch (NotLinkException ex) {
            return Utils.getPathError(Constants.NOT_LINK_ERROR, "Path is not a symbolic link " + inputPath);
        } catch (NoSuchFileException ex) {
            return Utils.getPathError(Constants.FILE_NOT_FOUND_ERROR, "File does not exist at " + inputPath);
        } catch (IOException ex) {
            return Utils.getPathError(Constants.IO_ERROR, "IO error for " + inputPath);
        } catch (SecurityException ex) {
            return Utils.getPathError(Constants.SECURITY_ERROR, "Security error for " + inputPath);
        }
    }

}
