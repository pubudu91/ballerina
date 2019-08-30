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

package org.ballerinalang.messaging.kafka.utils;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.config.SslConfigs;
import org.ballerinalang.jvm.BallerinaErrors;
import org.ballerinalang.jvm.BallerinaValues;
import org.ballerinalang.jvm.types.BArrayType;
import org.ballerinalang.jvm.types.BTypes;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.ErrorValue;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

import static org.ballerinalang.jvm.BallerinaValues.createRecord;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.ALIAS_CONCURRENT_CONSUMERS;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.ALIAS_DECOUPLE_PROCESSING;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.ALIAS_OFFSET;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.ALIAS_PARTITION;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.ALIAS_POLLING_INTERVAL;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.ALIAS_POLLING_TIMEOUT;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.ALIAS_TOPIC;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.ALIAS_TOPICS;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.CONSUMER_AUTO_COMMIT_INTERVAL_MS_CONFIG;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.CONSUMER_AUTO_OFFSET_RESET_CONFIG;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.CONSUMER_BOOTSTRAP_SERVERS_CONFIG;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.CONSUMER_CHECK_CRCS_CONFIG;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.CONSUMER_CLIENT_ID_CONFIG;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.CONSUMER_CONFIG_FIELD_NAME;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.CONSUMER_CONNECTIONS_MAX_IDLE_MS_CONFIG;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.CONSUMER_DEFAULT_API_TIMEOUT_CONFIG;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.CONSUMER_ENABLE_AUTO_COMMIT_CONFIG;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.CONSUMER_ERROR;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.CONSUMER_EXCLUDE_INTERNAL_TOPICS_CONFIG;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.CONSUMER_FETCH_MAX_BYTES_CONFIG;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.CONSUMER_FETCH_MAX_WAIT_MS_CONFIG;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.CONSUMER_FETCH_MIN_BYTES_CONFIG;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.CONSUMER_GROUP_ID_CONFIG;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.CONSUMER_HEARTBEAT_INTERVAL_MS_CONFIG;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.CONSUMER_INTERCEPTOR_CLASSES_CONFIG;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.CONSUMER_ISOLATION_LEVEL_CONFIG;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.CONSUMER_MAX_PARTITION_FETCH_BYTES_CONFIG;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.CONSUMER_MAX_POLL_INTERVAL_MS_CONFIG;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.CONSUMER_MAX_POLL_RECORDS_CONFIG;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.CONSUMER_METADATA_MAX_AGE_CONFIG;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.CONSUMER_METRICS_NUM_SAMPLES_CONFIG;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.CONSUMER_METRICS_RECORDING_LEVEL_CONFIG;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.CONSUMER_METRICS_SAMPLE_WINDOW_MS_CONFIG;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.CONSUMER_METRIC_REPORTER_CLASSES_CONFIG;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.CONSUMER_PARTITION_ASSIGNMENT_STRATEGY_CONFIG;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.CONSUMER_RECEIVE_BUFFER_CONFIG;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.CONSUMER_RECONNECT_BACKOFF_MAX_MS_CONFIG;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.CONSUMER_RECONNECT_BACKOFF_MS_CONFIG;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.CONSUMER_RECORD_STRUCT_NAME;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.CONSUMER_REQUEST_TIMEOUT_MS_CONFIG;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.CONSUMER_RETRY_BACKOFF_MS_CONFIG;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.CONSUMER_SEND_BUFFER_CONFIG;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.CONSUMER_SESSION_TIMEOUT_MS_CONFIG;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.DEFAULT_KEY_DESERIALIZER;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.DEFAULT_KEY_SERIALIZER;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.DEFAULT_VALUE_DESERIALIZER;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.DEFAULT_VALUE_SERIALIZER;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.DETAIL_RECORD_NAME;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.DURATION_UNDEFINED_VALUE;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.ENABLED_PROTOCOLS_CONFIG;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.KAFKA_PROTOCOL_PACKAGE_ID;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.KEYMANAGER_ALGORITHM_CONFIG;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.KEYSTORE_CONFIG;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.KEYSTORE_TYPE_CONFIG;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.LOCATION_CONFIG;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.NATIVE_PRODUCER;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.NATIVE_PRODUCER_CONFIG;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.OFFSET_STRUCT_NAME;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.PASSWORD_CONFIG;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.PRODUCER_ACKS_CONFIG;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.PRODUCER_BATCH_SIZE_CONFIG;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.PRODUCER_BOOTSTRAP_SERVERS_CONFIG;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.PRODUCER_BUFFER_MEMORY_CONFIG;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.PRODUCER_CLIENT_ID_CONFIG;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.PRODUCER_COMPRESSION_TYPE_CONFIG;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.PRODUCER_CONNECTIONS_MAX_IDLE_MS_CONFIG;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.PRODUCER_ENABLE_IDEMPOTENCE_CONFIG;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.PRODUCER_INTERCEPTOR_CLASSES_CONFIG;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.PRODUCER_LINGER_MS_CONFIG;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.PRODUCER_MAX_BLOCK_MS_CONFIG;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.PRODUCER_MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.PRODUCER_MAX_REQUEST_SIZE_CONFIG;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.PRODUCER_METADATA_MAX_AGE_CONFIG;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.PRODUCER_METRICS_NUM_SAMPLES_CONFIG;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.PRODUCER_METRICS_RECORDING_LEVEL_CONFIG;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.PRODUCER_METRICS_SAMPLE_WINDOW_MS_CONFIG;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.PRODUCER_METRIC_REPORTER_CLASSES_CONFIG;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.PRODUCER_PARTITIONER_CLASS_CONFIG;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.PRODUCER_RECEIVE_BUFFER_CONFIG;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.PRODUCER_RECONNECT_BACKOFF_MAX_MS_CONFIG;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.PRODUCER_RECONNECT_BACKOFF_MS_CONFIG;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.PRODUCER_REQUEST_TIMEOUT_MS_CONFIG;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.PRODUCER_RETRIES_CONFIG;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.PRODUCER_RETRY_BACKOFF_MS_CONFIG;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.PRODUCER_SEND_BUFFER_CONFIG;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.PRODUCER_TRANSACTIONAL_ID_CONFIG;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.PRODUCER_TRANSACTION_TIMEOUT_CONFIG;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.PROPERTIES_ARRAY;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.PROTOCOL_CONFIG;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.SECURE_SOCKET;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.SECURITY_PROTOCOL_CONFIG;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.SSL_CIPHER_SUITES_CONFIG;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.SSL_KEY_PASSWORD_CONFIG;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.SSL_PROTOCOL_CONFIG;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.SSL_PROVIDER_CONFIG;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.SSL_SECURE_RANDOM_IMPLEMENTATION_CONFIG;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.TOPIC_PARTITION_STRUCT_NAME;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.TRUSTMANAGER_ALGORITHM_CONFIG;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.TRUSTSTORE_CONFIG;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.TRUSTSTORE_TYPE_CONFIG;
import static org.ballerinalang.messaging.kafka.utils.KafkaConstants.UNCHECKED;

/**
 * Utility class for Kafka Connector Implementation.
 */
public class KafkaUtils {

    private KafkaUtils() {
    }

    public static Object[] getResourceParameters(ObjectValue service, ObjectValue listener,
                                                 ConsumerRecords<byte[], byte[]> records, String groupId) {

        ArrayValue consumerRecordsArray = new ArrayValue(new BArrayType(getConsumerRecord().getType()));

        if (service.getType().getAttachedFunctions()[0].getParameterType().length == 2) {
//            records.forEach(record -> {
//                MapValue<String, Object> consumerRecord = populateConsumerRecord(record);
//                consumerRecordsArray.append(consumerRecord);
//            });
            // TODO: Use the above commented code instead of the for loop once #17075 fixed.
            int i = 0;
            for (ConsumerRecord<byte[], byte[]> record : records) {
                MapValue<String, Object> consumerRecord = populateConsumerRecord(record);
                consumerRecordsArray.add(i++, consumerRecord);
            }
            return new Object[]{listener, true, consumerRecordsArray, true, null, false, null, false};
        } else {
            ArrayValue partitionOffsetsArray = new ArrayValue(new BArrayType(getPartitionOffsetRecord().getType()));
//            records.forEach(record -> {
//                MapValue<String, Object> consumerRecord = populateConsumerRecord(record);
//                MapValue<String, Object> topicPartition = populateTopicPartitionRecord(record.topic(),
//                        record.partition());
//                MapValue<String, Object> partitionOffset = populatePartitionOffsetRecord(topicPartition,
//                        record.offset());
//
//                consumerRecordsArray.append(consumerRecord);
//                partitionOffsetsArray.append(partitionOffset);
//            });
            // TODO: Use the above commented code instead of the for loop once #17075 fixed.
            int i = 0;
            for (ConsumerRecord<byte[], byte[]> record : records) {
                MapValue<String, Object> consumerRecord = populateConsumerRecord(record);
                MapValue<String, Object> topicPartition = populateTopicPartitionRecord(record.topic(),
                        record.partition());
                MapValue<String, Object> partitionOffset = populatePartitionOffsetRecord(topicPartition,
                        record.offset());
                consumerRecordsArray.add(i, consumerRecord);
                partitionOffsetsArray.add(i, partitionOffset);
                i++;
            }
            return new Object[]{listener, true, consumerRecordsArray, true, partitionOffsetsArray, true, groupId, true};
        }
    }

    public static Properties processKafkaConsumerConfig(MapValue<String, Object> configurations) {
        Properties properties = new Properties();

        addStringParamIfPresent(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, configurations, properties,
                CONSUMER_BOOTSTRAP_SERVERS_CONFIG);
        addStringParamIfPresent(ConsumerConfig.GROUP_ID_CONFIG, configurations, properties,
                CONSUMER_GROUP_ID_CONFIG);
        addStringParamIfPresent(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, configurations, properties,
                CONSUMER_AUTO_OFFSET_RESET_CONFIG);
        addStringParamIfPresent(ConsumerConfig.PARTITION_ASSIGNMENT_STRATEGY_CONFIG, configurations, properties,
                CONSUMER_PARTITION_ASSIGNMENT_STRATEGY_CONFIG);
        addStringParamIfPresent(ConsumerConfig.METRICS_RECORDING_LEVEL_CONFIG, configurations, properties,
                CONSUMER_METRICS_RECORDING_LEVEL_CONFIG);
        addStringParamIfPresent(ConsumerConfig.METRIC_REPORTER_CLASSES_CONFIG, configurations, properties,
                CONSUMER_METRIC_REPORTER_CLASSES_CONFIG);
        addStringParamIfPresent(ConsumerConfig.CLIENT_ID_CONFIG, configurations, properties,
                CONSUMER_CLIENT_ID_CONFIG);
        addStringParamIfPresent(ConsumerConfig.INTERCEPTOR_CLASSES_CONFIG, configurations, properties,
                CONSUMER_INTERCEPTOR_CLASSES_CONFIG);
        addStringParamIfPresent(ConsumerConfig.ISOLATION_LEVEL_CONFIG, configurations, properties,
                CONSUMER_ISOLATION_LEVEL_CONFIG);

        addStringArrayParamIfPresent(ALIAS_TOPICS, configurations, properties,
                ALIAS_TOPICS);
        addStringArrayParamIfPresent(PROPERTIES_ARRAY, configurations, properties, PROPERTIES_ARRAY);

        addIntParamIfPresent(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, configurations, properties,
                CONSUMER_SESSION_TIMEOUT_MS_CONFIG);
        addIntParamIfPresent(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, configurations, properties,
                CONSUMER_HEARTBEAT_INTERVAL_MS_CONFIG);
        addIntParamIfPresent(ConsumerConfig.METADATA_MAX_AGE_CONFIG, configurations, properties,
                CONSUMER_METADATA_MAX_AGE_CONFIG);
        addIntParamIfPresent(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, configurations, properties,
                CONSUMER_AUTO_COMMIT_INTERVAL_MS_CONFIG);
        addIntParamIfPresent(ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG, configurations, properties,
                CONSUMER_MAX_PARTITION_FETCH_BYTES_CONFIG);
        addIntParamIfPresent(ConsumerConfig.SEND_BUFFER_CONFIG, configurations, properties,
                CONSUMER_SEND_BUFFER_CONFIG);
        addIntParamIfPresent(ConsumerConfig.RECEIVE_BUFFER_CONFIG, configurations, properties,
                CONSUMER_RECEIVE_BUFFER_CONFIG);
        addIntParamIfPresent(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, configurations, properties,
                CONSUMER_FETCH_MIN_BYTES_CONFIG);
        addIntParamIfPresent(ConsumerConfig.FETCH_MAX_BYTES_CONFIG, configurations, properties,
                CONSUMER_FETCH_MAX_BYTES_CONFIG);
        addIntParamIfPresent(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, configurations, properties,
                CONSUMER_FETCH_MAX_WAIT_MS_CONFIG);
        addIntParamIfPresent(ConsumerConfig.RECONNECT_BACKOFF_MS_CONFIG, configurations, properties,
                CONSUMER_RECONNECT_BACKOFF_MS_CONFIG);
        addIntParamIfPresent(ConsumerConfig.RETRY_BACKOFF_MS_CONFIG, configurations, properties,
                CONSUMER_RETRY_BACKOFF_MS_CONFIG);
        addIntParamIfPresent(ConsumerConfig.METRICS_SAMPLE_WINDOW_MS_CONFIG, configurations, properties,
                CONSUMER_METRICS_SAMPLE_WINDOW_MS_CONFIG);

        addIntParamIfPresent(ConsumerConfig.METRICS_NUM_SAMPLES_CONFIG, configurations, properties,
                CONSUMER_METRICS_NUM_SAMPLES_CONFIG);
        addIntParamIfPresent(ConsumerConfig.REQUEST_TIMEOUT_MS_CONFIG, configurations, properties,
                CONSUMER_REQUEST_TIMEOUT_MS_CONFIG);
        addIntParamIfPresent(ConsumerConfig.CONNECTIONS_MAX_IDLE_MS_CONFIG, configurations, properties,
                CONSUMER_CONNECTIONS_MAX_IDLE_MS_CONFIG);
        addIntParamIfPresent(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, configurations, properties,
                CONSUMER_MAX_POLL_RECORDS_CONFIG);
        addIntParamIfPresent(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, configurations, properties,
                CONSUMER_MAX_POLL_INTERVAL_MS_CONFIG);
        addIntParamIfPresent(ConsumerConfig.RECONNECT_BACKOFF_MAX_MS_CONFIG, configurations, properties,
                CONSUMER_RECONNECT_BACKOFF_MAX_MS_CONFIG);
        addIntParamIfPresent(ConsumerConfig.DEFAULT_API_TIMEOUT_MS_CONFIG, configurations, properties,
                CONSUMER_DEFAULT_API_TIMEOUT_CONFIG);

        addIntParamIfPresent(ALIAS_POLLING_TIMEOUT, configurations, properties, ALIAS_POLLING_TIMEOUT);
        addIntParamIfPresent(ALIAS_POLLING_INTERVAL, configurations, properties, ALIAS_POLLING_INTERVAL);
        addIntParamIfPresent(ALIAS_CONCURRENT_CONSUMERS, configurations, properties, ALIAS_CONCURRENT_CONSUMERS);

        addBooleanParamIfPresent(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, configurations, properties,
                CONSUMER_ENABLE_AUTO_COMMIT_CONFIG, true);
        addBooleanParamIfPresent(ConsumerConfig.CHECK_CRCS_CONFIG, configurations, properties,
                CONSUMER_CHECK_CRCS_CONFIG, true);
        addBooleanParamIfPresent(ConsumerConfig.EXCLUDE_INTERNAL_TOPICS_CONFIG, configurations, properties,
                CONSUMER_EXCLUDE_INTERNAL_TOPICS_CONFIG, true);

        addBooleanParamIfPresent(ALIAS_DECOUPLE_PROCESSING, configurations, properties,
                ALIAS_DECOUPLE_PROCESSING, false);
        if (Objects.nonNull(configurations.get(SECURE_SOCKET))) {
            processSSLProperties(configurations, properties);
        }
        processDefaultConsumerProperties(properties);
        return properties;
    }

    @SuppressWarnings(UNCHECKED)
    private static void processSSLProperties(MapValue<String, Object> configurations, Properties configParams) {
        MapValue<String, Object> secureSocket = (MapValue<String, Object>) configurations.get(SECURE_SOCKET);
        addStringParamIfPresent(SslConfigs.SSL_KEYSTORE_TYPE_CONFIG,
                (MapValue<String, Object>) secureSocket.get(KEYSTORE_CONFIG), configParams,
                KEYSTORE_TYPE_CONFIG);
        addStringParamIfPresent(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG,
                (MapValue<String, Object>) secureSocket.get(KEYSTORE_CONFIG), configParams,
                LOCATION_CONFIG);
        addStringParamIfPresent(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG,
                (MapValue<String, Object>) secureSocket.get(KEYSTORE_CONFIG), configParams,
                PASSWORD_CONFIG);
        addStringParamIfPresent(SslConfigs.SSL_KEYMANAGER_ALGORITHM_CONFIG,
                (MapValue<String, Object>) secureSocket.get(KEYSTORE_CONFIG), configParams,
                KEYMANAGER_ALGORITHM_CONFIG);
        addStringParamIfPresent(SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG,
                (MapValue<String, Object>) secureSocket.get(TRUSTSTORE_CONFIG), configParams,
                TRUSTSTORE_TYPE_CONFIG);
        addStringParamIfPresent(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG,
                (MapValue<String, Object>) secureSocket.get(TRUSTSTORE_CONFIG), configParams,
                LOCATION_CONFIG);
        addStringParamIfPresent(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG,
                (MapValue<String, Object>) secureSocket.get(TRUSTSTORE_CONFIG), configParams,
                PASSWORD_CONFIG);
        addStringParamIfPresent(SslConfigs.SSL_TRUSTMANAGER_ALGORITHM_CONFIG,
                (MapValue<String, Object>) secureSocket.get(TRUSTSTORE_CONFIG), configParams,
                TRUSTMANAGER_ALGORITHM_CONFIG);
        addStringParamIfPresent(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG,
                (MapValue<String, Object>) secureSocket.get(PROTOCOL_CONFIG), configParams,
                SECURITY_PROTOCOL_CONFIG);
        addStringParamIfPresent(SslConfigs.SSL_PROTOCOL_CONFIG,
                (MapValue<String, Object>) secureSocket.get(PROTOCOL_CONFIG), configParams,
                SSL_PROTOCOL_CONFIG);
        addStringParamIfPresent(SslConfigs.SSL_ENABLED_PROTOCOLS_CONFIG,
                (MapValue<String, Object>) secureSocket.get(PROTOCOL_CONFIG), configParams,
                ENABLED_PROTOCOLS_CONFIG);
        addStringParamIfPresent(SslConfigs.SSL_PROVIDER_CONFIG, configurations, configParams,
                SSL_PROVIDER_CONFIG);
        addStringParamIfPresent(SslConfigs.SSL_KEY_PASSWORD_CONFIG, configurations, configParams,
                SSL_KEY_PASSWORD_CONFIG);
        addStringParamIfPresent(SslConfigs.SSL_CIPHER_SUITES_CONFIG, configurations, configParams,
                SSL_CIPHER_SUITES_CONFIG);
        addStringParamIfPresent(SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG, configurations, configParams,
                SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG);
        addStringParamIfPresent(SslConfigs.SSL_SECURE_RANDOM_IMPLEMENTATION_CONFIG, configurations, configParams,
                SSL_SECURE_RANDOM_IMPLEMENTATION_CONFIG);
    }

    public static Properties processKafkaProducerConfig(MapValue<String, Object> configurations) {
        Properties properties = new Properties();
        if (Objects.isNull(configurations)) {
            processDefaultProducerProperties(properties);
            return properties;
        }
        addStringParamIfPresent(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, configurations,
                properties, PRODUCER_BOOTSTRAP_SERVERS_CONFIG);
        addStringParamIfPresent(ProducerConfig.ACKS_CONFIG, configurations,
                properties, PRODUCER_ACKS_CONFIG);
        addStringParamIfPresent(ProducerConfig.COMPRESSION_TYPE_CONFIG, configurations,
                properties, PRODUCER_COMPRESSION_TYPE_CONFIG);
        addStringParamIfPresent(ProducerConfig.CLIENT_ID_CONFIG, configurations,
                properties, PRODUCER_CLIENT_ID_CONFIG);
        addStringParamIfPresent(ProducerConfig.METRICS_RECORDING_LEVEL_CONFIG, configurations,
                properties, PRODUCER_METRICS_RECORDING_LEVEL_CONFIG);
        addStringParamIfPresent(ProducerConfig.METRIC_REPORTER_CLASSES_CONFIG, configurations,
                properties, PRODUCER_METRIC_REPORTER_CLASSES_CONFIG);
        addStringParamIfPresent(ProducerConfig.PARTITIONER_CLASS_CONFIG, configurations,
                properties, PRODUCER_PARTITIONER_CLASS_CONFIG);
        addStringParamIfPresent(ProducerConfig.INTERCEPTOR_CLASSES_CONFIG, configurations,
                properties, PRODUCER_INTERCEPTOR_CLASSES_CONFIG);
        addStringParamIfPresent(ProducerConfig.TRANSACTIONAL_ID_CONFIG, configurations,
                properties, PRODUCER_TRANSACTIONAL_ID_CONFIG);

        addIntParamIfPresent(ProducerConfig.BUFFER_MEMORY_CONFIG, configurations,
                properties, PRODUCER_BUFFER_MEMORY_CONFIG);
        addIntParamIfPresent(ProducerConfig.RETRIES_CONFIG, configurations,
                properties, PRODUCER_RETRIES_CONFIG);
        addIntParamIfPresent(ProducerConfig.BATCH_SIZE_CONFIG, configurations,
                properties, PRODUCER_BATCH_SIZE_CONFIG);
        addIntParamIfPresent(ProducerConfig.LINGER_MS_CONFIG, configurations,
                properties, PRODUCER_LINGER_MS_CONFIG);
        addIntParamIfPresent(ProducerConfig.SEND_BUFFER_CONFIG, configurations,
                properties, PRODUCER_SEND_BUFFER_CONFIG);
        addIntParamIfPresent(ProducerConfig.RECEIVE_BUFFER_CONFIG, configurations,
                properties, PRODUCER_RECEIVE_BUFFER_CONFIG);
        addIntParamIfPresent(ProducerConfig.MAX_REQUEST_SIZE_CONFIG, configurations,
                properties, PRODUCER_MAX_REQUEST_SIZE_CONFIG);
        addIntParamIfPresent(ProducerConfig.RECONNECT_BACKOFF_MS_CONFIG, configurations,
                properties, PRODUCER_RECONNECT_BACKOFF_MS_CONFIG);
        addIntParamIfPresent(ProducerConfig.RECONNECT_BACKOFF_MAX_MS_CONFIG, configurations,
                properties, PRODUCER_RECONNECT_BACKOFF_MAX_MS_CONFIG);
        addIntParamIfPresent(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, configurations,
                properties, PRODUCER_RETRY_BACKOFF_MS_CONFIG);
        addIntParamIfPresent(ProducerConfig.MAX_BLOCK_MS_CONFIG, configurations,
                properties, PRODUCER_MAX_BLOCK_MS_CONFIG);
        addIntParamIfPresent(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, configurations,
                properties, PRODUCER_REQUEST_TIMEOUT_MS_CONFIG);
        addIntParamIfPresent(ProducerConfig.METADATA_MAX_AGE_CONFIG, configurations,
                properties, PRODUCER_METADATA_MAX_AGE_CONFIG);
        addIntParamIfPresent(ProducerConfig.METRICS_SAMPLE_WINDOW_MS_CONFIG, configurations,
                properties, PRODUCER_METRICS_SAMPLE_WINDOW_MS_CONFIG);
        addIntParamIfPresent(ProducerConfig.METRICS_NUM_SAMPLES_CONFIG, configurations,
                properties, PRODUCER_METRICS_NUM_SAMPLES_CONFIG);
        addIntParamIfPresent(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, configurations,
                properties, PRODUCER_MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION);
        addIntParamIfPresent(ProducerConfig.CONNECTIONS_MAX_IDLE_MS_CONFIG, configurations,
                properties, PRODUCER_CONNECTIONS_MAX_IDLE_MS_CONFIG);
        addIntParamIfPresent(ProducerConfig.TRANSACTION_TIMEOUT_CONFIG, configurations,
                properties, PRODUCER_TRANSACTION_TIMEOUT_CONFIG);

        addBooleanParamIfPresent(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, configurations,
                properties, PRODUCER_ENABLE_IDEMPOTENCE_CONFIG);
        if (Objects.nonNull(configurations.get(SECURE_SOCKET))) {
            processSSLProperties(configurations, properties);
        }
        processDefaultProducerProperties(properties);
        return properties;
    }

    private static void addStringParamIfPresent(String paramName,
                                                MapValue<String, Object> configs,
                                                Properties configParams,
                                                String key) {
        if (Objects.nonNull(configs.get(key))) {
            String value = (String) configs.get(key);
            if (!(value == null || value.equals(""))) {
                configParams.put(paramName, value);
            }
        }
    }

    private static void addStringArrayParamIfPresent(String paramName,
                                                     MapValue<String, Object> configs,
                                                     Properties configParams,
                                                     String key) {
        ArrayValue stringArray = (ArrayValue) configs.get(key);
        List<String> values = getStringListFromStringArrayValue(stringArray);
        configParams.put(paramName, values);
    }

    private static void addIntParamIfPresent(String paramName,
                                             MapValue<String, Object> configs,
                                             Properties configParams,
                                             String key) {
        long value = (long) configs.get(key);
        if (value != -1) {
            configParams.put(paramName, Long.valueOf(value).intValue());
        }
    }

    private static void addBooleanParamIfPresent(String paramName,
                                                 MapValue<String, Object> configs,
                                                 Properties configParams,
                                                 String key,
                                                 boolean defaultValue) {
        boolean value = (boolean) configs.get(key);
        if (value != defaultValue) {
            configParams.put(paramName, value);
        }
    }

    private static void addBooleanParamIfPresent(String paramName,
                                                 MapValue<String, Object> configs,
                                                 Properties configParams,
                                                 String key) {
        boolean value = (boolean) configs.get(key);
        configParams.put(paramName, value);
    }

    public static void processDefaultConsumerProperties(Properties configParams) {
        configParams.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, DEFAULT_KEY_DESERIALIZER);
        configParams.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, DEFAULT_VALUE_DESERIALIZER);
    }

    public static void processDefaultProducerProperties(Properties configParams) {
        configParams.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, DEFAULT_KEY_SERIALIZER);
        configParams.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, DEFAULT_VALUE_SERIALIZER);
    }

    public static ArrayList<TopicPartition> getTopicPartitionList(ArrayValue partitions, Logger logger) {
        ArrayList<TopicPartition> partitionList = new ArrayList<>();
        if (partitions != null) {
            for (int counter = 0; counter < partitions.size(); counter++) {
                MapValue<String, Object> partition = (MapValue<String, Object>) partitions.get(counter);
                String topic = (String) partition.get(ALIAS_TOPIC);
                int partitionValue = getIntFromLong((Long) partition.get(ALIAS_PARTITION), logger, ALIAS_PARTITION);
                partitionList.add(new TopicPartition(topic, partitionValue));
            }
        }
        return partitionList;
    }

    public static ArrayList<String> getStringListFromStringArrayValue(ArrayValue stringArray) {
        ArrayList<String> values = new ArrayList<>();
        if ((Objects.isNull(stringArray)) ||
                (!((BArrayType) stringArray.getType()).getElementType().equals(BTypes.typeString))) {
            return values;
        }
        if (stringArray.size() != 0) {
            for (int i = 0; i < stringArray.size(); i++) {
                values.add(stringArray.getString(i));
            }
        }
        return values;
    }

    /**
     * Populate the {@code TopicPartition} record type in Ballerina.
     *
     * @param topic     name of the topic
     * @param partition value of the partition offset
     * @return {@code MapValue} of the record
     */
    public static MapValue<String, Object> populateTopicPartitionRecord(String topic, int partition) {

        return createRecord(getTopicPartitionRecord(), topic, partition);
    }

    public static MapValue<String, Object> populatePartitionOffsetRecord(MapValue<String, Object> topicPartition,
                                                                         long offset) {
        return createRecord(getPartitionOffsetRecord(), topicPartition, offset);
    }

    public static MapValue<String, Object> populateConsumerRecord(ConsumerRecord<byte[], byte[]> record) {
        if (Objects.isNull(record)) {
            return null;
        }
        return createRecord(getConsumerRecord(),
                getByteArrayValue(record.key()),
                getByteArrayValue(record.value()),
                record.offset(),
                record.partition(),
                record.timestamp(),
                record.topic());
    }

    private static ArrayValue getByteArrayValue(byte[] byteArray) {
        if (Objects.isNull(byteArray)) {
            return null;
        }
        return new ArrayValue(byteArray);
    }

    public static MapValue<String, Object> getConsumerRecord() {
        return createKafkaRecord(CONSUMER_RECORD_STRUCT_NAME);
    }

    public static MapValue<String, Object> getPartitionOffsetRecord() {
        return createKafkaRecord(OFFSET_STRUCT_NAME);
    }

    public static MapValue<String, Object> getTopicPartitionRecord() {
        return createKafkaRecord(TOPIC_PARTITION_STRUCT_NAME);
    }

    public static ErrorValue createKafkaError(String message) {
        return createKafkaError(message, CONSUMER_ERROR);
    }

    public static ErrorValue createKafkaError(String message, String reason) {
        MapValue<String, Object> detail = createKafkaDetailRecord(message);
        return BallerinaErrors.createError(reason, detail);
    }

    private static MapValue<String, Object> createKafkaDetailRecord(String message) {
        return createKafkaDetailRecord(message, null);
    }

    private static MapValue<String, Object> createKafkaDetailRecord(String message, ErrorValue cause) {
        MapValue<String, Object> detail = createKafkaRecord(DETAIL_RECORD_NAME);
        return BallerinaValues.createRecord(detail, message, cause);
    }

    public static MapValue<String, Object> createKafkaRecord(String recordName) {
        return BallerinaValues.createRecordValue(KAFKA_PROTOCOL_PACKAGE_ID, recordName);
    }

    public static ArrayValue getPartitionOffsetArrayFromOffsetMap(Map<TopicPartition, Long> offsetMap) {
        ArrayValue partitionOffsetArray = new ArrayValue(new BArrayType(getPartitionOffsetRecord().getType()));
        if (!offsetMap.entrySet().isEmpty()) {
            // TODO: remove the counter variable and use append method once #17075 fixed.
            int i = 0;
            for (Map.Entry<TopicPartition, Long> entry : offsetMap.entrySet()) {
                TopicPartition tp = entry.getKey();
                Long offset = entry.getValue();
                MapValue<String, Object> topicPartition = populateTopicPartitionRecord(tp.topic(), tp.partition());
                MapValue<String, Object> partition = populatePartitionOffsetRecord(topicPartition, offset);
                partitionOffsetArray.add(i++, partition);
            }
        }
        return partitionOffsetArray;
    }

    /**
     * Get {@code Map<TopicPartition, OffsetAndMetadata>} map used in committing consumers.
     *
     * @param offsets ArrayValue of Ballerina {@code PartitionOffset} records
     * @return {@code Map<TopicPartition, OffsetAndMetadata>} created using Ballerina {@code PartitionOffset}
     */
    public static Map<TopicPartition, OffsetAndMetadata> getPartitionToMetadataMap(ArrayValue offsets) {
        Map<TopicPartition, OffsetAndMetadata> partitionToMetadataMap = new HashMap<>();
        for (int i = 0; i < offsets.size(); i++) {
            MapValue offset = (MapValue) offsets.get(i);
            int offsetValue = offset.getIntValue(ALIAS_OFFSET).intValue();
            TopicPartition topicPartition = createTopicPartitionFromPartitionOffset(offset);
            partitionToMetadataMap.put(topicPartition, new OffsetAndMetadata(offsetValue));
        }
        return partitionToMetadataMap;
    }

    /**
     * Get {@code TopicPartition} object from {@code MapValue} of Ballerina {@code PartitionOffset}.
     *
     * @param offset MapValue consists of Ballerina PartitionOffset record.
     * @return TopicPartition Object created
     */
    public static TopicPartition createTopicPartitionFromPartitionOffset(MapValue offset) {
        MapValue partition = (MapValue) offset.get(ALIAS_PARTITION);
        String topic = partition.getStringValue(ALIAS_TOPIC);
        int partitionValue = partition.getIntValue(ALIAS_PARTITION).intValue();

        return new TopicPartition(topic, partitionValue);
    }

    /**
     * Get the Integer value from an Object, if possible.
     *
     * @param value  the {@code Object} which needs to be converted to int
     * @param name   name of the parameter, for logging purposes
     * @param logger {@code Logger} instance to log if there's an issue
     * @return Integer value of the {@code Object}, {@code null} otherwise
     */
    public static Integer getIntValue(Object value, String name, Logger logger) {
        Long longValue = getLongValue(value);
        if (Objects.isNull(longValue)) {
            return null;
        }
        return getIntFromLong(longValue, logger, name);
    }

    /**
     * Get the {@code int} value from a {@code long} value.
     *
     * @param longValue {@code long} value, which we want to convert
     * @param logger    {@code Logger} instance, to log the error if there's an error
     * @param name      parameter name, which will be converted. This is required for logging purposes
     * @return {@code int} value of the {@code long} value, if possible, {@code Integer.MAX_VALUE} is the number is too
     * large
     */
    public static int getIntFromLong(long longValue, Logger logger, String name) {
        try {
            return Math.toIntExact(longValue);
        } catch (ArithmeticException e) {
            logger.warn("The value set for {} needs to be less than {}. The {} value is set to {}", name,
                    Integer.MAX_VALUE, name, Integer.MAX_VALUE);
            return Integer.MAX_VALUE;
        }
    }

    /**
     * Get the {@code Long} value from an {@code Object}.
     *
     * @param value Object from which we want to get the Long value
     * @return Long value of the Object, if present. {@code null} otherwise
     */
    public static Long getLongValue(Object value) {
        if (Objects.isNull(value)) {
            return null;
        }
        return (Long) value;
    }

    /**
     * Get the default API timeout defined in the Kafka configurations.
     *
     * @param consumerProperties - Native consumer properties object
     * @return value of the default api timeout, if defined, -1 otherwise.
     */
    public static int getDefaultApiTimeout(Properties consumerProperties) {
        if (Objects.nonNull(consumerProperties.get(ConsumerConfig.DEFAULT_API_TIMEOUT_MS_CONFIG))) {
            return (int) consumerProperties.get(ConsumerConfig.DEFAULT_API_TIMEOUT_MS_CONFIG);
        }
        return DURATION_UNDEFINED_VALUE;
    }

    public static void createKafkaProducer(Properties producerProperties, ObjectValue producerObject) {
        KafkaProducer<byte[], byte[]> kafkaProducer = new KafkaProducer<>(producerProperties);
        producerObject.addNativeData(NATIVE_PRODUCER, kafkaProducer);
        producerObject.addNativeData(NATIVE_PRODUCER_CONFIG, producerProperties);
    }

    public static String getBrokerNames(ObjectValue listener) {
        MapValue<String, Object> listenerConfigurations = listener.getMapValue(CONSUMER_CONFIG_FIELD_NAME);
        return (String) listenerConfigurations.get(CONSUMER_BOOTSTRAP_SERVERS_CONFIG);
    }

    public static String getTopicNamesString(ArrayList<String> topicsList) {
        return String.join(", ", topicsList);
    }
}
