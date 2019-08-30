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

import ballerina/crypto;
import ballerina/encoding;

# Represents authentication provider configurations that supports generating JWT for client interactions.
#
# + username - JWT token username
# + issuer - JWT token issuer
# + audience - JWT token audience
# + customClaims - Map of custom claims
# + expTimeInSeconds - Expiry time in seconds
# + keyStoreConfig - JWT key store configurations
# + signingAlg - Signing algorithm
public type JwtIssuerConfig record {|
    string username?;
    string issuer;
    string[] audience;
    map<json> customClaims?;
    int expTimeInSeconds = 300;
    JwtKeyStoreConfig keyStoreConfig;
    JwtSigningAlgorithm signingAlg = RS256;
|};

# Represents JWT key store configurations.
#
# + keyStore - Keystore to be used in JWT signing
# + keyAlias - Signing key alias
# + keyPassword - Signing key password
public type JwtKeyStoreConfig record {|
    crypto:KeyStore keyStore;
    string keyAlias;
    string keyPassword;
|};

# Issue a JWT token based on provided header and payload. JWT will be signed (JWS) if `keyStore` information is provided
# in the `JwtKeyStoreConfig` and the `alg` field of `JwtHeader` is not `NONE`.
#
# + header - JwtHeader object
# + payload - JwtPayload object
# + config - JWT key store config record
# + return - JWT token string or an `Error` if token validation fails
public function issueJwt(JwtHeader header, JwtPayload payload, JwtKeyStoreConfig? config) returns string|Error {
    string jwtHeader = check buildHeaderString(header);
    string jwtPayload = check buildPayloadString(payload);
    string jwtAssertion = jwtHeader + "." + jwtPayload;
    JwtSigningAlgorithm? alg = header?.alg;
    if (alg is JwtSigningAlgorithm) {
        if (alg == NONE) {
            return jwtAssertion;
        } else {
            if (config is JwtKeyStoreConfig) {
                crypto:KeyStore keyStore = config.keyStore;
                string keyAlias = config.keyAlias;
                string keyPassword = config.keyPassword;
                var privateKey = crypto:decodePrivateKey(keyStore, keyAlias, keyPassword);
                if (privateKey is crypto:PrivateKey) {
                    if (alg == RS256) {
                        var signature = crypto:signRsaSha256(jwtAssertion.toBytes(), privateKey);
                        if (signature is byte[]) {
                            return (jwtAssertion + "." + encoding:encodeBase64Url(signature));
                        } else {
                            return prepareError("Private key signing failed for SHA256 algorithm.", signature);
                        }
                    } else if (alg == RS384) {
                        var signature = crypto:signRsaSha384(jwtAssertion.toBytes(), privateKey);
                        if (signature is byte[]) {
                            return (jwtAssertion + "." + encoding:encodeBase64Url(signature));
                        } else {
                            return prepareError("Private key signing failed for SHA384 algorithm.", signature);
                        }
                    } else if (alg == RS512) {
                        var signature = crypto:signRsaSha512(jwtAssertion.toBytes(), privateKey);
                        if (signature is byte[]) {
                            return (jwtAssertion + "." + encoding:encodeBase64Url(signature));
                        } else {
                            return prepareError("Private key signing failed for SHA512 algorithm.", signature);
                        }
                    } else {
                        return prepareError("Unsupported JWS algorithm.");
                    }
                } else {
                    return prepareError("Private key decoding failed.", privateKey);
                }
            } else {
                return prepareError("Signing JWT requires JwtKeyStoreConfig with keystore information.");
            }
        }
    }
    return prepareError("Failed to issue JWT since signing algorithm is not specified.");
}

# Build the header string from the `JwtHeader` record.
#
# + header - JWT header record to be built as a string
# + return - The header string or an `Error` if building the string fails
public function buildHeaderString(JwtHeader header) returns string|Error {
    map<json> headerJson = {};
    if (!validateMandatoryJwtHeaderFields(header)) {
        return prepareError("Mandatory field signing algorithm (alg) is empty.");
    }
    JwtSigningAlgorithm? alg = header?.alg;
    if (alg is JwtSigningAlgorithm) {
        if (alg == RS256) {
            headerJson[ALG] = "RS256";
        } else if (alg == RS384) {
            headerJson[ALG] = "RS384";
        } else if (alg == RS512) {
            headerJson[ALG] = "RS512";
        } else if (alg == NONE) {
            headerJson[ALG] = "none";
        } else {
            return prepareError("Unsupported JWS algorithm.");
        }
        string? typ = header?.typ;
        if (typ is string) {
            headerJson[TYP] = typ;
        }
        string? cty = header?.cty;
        if (cty is string) {
            headerJson[CTY] = cty;
        }
        string? kid = header?.kid;
        if (kid is string) {
            headerJson[KID] = kid;
        }
    }
    string headerValInString = headerJson.toJsonString();
    string encodedPayload = encoding:encodeBase64Url(headerValInString.toBytes());
    return encodedPayload;
}

# Build the payload string from the `JwtPayload` record.
#
# + payload - JWT payload record to be built as a string
# + return - The payload string or an `Error` if building the string fails
public function buildPayloadString(JwtPayload payload) returns string|Error {
    map<json> payloadJson = {};
    string? sub = payload?.sub;
    if (sub is string) {
        payloadJson[SUB] = sub;
    }
    string? iss = payload?.iss;
    if (iss is string) {
        payloadJson[ISS] = iss;
    }
    int? exp = payload?.exp;
    if (exp is int) {
        payloadJson[EXP] = exp;
    }
    int? iat = payload?.iat;
    if (iat is int) {
        payloadJson[IAT] = iat;
    }
    string? jti = payload?.jti;
    if (jti is string) {
        payloadJson[JTI] = jti;
    }
    var aud = payload?.aud;
    if (aud is string) {
        payloadJson[AUD] = aud;
    } else if (aud is string[]) {
        payloadJson[AUD] = aud;
    }
    int? nbf = payload?.nbf;
    if (nbf is int) {
        payloadJson[NBF] = nbf;
    }
    var customClaims = payload?.customClaims;
    if (customClaims is map<json> && customClaims.length() > 0) {
        payloadJson = appendToMap(customClaims, payloadJson);
    }
    string payloadInString = payloadJson.toJsonString();
    return encoding:encodeBase64Url(payloadInString.toBytes());
}

function appendToMap(map<json> fromMap, map<json> toMap) returns map<json> {
    foreach var key in fromMap.keys() {
        toMap[key] = fromMap[key];
    }
    return toMap;
}
