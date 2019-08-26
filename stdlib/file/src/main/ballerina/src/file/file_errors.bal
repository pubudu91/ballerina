// Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

# Represents the details of an error.
#
# + message - The error message.
# + cause - Cause of the error.
type Detail record {
    string message;
    error cause?;
};

public const INVALID_OPERATION_ERROR = "{ballerina/file}InvalidOperationError";

public type InvalidOperationError error<INVALID_OPERATION_ERROR, Detail>;

public const PERMISSION_ERROR = "{ballerina/file}PermissionError";

public type PermissionError error<PERMISSION_ERROR, Detail>;

public const FILE_SYSTEM_ERROR = "{ballerina/file}FileSystemError";

public type FileSystemError error<FILE_SYSTEM_ERROR, Detail>;

public const FILE_NOT_FOUND_ERROR = "{ballerina/file}FileNotFoundError";

public type FileNotFoundError error<FILE_NOT_FOUND_ERROR, Detail>;

public type Error InvalidOperationError|PermissionError|FileSystemError|FileNotFoundError;
