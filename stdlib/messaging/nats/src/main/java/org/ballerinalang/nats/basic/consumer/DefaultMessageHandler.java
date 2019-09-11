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

package org.ballerinalang.nats.basic.consumer;

import io.nats.client.Message;
import io.nats.client.MessageHandler;
import org.ballerinalang.jvm.BallerinaValues;
import org.ballerinalang.jvm.scheduling.Scheduler;
import org.ballerinalang.jvm.services.ErrorHandlerUtils;
import org.ballerinalang.jvm.types.AttachedFunction;
import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.ErrorValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.connector.CallableUnitCallback;
import org.ballerinalang.jvm.values.connector.Executor;
import org.ballerinalang.nats.Constants;
import org.ballerinalang.nats.Utils;

import java.util.concurrent.CountDownLatch;

import static org.ballerinalang.nats.Constants.ON_MESSAGE_RESOURCE;
import static org.ballerinalang.nats.Utils.bindDataToIntendedType;
import static org.ballerinalang.nats.Utils.getAttachedFunction;

/**
 * Handles incoming message for a given subscription.
 *
 * @since 1.0.0
 */
public class DefaultMessageHandler implements MessageHandler {

    // Resource which the message should be dispatched.
    private ObjectValue serviceObject;
    private Scheduler scheduler;

    DefaultMessageHandler(Scheduler scheduler, ObjectValue serviceObject) {
        this.scheduler = scheduler;
        this.serviceObject = serviceObject;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onMessage(Message message) {
        ArrayValue msgData = new ArrayValue(message.getData());
        ObjectValue msgObj = BallerinaValues.createObjectValue(Constants.NATS_PACKAGE_ID,
                Constants.NATS_MESSAGE_OBJ_NAME, message.getSubject(), msgData, message.getReplyTo());
        AttachedFunction onMessage = getAttachedFunction(serviceObject, ON_MESSAGE_RESOURCE);
        BType[] parameterTypes = onMessage.getParameterType();
        if (parameterTypes.length == 1) {
            dispatch(msgObj);
        } else {
            BType intendedTypeForData = parameterTypes[1];
            dispatchWithDataBinding(msgObj, intendedTypeForData, message.getData());
        }

    }

    /**
     * Dispatch only the message to the onMessage resource.
     *
     * @param msgObj Message object
     */
    private void dispatch(ObjectValue msgObj) {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        Executor.submit(scheduler, serviceObject, ON_MESSAGE_RESOURCE, new ResponseCallback(countDownLatch),
                null, msgObj, Boolean.TRUE);
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw Utils.createNatsError(Constants.THREAD_INTERRUPTED_ERROR);
        }
    }

    /**
     * Dispatch message and type bound data to the onMessage resource.
     *
     * @param msgObj       Message object
     * @param intendedType Message type for data binding
     * @param data         Message data
     */
    private void dispatchWithDataBinding(ObjectValue msgObj, BType intendedType, byte[] data) {
        try {
            Object typeBoundData = bindDataToIntendedType(data, intendedType);
            CountDownLatch countDownLatch = new CountDownLatch(1);
            Executor.submit(scheduler, serviceObject, ON_MESSAGE_RESOURCE,
                    new ResponseCallback(countDownLatch), null,
                    msgObj, true, typeBoundData, true);
            countDownLatch.await();
        } catch (NumberFormatException e) {
            ErrorValue dataBindError = Utils
                    .createNatsError("The received message is unsupported by the resource signature");
            ErrorHandler.dispatchError(serviceObject, scheduler, msgObj, dataBindError);
        } catch (ErrorValue e) {
            ErrorHandler.dispatchError(serviceObject, scheduler, msgObj, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw Utils.createNatsError(Constants.THREAD_INTERRUPTED_ERROR);
        }
    }

    /**
     * Represents the callback which will be triggered upon submitting to resource.
     */
    public static class ResponseCallback implements CallableUnitCallback {
        private CountDownLatch countDownLatch;

        ResponseCallback(CountDownLatch countDownLatch) {
            this.countDownLatch = countDownLatch;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void notifySuccess() {
            countDownLatch.countDown();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void notifyFailure(ErrorValue error) {
            ErrorHandlerUtils.printError(error);
            countDownLatch.countDown();
        }
    }
}
