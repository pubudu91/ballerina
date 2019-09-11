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

package org.ballerinalang.jvm.values;

import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.streams.StreamSubscriptionManager;
import org.ballerinalang.jvm.types.BStreamType;
import org.ballerinalang.jvm.types.BType;

import java.util.Map;
import java.util.UUID;

/**
 * <p>
 * The {@link StreamValue} represents a stream in Ballerina.
 * </p>
 * <p>
 * <i>Note: This is an internal API and may change in future versions.</i>
 * </p>
 * 
 * @since 0.995.0
 */
public class StreamValue implements RefValue {

    private BType type;
    private BType constraintType;

    private StreamSubscriptionManager streamSubscriptionManager;

    /**
     * The name of the underlying broker topic representing the stream object.
     */
    public String streamId;

    public StreamValue(BType type) {
        this.streamSubscriptionManager = StreamSubscriptionManager.getInstance();
        this.constraintType = ((BStreamType) type).getConstrainedType();
        this.type = new BStreamType(constraintType);
        this.streamId = UUID.randomUUID().toString();
    }

    public String getStreamId() {
        return streamId;
    }

    public String stringValue(Strand strand) {
        return "stream " + streamId + " " + getType().toString();
    }

    @Override
    public BType getType() {
        return this.type;
    }

    @Override
    public Object copy(Map<Object, Object> refs) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    public Object frozenCopy(Map<Object, Object> refs) {
            throw new UnsupportedOperationException();
    }

    public BType getConstraintType() {
        return constraintType;
    }

    /**
     * Method to publish to a topic representing the stream in the broker.
     *
     * @param strand the strand in which the data being published
     * @param data the data to publish to the stream
     */
    public void publish(Strand strand, Object data) {
        streamSubscriptionManager.sendMessage(this, strand, data);
    }

    /**
     * Method to register a subscription to the underlying topic representing the stream in the broker.
     *
     * @param functionPointer represents the function pointer reference for the function to be invoked on receiving
     *                        messages
     */
    public void subscribe(FPValue<Object[], Object> functionPointer) {
        streamSubscriptionManager.registerMessageProcessor(this, functionPointer);
    }
}
