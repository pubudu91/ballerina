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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.stdlib.utils;

import org.ballerinalang.jvm.BallerinaValues;
import org.ballerinalang.jvm.values.ObjectValue;

import static org.ballerinalang.mime.util.MimeConstants.MEDIA_TYPE;
import static org.ballerinalang.mime.util.MimeConstants.PROTOCOL_MIME_PKG_ID;
import static org.ballerinalang.net.http.HttpConstants.ENTITY;
import static org.ballerinalang.net.http.HttpConstants.PROTOCOL_HTTP_PKG_ID;
import static org.ballerinalang.net.http.HttpConstants.PUSH_PROMISE;
import static org.ballerinalang.net.http.HttpConstants.REQUEST;
import static org.ballerinalang.net.http.HttpConstants.REQUEST_CACHE_CONTROL;
import static org.ballerinalang.net.http.HttpConstants.RESPONSE;
import static org.ballerinalang.net.http.HttpConstants.RESPONSE_CACHE_CONTROL;

/**
 * Utility functions to create JVM values.
 *
 * @since 1.0
 */
public class ValueCreatorUtils {

    public static ObjectValue createRequestObject() {
        return BallerinaValues.createObjectValue(PROTOCOL_HTTP_PKG_ID, REQUEST);
    }

    public static ObjectValue createResponseObject() {
        return BallerinaValues.createObjectValue(PROTOCOL_HTTP_PKG_ID, RESPONSE);
    }

    public static ObjectValue createEntityObject() {
        return BallerinaValues.createObjectValue(PROTOCOL_MIME_PKG_ID, ENTITY);
    }

    public static ObjectValue createMediaTypeObject() {
        return BallerinaValues.createObjectValue(PROTOCOL_MIME_PKG_ID, MEDIA_TYPE);
    }

    public static ObjectValue createPushPromiseObject() {
        return BallerinaValues.createObjectValue(PROTOCOL_HTTP_PKG_ID, PUSH_PROMISE, "/", "GET");
    }

    public static ObjectValue createRequestCacheControlObject() {
        return BallerinaValues.createObjectValue(PROTOCOL_HTTP_PKG_ID, REQUEST_CACHE_CONTROL);
    }

    public static ObjectValue createResponseCacheControlObject() {
        return BallerinaValues.createObjectValue(PROTOCOL_HTTP_PKG_ID, RESPONSE_CACHE_CONTROL);
    }
}
