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
package org.ballerinalang.openapi.cmd;

import org.testng.Assert;
import org.testng.annotations.Test;
import picocli.CommandLine;

import java.io.IOException;

/**
 * OpenAPI command test suit.
 */
public class OpenAPICmdTest extends OpenAPICommandTest {

    @Test(description = "Test openapi command with help flag")
    public void testOpenAPICmdHelp() throws IOException {
        String[] args = {"-h"};
        OpenApiCmd openApiCommand = new OpenApiCmd(printStream);
        new CommandLine(openApiCommand).parseArgs(args);
        openApiCommand.execute();

        String output = readOutput(true);
        Assert.assertTrue(output.contains("NAME\n       The Ballerina OpenAPI Tool"));
    }

    @Test(description = "Test openapi command without help flag")
    public void testOpenAPICmdHelpWithoutFlag() throws IOException {
        OpenApiCmd openApiCommand = new OpenApiCmd(printStream);
        new CommandLine(openApiCommand);
        openApiCommand.execute();

        String output = readOutput(true);
        Assert.assertTrue(output.contains("NAME\n       The Ballerina OpenAPI Tool"));
    }
}
