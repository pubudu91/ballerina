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
package org.ballerina.compiler.api.symbol;

import org.ballerina.compiler.api.element.DocAttachment;
import org.ballerina.compiler.api.type.BallerinaTypeDescriptor;
import org.ballerina.compiler.impl.semantic.TypesFactory;
import org.wso2.ballerinalang.compiler.semantics.model.types.BField;
import org.wso2.ballerinalang.util.Flags;

import java.util.Optional;

/**
 * Represents a field with a name and type.
 *
 * @since 2.0.0
 */
public class BallerinaField {

    private final DocAttachment docAttachment;

    private final BField bField;

    private final BallerinaTypeDescriptor typeDescriptor;

    public BallerinaField(BField bField) {
        this.bField = bField;
        this.typeDescriptor = TypesFactory.getTypeDescriptor(bField.getType());
        this.docAttachment = new DocAttachment(bField.symbol.markdownDocumentation);
    }

    /**
     * Get the field name.
     *
     * @return {@link String} name of the field
     */
    public String name() {
        return this.bField.getName().getValue();
    }

    /**
     * Whether optional field or not.
     *
     * @return {@link Boolean} optional status
     */
    public boolean optional() {
        return (this.bField.type.flags & Flags.OPTIONAL) == Flags.OPTIONAL;
    }

    /**
     * Whether required field or not.
     *
     * @return {@link Boolean} required status
     */
    public boolean required() {
        return (this.bField.type.flags & Flags.REQUIRED) == Flags.REQUIRED;
    }

    /**
     * Get the type descriptor of the field.
     *
     * @return {@link BallerinaTypeDescriptor} of the field
     */
    public BallerinaTypeDescriptor typeDescriptor() {
        return TypesFactory.getTypeDescriptor(this.bField.getType());
    }

    /**
     * Get the documentation attachment.
     *
     * @return {@link Optional} doc attachment of the field
     */
    public Optional<DocAttachment> docAttachment() {
        return Optional.ofNullable(docAttachment);
    }

    /**
     * Get the accessibility modifier if available.
     *
     * @return {@link Optional} accessibility modifier
     */
    public Optional<Qualifier> qualifier() {
        if ((this.bField.symbol.flags & Flags.PUBLIC) == Flags.PUBLIC) {
            return Optional.of(Qualifier.PUBLIC);
        } else if ((this.bField.symbol.flags & Flags.PRIVATE) == Flags.PRIVATE) {
            return Optional.of(Qualifier.PRIVATE);
        }

        return Optional.empty();
    }

    /**
     * Get the signature of the field.
     *
     * @return {@link String} signature
     */
    public String signature() {
        StringBuilder signature = new StringBuilder(this.typeDescriptor.signature() + " " + this.name());
        if (this.optional()) {
            signature.append("?");
        }

        return signature.toString();
    }
}