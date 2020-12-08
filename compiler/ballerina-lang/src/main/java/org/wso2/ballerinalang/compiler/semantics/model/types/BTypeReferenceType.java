/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.ballerinalang.compiler.semantics.model.types;

import org.ballerinalang.model.symbols.Symbol;
import org.ballerinalang.model.types.Type;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.types.TypeReference;
import org.wso2.ballerinalang.compiler.semantics.model.TypeVisitor;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.util.TypeTags;

/**
 * Represents a type-reference type descriptor.
 *
 * @since 2.0.0
 */
public class BTypeReferenceType extends BType implements TypeReference {

    public final BType type;

    public BTypeReferenceType(BTypeSymbol tsymbol) {
        super(TypeTags.TYPE_REFERENCE, tsymbol);
        this.type = tsymbol.type;
        this.flags = tsymbol.flags;
    }

    @Override
    public TypeKind getKind() {
        return TypeKind.TYPE_REFERENCE;
    }

    @Override
    public <T, R> R accept(BTypeVisitor<T, R> visitor, T t) {
        return visitor.visit(this, t);
    }

    @Override
    public void accept(TypeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public boolean isNullable() {
        return this.type.isNullable();
    }

    @Override
    public boolean isAnydata() {
        return this.type.isAnydata();
    }

    @Override
    public boolean isPureType() {
        return this.type.isPureType();
    }

    @Override
    public Type type() {
        return this.type;
    }

    @Override
    public Symbol symbol() {
        return this.tsymbol;
    }

    @Override
    public String toString() {
        return this.tsymbol.name.value;
    }
}
