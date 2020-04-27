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
package org.ballerina.compiler.api.types;

import org.ballerina.compiler.api.model.ModuleID;
import org.ballerinalang.model.elements.PackageID;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

/**
 * Represents an stream type descriptor.
 * 
 * @since 1.3.0
 */
public class StreamTypeDescriptor extends BallerinaTypeDesc {
    private List<TypeDescriptor> typeParameters;
    
    private StreamTypeDescriptor(TypeDescKind typeDescKind,
                                 ModuleID moduleID,
                                 List<TypeDescriptor> typeParameters) {
        super(typeDescKind, moduleID);
        this.typeParameters = typeParameters;
    }
    
    public List<TypeDescriptor> getTypeParameters() {
        return this.typeParameters;
    }

    @Override
    public String getSignature() {
        String memberSignature;
        if (this.getTypeParameters().isEmpty()) {
            memberSignature = "()";
        } else {
            StringJoiner joiner = new StringJoiner(", ");
            this.getTypeParameters().forEach(typeDescriptor -> joiner.add(typeDescriptor.getSignature()));
            memberSignature = joiner.toString();
        }
        return "stream<" + memberSignature + ">";
    }

    /**
     * Represents Future Type Descriptor Builder.
     */
    public static class FutureTypeBuilder extends TypeBuilder<FutureTypeBuilder> {
        private List<TypeDescriptor> typeParameters = new ArrayList<>();

        /**
         * Symbol Builder Constructor.
         *
         * @param typeDescKind type descriptor kind
         * @param moduleID     Module ID of the type descriptor
         */
        public FutureTypeBuilder(TypeDescKind typeDescKind, PackageID moduleID) {
            super(typeDescKind, moduleID);
        }

        /**
         * Build the Ballerina Type Descriptor.
         *
         * @return {@link TypeDescriptor} built
         */
        public TypeDescriptor build() {
            return new StreamTypeDescriptor(this.typeDescKind,
                    this.moduleID,
                    this.typeParameters);
        }

        public FutureTypeBuilder withTypeParameter(TypeDescriptor memberType) {
            this.typeParameters.add(memberType);
            return this;
        }
    }
}
