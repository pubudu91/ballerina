/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.stdlib.filepath;

import static org.ballerinalang.jvm.util.BLangConstants.ORG_NAME_SEPARATOR;

/**
 * Constants for path package file functions.
 *
 * @since 0.995.0
 */
public class Constants {
    /**
     * Organization name.
     */
    public static final String ORG_NAME = "ballerina";

    /**
     * Package name.
     */
    public static final String PACKAGE_NAME = "filepath";

    /**
     * Package path to path package.
     */
    public static final String PACKAGE_PATH = ORG_NAME + ORG_NAME_SEPARATOR + PACKAGE_NAME;

    public static final String ERROR_REASON_PREFIX = "{ballerina/filepath}";

    public static final String FILE_NOT_FOUND_ERROR = "{ballerina/filepath}FileNotFoundError";
    public static final String NOT_LINK_ERROR = "{ballerina/filepath}NotLinkError";
    public static final String IO_ERROR = "{ballerina/filepath}IOError";
    public static final String SECURITY_ERROR = "{ballerina/filepath}SecurityError";
    public static final String INVALID_PATH_ERROR = "{ballerina/filepath}InvalidPathError";
    public static final String INVALID_PATTERN_ERROR = "{ballerina/filepath}InvalidPatternError";
    public static final String GENERIC_ERROR = "{ballerina/filepath}GenericError";
}
