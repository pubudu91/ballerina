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

import ballerina/math;
import ballerina/runtime;

# Derived set of configurations from the `RetryConfig`.
#
# + count - Number of retry attempts before giving up
# + intervalInMillis - Retry interval in milliseconds
# + backOffFactor - Multiplier of the retry interval to exponentially increase retry interval
# + maxWaitIntervalInMillis - Maximum time of the retry interval in milliseconds
# + statusCodes - HTTP response status codes which are considered as failures
public type RetryInferredConfig record {|
    int count = 0;
    int intervalInMillis = 0;
    float backOffFactor = 0.0;
    int maxWaitIntervalInMillis = 0;
    boolean[] statusCodes = [];
|};

# Provides the HTTP remote functions for interacting with an HTTP endpoint. This is created by wrapping the HTTP client
# to provide retrying over HTTP requests.
#
# + url - Target service url
# + config - HTTP ClientConfiguration to be used for HTTP client invocation
# + retryInferredConfig - Derived set of configurations associated with retry
# + httpClient - Chain of different HTTP clients which provides the capability for initiating contact with a remote
#                HTTP service in resilient manner.
public type RetryClient client object {

    public string url;
    public ClientConfiguration config;
    public RetryInferredConfig retryInferredConfig;
    public HttpClient httpClient;

    # Provides the HTTP remote functions for interacting with an HTTP endpoint. This is created by wrapping the HTTP
    # client to provide retrying over HTTP requests.
    #
    # + url - Target service url
    # + config - HTTP ClientConfiguration to be used for HTTP client invocation
    # + retryInferredConfig - Derived set of configurations associated with retry
    # + httpClient - HTTP client for outbound HTTP requests
    public function __init(string url, ClientConfiguration config, RetryInferredConfig retryInferredConfig,
                                        HttpClient httpClient) {
        self.url = url;
        self.config = config;
        self.retryInferredConfig = retryInferredConfig;
        self.httpClient = httpClient;
    }

    # The `post()` function wraps the underlying HTTP remote functions in a way to provide
    # retrying functionality for a given endpoint to recover from network level failures.
    #
    # + path - Resource path
    # + message - An HTTP outbound request message or any payload of type `string`, `xml`, `json`, `byte[]`,
    #             `io:ReadableByteChannel` or `mime:Entity[]`
    # + return - The HTTP `Response` message, or an error if the invocation fails
    public remote function post(string path, RequestMessage message) returns Response|ClientError {
        var result = performRetryAction(path, <Request>message, HTTP_POST, self);
        if (result is HttpFuture) {
            return getInvalidTypeError();
        } else {
            return result;
        }
    }

    # The `head()` function wraps the underlying HTTP remote functions in a way to provide
    # retrying functionality for a given endpoint to recover from network level failures.
    #
    # + path - Resource path
    # + message - An HTTP outbound request message or any payload of type `string`, `xml`, `json`, `byte[]`,
    #             `io:ReadableByteChannel` or `mime:Entity[]`
    # + return - The HTTP `Response` message, or an error if the invocation fails
    public remote function head(string path, public RequestMessage message = ()) returns Response|ClientError {
        var result = performRetryAction(path, <Request>message, HTTP_HEAD, self);
        if (result is HttpFuture) {
            return getInvalidTypeError();
        } else {
            return result;
        }
    }

    # The `put()` function wraps the underlying HTTP remote function in a way to provide
    # retrying functionality for a given endpoint to recover from network level failures.
    #
    # + path - Resource path
    # + message - An HTTP outbound request message or any payload of type `string`, `xml`, `json`, `byte[]`,
    #             `io:ReadableByteChannel` or `mime:Entity[]`
    # + return - The HTTP `Response` message, or an error if the invocation fails
    public remote function put(string path, RequestMessage message) returns Response|ClientError {
        var result = performRetryAction(path, <Request>message, HTTP_PUT, self);
        if (result is HttpFuture) {
            return getInvalidTypeError();
        } else {
            return result;
        }
    }

    # The `forward()` function wraps the underlying HTTP remote function in a way to provide retrying functionality
    # for a given endpoint with inbound request's HTTP verb to recover from network level failures.
    #
    # + path - Resource path
    # + request - An HTTP inbound request message
    # + return - The HTTP `Response` message, or an error if the invocation fails
    public remote function forward(string path, Request request) returns Response|ClientError {
        var result = performRetryAction(path, request, HTTP_FORWARD, self);
        if (result is HttpFuture) {
            return getInvalidTypeError();
        } else {
            return result;
        }
    }

    # The `execute()` sends an HTTP request to a service with the specified HTTP verb. The function wraps the
    # underlying HTTP remote function in a way to provide retrying functionality for a given endpoint to recover
    # from network level failures.
    #
    # + httpVerb - The HTTP verb value
    # + path - Resource path
    # + message - An HTTP outbound request message or any payload of type `string`, `xml`, `json`, `byte[]`,
    #             `io:ReadableByteChannel` or `mime:Entity[]`
    # + return - The HTTP `Response` message, or an error if the invocation fails
    public remote function execute(string httpVerb, string path, RequestMessage message) returns Response|ClientError {
        var result = performRetryClientExecuteAction(path, <Request>message, httpVerb, self);
        if (result is HttpFuture) {
            return getInvalidTypeError();
        } else {
            return result;
        }
    }

    # The `patch()` function wraps the underlying HTTP remote function in a way to provide
    # retrying functionality for a given endpoint to recover from network level failures.
    #
    # + path - Resource path
    # + message - An HTTP outbound request message or any payload of type `string`, `xml`, `json`, `byte[]`,
    #             `io:ReadableByteChannel` or `mime:Entity[]`
    # + return - The HTTP `Response` message, or an error if the invocation fails
    public remote function patch(string path, RequestMessage message) returns Response|ClientError {
        var result = performRetryAction(path, <Request>message, HTTP_PATCH, self);
        if (result is HttpFuture) {
            return getInvalidTypeError();
        } else {
            return result;
        }
    }

    # The `delete()` function wraps the underlying HTTP remote function in a way to provide
    # retrying functionality for a given endpoint to recover from network level failures.
    #
    # + path - Resource path
    # + message - An HTTP outbound request message or any payload of type `string`, `xml`, `json`, `byte[]`,
    #             `io:ReadableByteChannel` or `mime:Entity[]`
    # + return - The HTTP `Response` message, or an error if the invocation fails
    public remote function delete(string path, public RequestMessage message = ()) returns Response|ClientError {
        var result = performRetryAction(path, <Request>message, HTTP_DELETE, self);
        if (result is HttpFuture) {
            return getInvalidTypeError();
        } else {
            return result;
        }
    }

    # The `get()` function wraps the underlying HTTP remote function in a way to provide
    # retrying functionality for a given endpoint to recover from network level failures.
    #
    # + path - Resource path
    # + message - An HTTP outbound request message or any payload of type `string`, `xml`, `json`, `byte[]`,
    #             `io:ReadableByteChannel` or `mime:Entity[]`
    # + return - The HTTP `Response` message, or an error if the invocation fails
    public remote function get(string path, public RequestMessage message = ()) returns Response|ClientError {
        var result = performRetryAction(path, <Request>message, HTTP_GET, self);
        if (result is HttpFuture) {
            return getInvalidTypeError();
        } else {
            return result;
        }
    }

    # The `options()` function wraps the underlying HTTP remote function in a way to provide
    # retrying functionality for a given endpoint to recover from network level failures.
    #
    # + path - Resource path
    # + message - An HTTP outbound request message or any payload of type `string`, `xml`, `json`, `byte[]`,
    #             `io:ReadableByteChannel` or `mime:Entity[]`
    # + return - The HTTP `Response` message, or an error if the invocation fails
    public remote function options(string path, public RequestMessage message = ()) returns Response|ClientError {
        var result = performRetryAction(path, <Request>message, HTTP_OPTIONS, self);
        if (result is HttpFuture) {
            return getInvalidTypeError();
        } else {
            return result;
        }
    }

    # Submits an HTTP request to a service with the specified HTTP verb.
	#cThe `submit()` function does not give out a `Response` as the result,
	#crather it returns an `HttpFuture` which can be used to do further interactions with the endpoint.
    #
    # + httpVerb - The HTTP verb value
    # + path - The resource path
    # + message - An HTTP outbound request message or any payload of type `string`, `xml`, `json`, `byte[]`,
    #             `io:ReadableByteChannel` or `mime:Entity[]`
    # + return - An `HttpFuture` that represents an asynchronous service invocation, or an error if the submission fails
    public remote function submit(string httpVerb, string path, RequestMessage message) returns HttpFuture|ClientError {
        var result = performRetryClientExecuteAction(path, <Request>message, HTTP_SUBMIT, self, verb = httpVerb);
        if (result is Response) {
            return getInvalidTypeError();
        } else {
            return result;
        }
    }

    # Retrieves the `Response` for a previously submitted request.
    #
    # + httpFuture - The `HttpFuture` relates to a previous asynchronous invocation
    # + return - An HTTP response message, or an error if the invocation fails
    public remote function getResponse(HttpFuture httpFuture) returns Response|ClientError {
        // We do not need to retry this as we already check the response when submit is called.
        return self.httpClient->getResponse(httpFuture);
    }

    # Checks whether a `PushPromise` exists for a previously submitted request.
    #
    # + httpFuture - The `HttpFuture` relates to a previous asynchronous invocation
    # + return - A `boolean` that represents whether a `PushPromise` exists
    public remote function hasPromise(HttpFuture httpFuture) returns (boolean) {
        return self.httpClient->hasPromise(httpFuture);
    }

    # Retrieves the next available `PushPromise` for a previously submitted request.
    #
    # + httpFuture - The `HttpFuture` relates to a previous asynchronous invocation
    # + return - An HTTP Push Promise message, or an error if the invocation fails
    public remote function getNextPromise(HttpFuture httpFuture) returns PushPromise|ClientError {
        return self.httpClient->getNextPromise(httpFuture);
    }

    # Retrieves the promised server push `Response` message.
    #
    # + promise - The related `PushPromise`
    # + return - A promised HTTP `Response` message, or an error if the invocation fails
    public remote function getPromisedResponse(PushPromise promise) returns Response|ClientError {
        return self.httpClient->getPromisedResponse(promise);
    }

    # Rejects a `PushPromise`.
	# When a `PushPromise` is rejected, there is no chance of fetching a promised response using the rejected promise.
    #
    # + promise - The Push Promise to be rejected
    public remote function rejectPromise(PushPromise promise) {
        return self.httpClient->rejectPromise(promise);
    }
};


// Performs execute remote function of the retry client. extract the corresponding http integer value representation
// of the http verb and invokes the perform action method.
// verb is used for submit methods only.
function performRetryClientExecuteAction(@untainted string path, Request request, @untainted string httpVerb,
                                         RetryClient retryClient, string verb = "") returns HttpResponse|ClientError {
    HttpOperation connectorAction = extractHttpOperation(httpVerb);
    return performRetryAction(path, request, connectorAction, retryClient, verb = verb);
}

// Handles all the actions exposed through the retry client.
function performRetryAction(@untainted string path, Request request, HttpOperation requestAction,
                            RetryClient retryClient, string verb = "") returns HttpResponse|ClientError {
    HttpClient httpClient = retryClient.httpClient;
    int currentRetryCount = 0;
    int retryCount = retryClient.retryInferredConfig.count;
    int intervalInMillis = retryClient.retryInferredConfig.intervalInMillis;
    boolean[] statusCodeIndex = retryClient.retryInferredConfig.statusCodes;
    initializeBackOffFactorAndMaxWaitInterval(retryClient);
    
    string message = "All the retry attempts failed.";
    AllRetryAttemptsFailed retryFailedError = error(ALL_RETRY_ATTEMPTS_FAILED, message = message);
    ClientError httpConnectorErr = retryFailedError;
    Request inRequest = request;
    // When performing passthrough scenarios using retry client, message needs to be built before sending out the
    // to keep the request message to retry.
    var binaryPayload = check inRequest.getBinaryPayload();

    while (currentRetryCount < (retryCount + 1)) {
        inRequest = check populateMultipartRequest(inRequest);
        var backendResponse = invokeEndpoint(path, inRequest, requestAction, httpClient, verb = verb);
        if (backendResponse is Response) {
            int responseStatusCode = backendResponse.statusCode;
            if (statusCodeIndex.length() > responseStatusCode && (statusCodeIndex[responseStatusCode])
                                                              && currentRetryCount < (retryCount)) {
                [intervalInMillis, currentRetryCount] =
                                calculateEffectiveIntervalAndRetryCount(retryClient, currentRetryCount, intervalInMillis);
            } else {
                return backendResponse;
            }
        } else if (backendResponse is HttpFuture) {
            var response = httpClient->getResponse(backendResponse);
            if (response is Response) {
                int responseStatusCode = response.statusCode;
                if (statusCodeIndex.length() > responseStatusCode && (statusCodeIndex[responseStatusCode])
                                                                  && currentRetryCount < (retryCount)) {
                    [intervalInMillis, currentRetryCount] =
                                    calculateEffectiveIntervalAndRetryCount(retryClient, currentRetryCount, intervalInMillis);
                } else {
                    // We return the HttpFuture object as this is called by submit method.
                    return backendResponse;
                }
            } else {
                [intervalInMillis, currentRetryCount] =
                                calculateEffectiveIntervalAndRetryCount(retryClient, currentRetryCount, intervalInMillis);
                httpConnectorErr = response;
            }
        } else {
            [intervalInMillis, currentRetryCount] =
                            calculateEffectiveIntervalAndRetryCount(retryClient, currentRetryCount, intervalInMillis);
            httpConnectorErr = backendResponse;
        }
        runtime:sleep(intervalInMillis);
    }
    return httpConnectorErr;
}

function initializeBackOffFactorAndMaxWaitInterval(RetryClient retryClient) {
    if (retryClient.retryInferredConfig.backOffFactor <= 0.0) {
        retryClient.retryInferredConfig.backOffFactor = 1.0;
    }
    if (retryClient.retryInferredConfig.maxWaitIntervalInMillis == 0) {
        retryClient.retryInferredConfig.maxWaitIntervalInMillis = 60000;
    }
}

function getWaitTime(float backOffFactor, int maxWaitTime, int interval) returns int {
    int waitTime = math:round(interval * backOffFactor);
    waitTime = waitTime > maxWaitTime ? maxWaitTime : waitTime;
    return waitTime;
}

function calculateEffectiveIntervalAndRetryCount(RetryClient retryClient, int currentRetryCount, int currentDelay)
                                                 returns [int, int] {
    int interval = currentDelay;
    if (currentRetryCount != 0) {
        interval = getWaitTime(retryClient.retryInferredConfig.backOffFactor,
                    retryClient.retryInferredConfig.maxWaitIntervalInMillis, interval);
    }
    int retryCount = currentRetryCount + 1;
    return [interval, retryCount];
}
