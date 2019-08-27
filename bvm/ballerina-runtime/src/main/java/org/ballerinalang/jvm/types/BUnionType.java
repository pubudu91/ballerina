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
package org.ballerinalang.jvm.types;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * {@code BUnionType} represents a union type in Ballerina.
 *
 * @since 0.995.0
 */
public class BUnionType extends BType {

    private List<BType> memberTypes;
    private boolean nullable;

    /**
     * Create a {@code BUnionType} which represents the union type.
     */
    public BUnionType() {
        super(null, null, Object.class);
    }

    /**
     * Create a {@code BUnionType} which represents the union type.
     *
     * @param memberTypes of the union type
     */
    public BUnionType(List<BType> memberTypes) {
        super(null, null, Object.class);
        this.memberTypes = memberTypes;
        this.nullable = checkNillable(memberTypes);
    }

    public BUnionType(BType[] memberTypes) {
        this(Arrays.asList(memberTypes));
    }

    public List<BType> getMemberTypes() {
        return memberTypes;
    }

    public boolean isNullable() {
        return nullable;
    }

    @Override
    public <V extends Object> V getZeroValue() {
        if (nullable || memberTypes.stream().anyMatch(BType::isNilable)) {
            return null;
        }

        return memberTypes.get(0).getZeroValue();
    }

    @Override
    public <V extends Object> V getEmptyValue() {
        if (nullable || memberTypes.stream().anyMatch(BType::isNilable)) {
            return null;
        }

        return memberTypes.get(0).getEmptyValue();
    }

    @Override
    public int getTag() {
        return TypeTags.UNION_TAG;
    }

    @Override
    public String toString() {
        List<String> list = memberTypes.stream().map(BType::toString).collect(Collectors.toList());
        return String.join("|", list);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BUnionType)) {
            return false;
        }
        BUnionType that = (BUnionType) o;
        return memberTypes.containsAll(that.memberTypes) && that.memberTypes.containsAll(memberTypes);
    }

    @Override
    public String getName() {
        return toString();
    }

    @Override
    public int hashCode() {

        return Objects.hash(super.hashCode(), memberTypes);
    }

    public boolean isNilable() {
        return nullable;
    }

    private boolean checkNillable(List<BType> memberTypes) {
        boolean nillable = false;
        for (BType t : memberTypes) {
            if (t.isNilable()) {
                nillable = true;
                break;
            }
        }
        return nillable;
    }
}
