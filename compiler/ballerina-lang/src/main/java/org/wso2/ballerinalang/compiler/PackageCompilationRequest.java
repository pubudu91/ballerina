/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.ballerinalang.compiler;

import io.ballerina.projects.PackageId;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a package compilation request.
 *
 * @since 2.0.0
 */
public final class PackageCompilationRequest implements CompilationRequest {

    private final UUID id;
    private final Instant timestamp;
    private final PackageId packageId;

    public PackageCompilationRequest(PackageId packageId) {
        this.id = UUID.randomUUID();
        this.timestamp = Instant.now();
        this.packageId = packageId;
    }

    @Override
    public UUID getId() {
        return this.id;
    }

    @Override
    public Instant getTimestamp() {
        return this.timestamp;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof PackageCompilationRequest)) {
            return false;
        }

        PackageCompilationRequest that = (PackageCompilationRequest) obj;
        return Objects.equals(this.id, that.id) && Objects.equals(this.timestamp, that.timestamp)
                && Objects.equals(this.packageId, that.packageId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id, this.timestamp, this.packageId);
    }

    @Override
    public String toString() {
        return String.format("PackageCompilationRequest{%s, %s, %s}", this.packageId, this.id, this.timestamp);
    }
}
