// Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import ballerina/io;
import ballerina/mime;

# Represents the HTTP/1.0 protocol
const string HTTP_1_0 = "1.0";

# Represents the HTTP/1.1 protocol
const string HTTP_1_1 = "1.1";

# Represents the HTTP/2.0 protocol
const string HTTP_2_0 = "2.0";

# Defines the supported HTTP protocols.
#
# `HTTP_1_0`: HTTP/1.0 protocol
# `HTTP_1_1`: HTTP/1.1 protocol
# `HTTP_2_0`: HTTP/2.0 protocol
public type HttpVersion HTTP_1_0|HTTP_1_1|HTTP_2_0;

# Represents the HTTP protocol scheme
const string HTTP_SCHEME = "http://";

# Represents the HTTPS protocol scheme
const string HTTPS_SCHEME = "https://";

# Constant for the HTTP error code
public const string HTTP_ERROR_CODE = "{ballerina/http}HTTPError";

# Constant for the default listener endpoint timeout
const int DEFAULT_LISTENER_TIMEOUT = 120000; //2 mins

# Constant for the default failover starting index for failover endpoints
const int DEFAULT_FAILOVER_EP_STARTING_INDEX = 0;

# Maximum number of requests that can be processed at a given time on a single connection
const int MAX_PIPELINED_REQUESTS = 10;

# Represents the multipart primary type
public const string MULTIPART_AS_PRIMARY_TYPE = "multipart/";

# Constant for the HTTP FORWARD method
public const HTTP_FORWARD = "FORWARD";

# Constant for the HTTP GET method
public const HTTP_GET = "GET";

# Constant for the HTTP POST method
public const HTTP_POST = "POST";

# Constant for the HTTP DELETE method
public const HTTP_DELETE = "DELETE";

# Constant for the HTTP OPTIONS method
public const HTTP_OPTIONS = "OPTIONS";

# Constant for the HTTP PUT method
public const HTTP_PUT = "PUT";

# Constant for the HTTP PATCH method
public const HTTP_PATCH = "PATCH";

# Constant for the HTTP HEAD method
public const HTTP_HEAD = "HEAD";

# constant for the HTTP SUBMIT method
public const HTTP_SUBMIT = "SUBMIT";

# Constant to identify a none-HTTP operation
public const HTTP_NONE = "NONE";

# Defines the possible values for the chunking configuration in HTTP services and clients.
#
# `AUTO`: If the payload is less than 8KB, content-length header is set in the outbound request/response,
#         Otherwise, the chunking header is set in the outbound request/response
# `ALWAYS`: Always set the chunking header in the response
# `NEVER`: Never set the chunking header even if the payload is larger than 8KB in the outbound request/response
public type Chunking CHUNKING_AUTO|CHUNKING_ALWAYS|CHUNKING_NEVER;

# If the payload is less than 8KB, content-length header is set in the outbound request/response.
# Otherwise, the chunking header is set in the outbound request/response
public const CHUNKING_AUTO = "AUTO";

# Always set the chunking header in the response.
public const CHUNKING_ALWAYS = "ALWAYS";

# Never set the chunking header even if the payload is larger than 8KB in the outbound request/response
public const CHUNKING_NEVER = "NEVER";

# Options to compress using GZIP or DEFLATE.
#
# `AUTO`: When the service behaves as an HTTP gateway, the accept-encoding option of the inbound request/response is
#         set as the accept-encoding/content-encoding option of the outbound request/response
# `ALWAYS`: Always set the accept-encoding/content-encoding in the outbound request/response
# `NEVER`: Never set the accept-encoding/content-encoding header in the outbound request/response
public type Compression COMPRESSION_AUTO|COMPRESSION_ALWAYS|COMPRESSION_NEVER;

# When the service behaves as an HTTP gateway, the accept-encoding option of the inbound request/response is set as the
# accept-encoding/content-encoding option of the outbound request/response
public const COMPRESSION_AUTO = "AUTO";

# Always set the accept-encoding/content-encoding option in the outbound request/response
public const COMPRESSION_ALWAYS = "ALWAYS";

# Never set accept-encoding/content-encoding header in outbound request/response
public const COMPRESSION_NEVER = "NEVER";

# The types of messages that are accepted by the HTTP `client` when sending out the outbound request
public type RequestMessage Request|string|xml|json|byte[]|io:ReadableByteChannel|mime:Entity[]|();

# The types of messages that are accepted by the HTTP `listener` when sending out the outbound response
public type ResponseMessage Response|string|xml|json|byte[]|io:ReadableByteChannel|mime:Entity[]|();

# The type of the user-defined custom record
type CustomRecordType record {| anydata...; |};

# The types of the response payload that are returned by the HTTP `client` after the data binding operation
public type PayloadType string|xml|json|byte[]|CustomRecordType|CustomRecordType[];

# The types of data values that are expected by the HTTP `client` to return after the data binding operation
public type TargetType typedesc<Response|string|xml|json|byte[]|CustomRecordType| CustomRecordType[]>;

# Defines the HTTP operations related to the circuit breaker, failover and load balancer.
#
# `FORWARD`: Forwards the specified payload
# `GET`: Requests a resource
# `POST`: Creates a new resource
# `DELETE`: Deletes the specified resource
# `OPTIONS`: Requests communication options that are available
# `PUT`: Replaces the target resource
# `PATCH`: Applies partial modification to the resource
# `HEAD`: Identical to `GET` but no resource body should be returned
# `SUBMIT`: Submits an HTTP request and returns an `HttpFuture` object
# `NONE`: No operation should be performed
public type HttpOperation HTTP_FORWARD|HTTP_GET|HTTP_POST|HTTP_DELETE|HTTP_OPTIONS|HTTP_PUT|HTTP_PATCH|HTTP_HEAD
                                                                                                |HTTP_SUBMIT|HTTP_NONE;

# Common type used for the `HttpFuture` and Response used for resiliency clients
type HttpResponse Response|HttpFuture;

# A record for configuring the SSL/TLS protocol and version to be used.
#
# + name - SSL Protocol to be used (e.g., TLS1.2)
# + versions - SSL/TLS protocols to be enabled (e.g., TLSv1,TLSv1.1,TLSv1.2)
public type Protocols record {|
    string name = "";
    string[] versions = [];
|};

# A record for providing configurations for certificate revocation status checks.
#
# + enable - The status of `validateCertEnabled`
# + cacheSize - Maximum size of the cache
# + cacheValidityPeriod - The time period during which a cache entry is valid
public type ValidateCert record {|
    boolean enable = false;
    int cacheSize = 0;
    int cacheValidityPeriod = 0;
|};

# A record for providing configurations for certificate revocation status checks.
#
# + enable - The status of OCSP stapling
# + cacheSize - Maximum size of the cache
# + cacheValidityPeriod - The time period during which a cache entry is valid
public type ListenerOcspStapling record {|
    boolean enable = false;
    int cacheSize = 0;
    int cacheValidityPeriod = 0;
|};

# A record for providing configurations for content compression.
#
# + enable - The status of the compression
# + contentTypes - Content types, which are allowed for compression
public type CompressionConfig record {|
    Compression enable = COMPRESSION_AUTO;
    string[] contentTypes = [];
|};

type HTTPError record {
    string message = "";
};

# Common client configurations for the next level clients.
#
# + httpVersion - The HTTP version understood by the client
# + http1Settings - Configurations related to the HTTP/1.x protocol
# + http2Settings - Configurations related to the HTTP/2 protocol
# + timeoutInMillis - The maximum time to wait (in milliseconds) for a response before closing the connection
# + forwarded - The choice of setting the `forwarded`/`x-forwarded` header
# + followRedirects - Configurations associated with the Redirection
# + poolConfig - Configurations associated with request pooling
# + cache - HTTP caching related configurations
# + compression - Specifies the way of handling the compression (`accept-encoding`) header
# + auth - HTTP authentication-related configurations
# + circuitBreaker - Configurations associated with the behaviour of the Circuit Breaker
# + retryConfig - Configurations associated with retrying
# + cookieConfig - Configurations associated with the cookies
public type CommonClientConfiguration record {|
    string httpVersion = HTTP_1_1;
    ClientHttp1Settings http1Settings = {};
    ClientHttp2Settings http2Settings = {};
    int timeoutInMillis = 60000;
    string forwarded = "disable";
    FollowRedirects? followRedirects = ();
    PoolConfiguration? poolConfig = ();
    CacheConfig cache = {};
    Compression compression = COMPRESSION_AUTO;
    OutboundAuthConfig? auth = ();
    CircuitBreakerConfig? circuitBreaker = ();
    RetryConfig? retryConfig = ();
    CookieConfig? cookieConfig = ();
|};
