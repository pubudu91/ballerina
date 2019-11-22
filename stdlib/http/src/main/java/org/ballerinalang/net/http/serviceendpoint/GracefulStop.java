/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.net.http.serviceendpoint;

import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.net.http.HttpConstants;
import org.ballerinalang.net.http.HttpErrorType;
import org.ballerinalang.net.http.HttpUtil;

import static org.ballerinalang.net.http.HttpConstants.HTTP_LISTENER_ENDPOINT;

/**
 * Stop the listener immediately and close the connection.
 *
 * @since 0.966
 */

@BallerinaFunction(
        orgName = "ballerina", packageName = "http",
        functionName = "gracefulStop",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = HTTP_LISTENER_ENDPOINT,
                             structPackage = "ballerina/http"),
        isPublic = true
)
public class GracefulStop extends AbstractHttpNativeFunction {
    public static Object gracefulStop(Strand strand, ObjectValue serverEndpoint) {
        try {
            getServerConnector(serverEndpoint).stop();
            serverEndpoint.addNativeData(HttpConstants.CONNECTOR_STARTED, false);
            resetRegistry(serverEndpoint);
        } catch (Exception ex) {
            return HttpUtil.createHttpError(ex.getMessage(), HttpErrorType.GENERIC_LISTENER_ERROR);
        }
        return null;
    }
}
