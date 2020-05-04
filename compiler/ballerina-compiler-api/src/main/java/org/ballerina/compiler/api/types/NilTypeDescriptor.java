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

/**
 * Represents the nil type descriptor.
 * 
 * @since 1.3.0
 */
public class NilTypeDescriptor extends BallerinaTypeDesc {
    
    private NilTypeDescriptor(ModuleID moduleID) {
        super(TypeDescKind.NIL, moduleID);
    }

    @Override
    public String getSignature() {
        return "()";
    }

    /**
     * Represents the builder for Builtin Type Descriptor.
     */
    public static class NilTypeBuilder extends TypeBuilder<NilTypeBuilder> {

        /**
         * Symbol Builder Constructor.
         *
         * @param moduleID     Module ID of the type descriptor
         */
        public NilTypeBuilder(PackageID moduleID) {
            super(TypeDescKind.NIL, moduleID);
        }

        /**
         * Build the Ballerina Type Descriptor.
         *
         * @return {@link TypeDescriptor} built
         */
        public NilTypeDescriptor build() {
            return new NilTypeDescriptor(this.moduleID);
        }
    }
}
