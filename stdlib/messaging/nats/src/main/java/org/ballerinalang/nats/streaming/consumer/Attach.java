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
package org.ballerinalang.nats.streaming.consumer;

import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.ballerinalang.nats.Constants;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static org.ballerinalang.nats.Constants.STREAMING_DISPATCHER_LIST;

/**
 * Create a listener and attach service.
 *
 * @since 1.0.0
 */
@BallerinaFunction(orgName = Constants.ORG_NAME,
        packageName = Constants.NATS,
        functionName = "attach",
        receiver = @Receiver(type = TypeKind.OBJECT,
                structType = "StreamingListener",
                structPackage = Constants.NATS_PACKAGE),
        isPublic = true)
public class Attach {

    public static void attach(Strand strand, ObjectValue streamingListener, ObjectValue service,
                              Object connection) {
        List<ObjectValue> serviceList = (List<ObjectValue>) ((ObjectValue) connection)
                .getNativeData(Constants.SERVICE_LIST);
        serviceList.add(service);
        ConcurrentHashMap<ObjectValue, StreamingListener> serviceListenerMap =
                (ConcurrentHashMap<ObjectValue, StreamingListener>) streamingListener
                        .getNativeData(STREAMING_DISPATCHER_LIST);
        serviceListenerMap.put(service, new StreamingListener(service, strand.scheduler));
    }
}
