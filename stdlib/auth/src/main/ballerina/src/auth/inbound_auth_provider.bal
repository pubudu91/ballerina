// Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

# Represents the inbound Auth provider. Any type of implementation such as JWT, OAuth2, LDAP, JDBC, file-based etc.
# should be object-wise similar.
public type InboundAuthProvider abstract object {

    # Authenticate with credential value passed.
    #
    # + credential - Credential value
    # + return - True if authentication is a success, else false or `Error` if any error occurred
    public function authenticate(string credential) returns boolean|Error;
};
