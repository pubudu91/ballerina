/*
 *   Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerina.runtime.api;

import io.ballerina.runtime.JSONParser;
import io.ballerina.runtime.api.types.JSONType;
import io.ballerina.runtime.api.types.MapType;
import io.ballerina.runtime.api.types.StructureType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;

import java.io.InputStream;
import java.io.Reader;

/**
 * Class @{@link JSONParser} provides APIs to handle json values.
 *
 * @since 2.0.0
 */
public class JSONUtils {

    /**
     * Parses the contents in the given {@link InputStream} and returns a json.
     *
     * @param in input stream which contains the JSON content
     * @return JSON structure
     * @throws BError for any parsing error
     */
    public static Object parse(InputStream in) throws BError {
        return JSONParser.parse(in);
    }

    /**
     * Parses the contents in the given {@link InputStream} and returns a json.
     *
     * @param in          input stream which contains the JSON content
     * @param charsetName the character set name of the input stream
     * @return JSON structure
     * @throws BError for any parsing error
     */
    public static Object parse(InputStream in, String charsetName) throws BError {
        return JSONParser.parse(in, charsetName);
    }

    /**
     * Parses the contents in the given string and returns a json.
     *
     * @param jsonStr the string which contains the JSON content
     * @return JSON structure
     * @throws BError for any parsing error
     */
    public static Object parse(BString jsonStr) throws BError {
        return JSONParser.parse(jsonStr.getValue());
    }

    /**
     * Parses the contents in the given string and returns a json.
     *
     * @param jsonStr the string which contains the JSON content
     * @param mode    the mode to use when processing numeric values
     * @return JSON   value if parsing is successful
     * @throws BError for any parsing error
     */
    public static Object parse(BString jsonStr, JSONUtils.NonStringValueProcessingMode mode) throws BError {
        return JSONParser.parse(jsonStr.getValue(), mode);
    }

    /**
     * Parses the contents in the given string and returns a json.
     *
     * @param jsonStr the string which contains the JSON content
     * @return JSON structure
     * @throws BError for any parsing error
     */
    public static Object parse(String jsonStr) throws BError {
        return JSONParser.parse(jsonStr);
    }

    /**
     * Parses the contents in the given string and returns a json.
     *
     * @param jsonStr the string which contains the JSON content
     * @param mode    the mode to use when processing numeric values
     * @return JSON   value if parsing is successful
     * @throws BError for any parsing error
     */
    public static Object parse(String jsonStr, JSONUtils.NonStringValueProcessingMode mode) throws BError {
        return JSONParser.parse(jsonStr, mode);
    }

    /**
     * Parses the contents in the given {@link Reader} and returns a json.
     *
     * @param reader reader which contains the JSON content
     * @param mode   the mode to use when processing numeric values
     * @return JSON structure
     * @throws BError for any parsing error
     */
    public static Object parse(Reader reader, JSONUtils.NonStringValueProcessingMode mode) throws BError {
        return JSONParser.parse(reader, mode);
    }

    /**
     * Convert {@link BArray} to JSON.
     *
     * @param bArray {@link BArray} to be converted to JSON
     * @return JSON representation of the provided bArray
     */
    public static BArray convertArrayToJSON(BArray bArray) {
        return io.ballerina.runtime.JSONUtils.convertArrayToJSON(bArray);
    }

    /**
     * Convert map value to JSON.
     *
     * @param map        value {@link BMap} to be converted to JSON
     * @param targetType the target JSON type to be convert to
     * @return JSON representation of the provided array
     */
    public static Object convertMapToJSON(BMap<BString, ?> map, JSONType targetType) {
        return io.ballerina.runtime.JSONUtils.convertMapToJSON(map, targetType);
    }

    /**
     * Convert a JSON node to a map.
     *
     * @param json    JSON to convert
     * @param mapType MapType which the JSON is converted to.
     * @return If the provided JSON is of object-type, this method will return a {@link BMap} containing the
     * values of the JSON object. Otherwise a {@link BError} will be thrown.
     */
    public static BMap<BString, ?> jsonToMap(Object json, MapType mapType) {
        return io.ballerina.runtime.JSONUtils.jsonToMap(json, mapType);
    }

    /**
     * Convert a BJSON to a user defined record.
     *
     * @param json       JSON to convert
     * @param structType Type (definition) of the target record
     * @return If the provided JSON is of object-type, this method will return a {@link BMap} containing the
     * values of the JSON object. Otherwise the method will throw a {@link BError}.
     */
    public static BMap<BString, Object> convertJSONToRecord(Object json, StructureType structType) {
        return io.ballerina.runtime.JSONUtils.convertJSONToRecord(json, structType);
    }

    public static Object convertJSON(Object jsonValue, Type targetType) {
        return io.ballerina.runtime.JSONUtils.convertJSON(jsonValue, targetType);
    }

    public static Object convertUnionTypeToJSON(Object source, JSONType targetType) {
        return io.ballerina.runtime.JSONUtils.convertUnionTypeToJSON(source, targetType);
    }

    /**
     * Represents the modes which process numeric values while converting a string to JSON.
     */
    public enum NonStringValueProcessingMode {
        /**
         * FROM_JSON_STRING converts a numeric value that
         * - starts with the negative sign (-) and is numerically equal to zero (0) to `-0.0f`
         * - is syntactically an integer to an `int`
         * - doesn't belong to the above to decimal.
         */
        FROM_JSON_STRING,

        /**
         * FROM_JSON_FLOAT_STRING converts all numerical values to float.
         */
        FROM_JSON_FLOAT_STRING,

        /**
         * FROM_JSON_DECIMAL_STRING converts all numerical values to decimal.
         */
        FROM_JSON_DECIMAL_STRING
    }

}
