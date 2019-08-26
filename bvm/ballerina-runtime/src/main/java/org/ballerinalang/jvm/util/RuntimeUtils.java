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
package org.ballerinalang.jvm.util;

import org.ballerinalang.jvm.TypeConverter;
import org.ballerinalang.jvm.types.BArrayType;
import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.types.TypeTags;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.ErrorValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintStream;
import java.util.Optional;

import static org.ballerinalang.jvm.util.BLangConstants.BBYTE_MAX_VALUE;
import static org.ballerinalang.jvm.util.BLangConstants.BBYTE_MIN_VALUE;

/**
 * Util methods required for jBallerina runtime.
 *
 * @since 0.995.0
 */

public class RuntimeUtils {

    private static PrintStream errStream = System.err;

    private static final Logger breLog = LoggerFactory.getLogger(RuntimeUtils.class);

    /**
     * Used to handle rest args passed in to the main method.
     *
     * @param args  args from main method
     * @param index starting index of var args
     * @param type  array type
     * @return ArrayValue
     */
    public static ArrayValue createVarArgsArray(String[] args, int index, BArrayType type) {

        ArrayValue array = new ArrayValue(type, type.getSize());
        for (int i = index; i < args.length; i++) {
            addToArray(type.getElementType(), args[i], array);
        }
        return array;
    }

    public static void addToArray(BType type, String value, ArrayValue array) {
        // TODO: need to add parsing logic for ref values for both var args and other args as well.
        switch (type.getTag()) {
            case TypeTags.STRING_TAG:
                array.add(array.size(), value);
                break;
            case TypeTags.INT_TAG:
                array.add(array.size(), (long) TypeConverter.convertValues(type, value));
                break;
            case TypeTags.FLOAT_TAG:
                array.add(array.size(), (double) TypeConverter.convertValues(type, value));
                break;
            case TypeTags.BOOLEAN_TAG:
                array.add(array.size(), (boolean) TypeConverter.convertValues(type, value));
                break;
            case TypeTags.BYTE_TAG:
                array.add(array.size(), (int) TypeConverter.convertValues(type, value));
                break;
            default:
                array.append((Object) value);
        }
    }

    /**
     * Check a given int value is within ballerina byte value range.
     *
     * @param intValue integer value
     * @return true if within byte value range
     */
    public static boolean isByteLiteral(int intValue) {

        return (intValue >= BBYTE_MIN_VALUE && intValue <= BBYTE_MAX_VALUE);
    }

    /**
     * Keep a function parameter info, required for argument parsing.
     */
    public static class ParamInfo {

        String name;
        boolean hasDefaultable;
        BType type;
        int index = -1;

        public ParamInfo(boolean hasDefaultable, String name, BType type) {

            this.name = name;
            this.hasDefaultable = hasDefaultable;
            this.type = type;
        }
    }

    public static void handleRuntimeErrors(Throwable throwable) {
        if (throwable instanceof ErrorValue) {
            errStream.println("error: " + ((ErrorValue) throwable).getPrintableStackTrace());
        } else {
            // These errors are unhandled errors in JVM, hence logging them to bre log.
            errStream.println(BLangConstants.INTERNAL_ERROR_MESSAGE);
            breLog.error(throwable.getMessage(), throwable);
        }

        Runtime.getRuntime().exit(1);
    }

    public static void handleRuntimeReturnValues(Object returnValue) {
        if (returnValue instanceof ErrorValue) {
            ErrorValue errorValue = (ErrorValue) returnValue;
            errStream.println("error: " + errorValue.getReason() +
                    Optional.ofNullable(errorValue.getDetails()).map(details -> " " + details).orElse(""));
            Runtime.getRuntime().exit(1);
        }
    }

    public static void handleInvalidOption(String arg) {
        handleUsageError("value for option '--' (<String=String>) should be in KEY=VALUE format but was " + arg);
    }

    public static void handleInvalidConfig() {
        handleUsageError("value for option 'config' is missing");
    }
    
    public static void handleUsageError(String errorMsg) {
        errStream.println("ballerina: " + errorMsg);
        Runtime.getRuntime().exit(1);
    }
    
    public static void silentlyLogBadSad(Throwable throwable) {
        // These errors are unhandled errors in JVM, hence logging them to bre log.
        breLog.error(throwable.getMessage(), throwable);
    }
}
