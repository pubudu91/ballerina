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

package org.ballerinalang.messaging.kafka.nativeimpl.consumer;

import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.PartitionInfo;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.types.BArrayType;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.ArrayValueImpl;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.messaging.kafka.utils.KafkaConstants;
import org.ballerinalang.messaging.kafka.utils.KafkaUtils;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import java.util.Properties;

import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.ALIAS_DURATION;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.CONSUMER_ERROR;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.DURATION_UNDEFINED_VALUE;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.KAFKA_PACKAGE_NAME;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.KAFKA_PROTOCOL_PACKAGE;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.NATIVE_CONSUMER;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.NATIVE_CONSUMER_CONFIG;
import static org.ballerinalang.messaging.kafka.utils.KafkaUtils.getDefaultApiTimeout;
import static org.ballerinalang.messaging.kafka.utils.KafkaUtils.getIntFromLong;
import static org.ballerinalang.messaging.kafka.utils.KafkaUtils.getTopicPartitionRecord;
import static org.ballerinalang.messaging.kafka.utils.KafkaUtils.populateTopicPartitionRecord;

/**
 * Native function returns partition array for given topic.
 */
@BallerinaFunction(
        orgName = KafkaConstants.ORG_NAME,
        packageName = KAFKA_PACKAGE_NAME,
        functionName = "getTopicPartitions",
        receiver = @Receiver(
                type = TypeKind.OBJECT,
                structType = KafkaConstants.CONSUMER_STRUCT_NAME,
                structPackage = KAFKA_PROTOCOL_PACKAGE
        ),
        isPublic = true
)

public class GetTopicPartitions {

    private static final Logger logger = LoggerFactory.getLogger(GetTopicPartitions.class);

    public static Object getTopicPartitions(Strand strand, ObjectValue consumerObject, String topic, long duration) {
        KafkaConsumer<byte[], byte[]> kafkaConsumer = (KafkaConsumer) consumerObject.getNativeData(NATIVE_CONSUMER);
        Properties consumerProperties = (Properties) consumerObject.getNativeData(NATIVE_CONSUMER_CONFIG);

        int defaultApiTimeout = getDefaultApiTimeout(consumerProperties);
        int apiTimeout = getIntFromLong(duration, logger, ALIAS_DURATION);

        try {
            List<PartitionInfo> partitionInfoList;
            if (apiTimeout > DURATION_UNDEFINED_VALUE) {
                partitionInfoList = getPartitionInfoList(kafkaConsumer, topic, apiTimeout);
            } else if (defaultApiTimeout > DURATION_UNDEFINED_VALUE) {
                partitionInfoList = getPartitionInfoList(kafkaConsumer, topic, defaultApiTimeout);
            } else {
                partitionInfoList = kafkaConsumer.partitionsFor(topic);
            }
            ArrayValue topicPartitionArray = new ArrayValueImpl(new BArrayType(getTopicPartitionRecord().getType()));
//            if (!partitionInfoList.isEmpty()) {
//                partitionInfoList.forEach(info -> {
//                    MapValue<String, Object> partition = populateTopicPartitionRecord(info.topic(), info.partition());
//                    topicPartitionArray.append(partition);
//                });
//            }

            // TODO: Use the above commented code instead of the for loop once #17075 fixed.
            int i = 0;
            for (PartitionInfo info : partitionInfoList) {
                MapValue<String, Object> partition = populateTopicPartitionRecord(info.topic(), info.partition());
                topicPartitionArray.add(i++, partition);
            }
            return topicPartitionArray;
        } catch (KafkaException e) {
            return KafkaUtils.createKafkaError("Failed to retrieve topic partitions for the consumer: "
                    + e.getMessage(), CONSUMER_ERROR);
        }
    }

    private static List<PartitionInfo> getPartitionInfoList(KafkaConsumer<byte[], byte[]> kafkaConsumer, String topic,
                                                            long timeout) {
        Duration duration = Duration.ofMillis(timeout);
        return kafkaConsumer.partitionsFor(topic, duration);
    }
}
