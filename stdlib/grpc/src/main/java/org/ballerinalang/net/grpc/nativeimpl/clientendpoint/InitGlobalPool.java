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

package org.ballerinalang.net.grpc.nativeimpl.clientendpoint;

import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.wso2.transport.http.netty.contractimpl.sender.channel.pool.ConnectionManager;
import org.wso2.transport.http.netty.contractimpl.sender.channel.pool.PoolConfiguration;

import static org.ballerinalang.net.grpc.GrpcConstants.PROTOCOL_STRUCT_PACKAGE_GRPC;
import static org.ballerinalang.net.grpc.GrpcUtil.populatePoolingConfig;
import static org.ballerinalang.net.http.HttpConstants.CONNECTION_MANAGER;

/**
 * Initializes the global pool.
 *
 * @since 0.995.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "grpc",
        functionName = "initGlobalPool",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = "ConnectionManager", structPackage =
                PROTOCOL_STRUCT_PACKAGE_GRPC),
        isPublic = true
)
public class InitGlobalPool {

    public static void initGlobalPool(Strand strand, ObjectValue endpointObject, MapValue<String,
                                      Long> globalPoolConfig) {
        PoolConfiguration globalPool = new PoolConfiguration();
        populatePoolingConfig(globalPoolConfig, globalPool);
        ConnectionManager connectionManager = new ConnectionManager(globalPool);
        globalPoolConfig.addNativeData(CONNECTION_MANAGER, connectionManager);
    }
}
