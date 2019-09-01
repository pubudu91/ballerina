/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.jvm.values.freeze;

import org.ballerinalang.jvm.util.exceptions.BLangFreezeException;
import org.ballerinalang.jvm.util.exceptions.BallerinaErrorReasons;

import static org.ballerinalang.jvm.util.exceptions.BallerinaErrorReasons.INVALID_UPDATE_ERROR_IDENTIFIER;
import static org.ballerinalang.jvm.util.exceptions.BallerinaErrorReasons.getModulePrefixedReason;

/**
 * Class for freeze() util methods.
 *
 * @since 0.995.0
 */
public class FreezeUtils {

    /**
     * Method to check if a value is open to a new freeze attempt.
     *
     * @param currentFreezeStatus the current freeze status of the value
     * @param receivedFreezeStatus the received freeze status of the new freeze attempt
     * @return true if the state is unfrozen, false if not. Would throw a {@link BLangFreezeException} if the value
     *         is already part of a different freeze attempt.
     */
    public static boolean isOpenForFreeze(Status currentFreezeStatus, Status receivedFreezeStatus) {
        switch (currentFreezeStatus.getState()) {
            case FROZEN:
                return false;
            case MID_FREEZE:
                if (currentFreezeStatus == receivedFreezeStatus) {
                    return false;
                }
                throw new BLangFreezeException(BallerinaErrorReasons.CONCURRENT_MODIFICATION_ERROR,
                        "concurrent 'freeze()' attempts not allowed");
            default:
                return true;
        }
    }

    /**
     * Method to handle an update to a value, that is invalid due to a freeze related state.
     *
     * An update to a value would panic either if a value is frozen or if a value is currently in the process of
     * being frozen.
     *
     * @param currentState the current {@link State} of the value
     * @param moduleName the name of the langlib module for whose values the error occurred
     */
    public static void handleInvalidUpdate(State currentState, String moduleName) {
        switch (currentState) {
            case FROZEN:
                throw new BLangFreezeException(getModulePrefixedReason(moduleName, INVALID_UPDATE_ERROR_IDENTIFIER),
                                               "modification not allowed on readonly value");
            case MID_FREEZE:
                throw new BLangFreezeException(getModulePrefixedReason(moduleName, INVALID_UPDATE_ERROR_IDENTIFIER),
                                               "modification not allowed during freeze");
            default:
                return;
        }
    }
}
