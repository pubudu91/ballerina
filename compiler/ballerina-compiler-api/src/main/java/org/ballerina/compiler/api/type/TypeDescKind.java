/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerina.compiler.api.type;

import java.util.Arrays;

/**
 * Represents the Type Kinds.
 * 
 * @since 2.0.0
 */
public enum TypeDescKind {
    ARRAY("array"),
    OBJECT("object"),
    RECORD("record"),
    MAP("map"),
    ERROR("error"),
    SIMPLE("simple"),
    OTHER("other"),
    FUNCTION("function"),
    BUILTIN("builtin"),
    NIL("nil"),
    TUPLE("tuple"),
    STREAM("stream"),
    FUTURE("future"),
    TYPEDESC("typeDesc"),
    TYPE_REFERENCE("typeReference"),
    UNION("union"),
    INT("int"),
    BYTE("byte"),
    FLOAT("float"),
    DECIMAL("decimal"),
    STRING("string"),
    BOOLEAN("boolean"),
    JSON("json"),
    XML("xml");
    
    private String name;

    TypeDescKind(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
    
    public static TypeDescKind getFromName(String name) {
        return Arrays.stream(TypeDescKind.values())
                .filter(typeDescKind -> typeDescKind.getName().equals(name))
                .findFirst()
                .orElse(null);
    }
}