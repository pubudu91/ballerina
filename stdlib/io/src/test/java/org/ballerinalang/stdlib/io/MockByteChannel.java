/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.ballerinalang.stdlib.io;

import org.ballerinalang.stdlib.io.channels.base.Channel;

import java.nio.channels.ByteChannel;
import java.nio.channels.WritableByteChannel;

/**
 * Mock implementation of byte channel for testing purposes.
 */
public class MockByteChannel extends Channel {

    public MockByteChannel(ByteChannel channel) {
        super(channel);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void transfer(int position, int count, WritableByteChannel dstChannel) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Channel getChannel() {
        throw new UnsupportedOperationException();
    }

    public boolean remaining() {
        return false;
    }
}
