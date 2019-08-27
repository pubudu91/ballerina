/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.test.service.grpc.tool;

import com.google.protobuf.DescriptorProtos;
import org.ballerinalang.protobuf.cmd.OSDetector;
import org.ballerinalang.protobuf.exception.CodeGeneratorException;
import org.ballerinalang.protobuf.utils.ProtocCommandBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.ballerinalang.protobuf.BalGenerationConstants.PROTOC_PLUGIN_EXE_PREFIX;
import static org.ballerinalang.protobuf.BalGenerationConstants.PROTOC_PLUGIN_EXE_URL_SUFFIX;
import static org.ballerinalang.protobuf.utils.BalFileGenerationUtils.downloadFile;
import static org.ballerinalang.protobuf.utils.BalFileGenerationUtils.generateDescriptor;
import static org.ballerinalang.protobuf.utils.BalFileGenerationUtils.grantPermission;
import static org.ballerinalang.protobuf.utils.BalFileGenerationUtils.resolveProtoFolderPath;

/**
 * Test util class contains util functions to retrieve proto descriptor.
 *
 * @since 0.982.0
 */
public class ProtoDescriptorUtils {

    /**
     * Download the protoc executor.
     *
     * @return protoc compiler file.
     */
    static File getProtocCompiler() throws IOException, CodeGeneratorException {
        File protocExeFile = new File(System.getProperty("java.io.tmpdir"), "protoc-" + OSDetector
                .getDetectedClassifier() + ".exe");
        String protocExePath = protocExeFile.getAbsolutePath(); // if file already exists will do nothing
        String protocVersion = "3.9.1";
        if (!protocExeFile.exists()) {
            String protocDownloadurl = PROTOC_PLUGIN_EXE_URL_SUFFIX + protocVersion + "/protoc-" + protocVersion
                    + "-" + OSDetector.getDetectedClassifier() + PROTOC_PLUGIN_EXE_PREFIX;
            try {
                downloadFile(new URL(protocDownloadurl), protocExeFile);
                //set application user permissions to 455
                grantPermission(protocExeFile);
            } catch (CodeGeneratorException e) {
                Files.deleteIfExists(Paths.get(protocExePath));
                throw e;
            }
        } else {
            grantPermission(protocExeFile);
        }
        return protocExeFile;
    }

    /**
     * Generate proto file and convert it to byte array.
     *
     * @param exePath        protoc executor path
     * @param protoPath      .proto file path
     * @return file descriptor of proto file.
     */
    static DescriptorProtos.FileDescriptorSet getProtoFileDescriptor(File exePath, String protoPath) throws
            IOException, CodeGeneratorException {

        File descriptorFile = File.createTempFile("file-desc-", ".desc");
        String command = new ProtocCommandBuilder
                (exePath.getAbsolutePath(), protoPath, resolveProtoFolderPath(protoPath), descriptorFile
                        .getAbsolutePath()).build();
        generateDescriptor(command);
        try (InputStream targetStream = new FileInputStream(descriptorFile)) {
            return DescriptorProtos.FileDescriptorSet.parseFrom(targetStream);
        } catch (IOException e) {
            throw new CodeGeneratorException("Error reading generated descriptor file.", e);
        } finally {
            Files.deleteIfExists(descriptorFile.toPath());
        }
    }
}
