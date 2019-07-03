/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerinalang.compiler.semantics.model.types;

import org.ballerinalang.model.Name;
import org.ballerinalang.model.types.TypeKind;
import org.wso2.ballerinalang.compiler.semantics.model.TypeVisitor;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.util.TypeDescriptor;

/**
 * @since 0.94
 */
public class BAnyType extends BBuiltInRefType {

    private boolean nullable = true;

    public BAnyType(int tag, BTypeSymbol tsymbol) {
        super(tag, tsymbol);
    }

    public BAnyType(int tag, BTypeSymbol tsymbol, Name name, int flag) {

        super(tag, tsymbol);
        this.name = name;
        this.flags = flag;
    }

    public BAnyType(int tag, BTypeSymbol tsymbol, boolean nullable) {
        super(tag, tsymbol);
        this.nullable = nullable;
    }

    public String getDesc() {
        return TypeDescriptor.SIG_ANY;
    }

    @Override
    public <T, R> R accept(BTypeVisitor<T, R> visitor, T t) {
        return visitor.visit(this, t);
    }

    public boolean isNullable() {
        return nullable;
    }

    @Override
    public TypeKind getKind() {
        return TypeKind.ANY;
    }

    @Override
    public void accept(TypeVisitor visitor) {
        visitor.visit(this);
    }
}
