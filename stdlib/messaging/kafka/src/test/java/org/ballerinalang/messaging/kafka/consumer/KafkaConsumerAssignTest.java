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

package org.ballerinalang.messaging.kafka.consumer;

import io.debezium.kafka.KafkaCluster;
import io.debezium.util.Testing;
import org.ballerinalang.model.types.TypeTags;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import static org.ballerinalang.messaging.kafka.utils.KafkaTestUtils.TEST_CONSUMER;
import static org.ballerinalang.messaging.kafka.utils.KafkaTestUtils.TEST_SRC;
import static org.ballerinalang.messaging.kafka.utils.KafkaTestUtils.createKafkaCluster;
import static org.ballerinalang.messaging.kafka.utils.KafkaTestUtils.getFilePath;

/**
 * Test cases for Kafka Consumer assign() function.
 */
public class KafkaConsumerAssignTest {
    private CompileResult result;
    private static KafkaCluster kafkaCluster;
    private File dataDir;

    @BeforeClass
    public void setup() throws IOException {
        dataDir =  Testing.Files.createTestingDirectory("kafka-consumer-assign-test");
        result = BCompileUtil.compile(
                getFilePath(Paths.get(TEST_SRC, TEST_CONSUMER, "kafka_consumer_assign.bal")));
        kafkaCluster = createKafkaCluster(dataDir, 14001, 14101).addBrokers(1).startup();
        kafkaCluster.createTopic("test-1", 1, 1);
    }

    @Test(description = "Test assign functions functionality")
    @SuppressWarnings("unchecked")
    public void testKafkaConsumerAssign() {
        // Invoke assign to assign topic partitions to the consumer
        BRunUtil.invoke(result, "funcKafkaAssign");

        // Check whether the partitions are assigned
        BValue[] returnBValues = BRunUtil.invoke(result, "funcKafkaGetTopicPartitions");
        Assert.assertEquals(returnBValues.length, 1);
        BMap<String, BValue> tpReturned = (BMap<String, BValue>) returnBValues[0];
        Assert.assertEquals(tpReturned.get("topic").stringValue(), "test");

    }

    @Test(description = "Test Kafka consumer getAssignment function", dependsOnMethods = "testKafkaConsumerAssign")
    public void testKafkaGetAssignment() {
        BValue[] returnBValues = BRunUtil.invoke(result, "funcKafkaGetAssignment");
        Assert.assertEquals(returnBValues.length, 1);
        Assert.assertEquals((returnBValues[0]).getType().getTag(), TypeTags.RECORD_TYPE_TAG);
        Assert.assertEquals(((BMap) returnBValues[0]).get("topic").stringValue(), "test");
    }

    @AfterClass
    public void tearDown() {
        if (kafkaCluster != null) {
            kafkaCluster.shutdown();
            kafkaCluster = null;
            boolean delete = dataDir.delete();
            // If files are still locked and a test fails: delete on exit to allow subsequent test execution
            if (!delete) {
                dataDir.deleteOnExit();
            }
        }
    }
}
