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

import ballerina/config;
import ballerina/crypto;
import ballerina/encoding;
import ballerina/http;
import ballerina/io;
import ballerina/log;
import ballerina/mime;
import ballerina/stringutils;

# Intent verification request parameter `hub.challenge` representing the challenge that needs to be echoed by
# susbscribers to verify intent.
const string HUB_CHALLENGE = "hub.challenge";

# Parameter `hub.mode` representing the mode of the request from hub to subscriber or subscriber to hub.
const string HUB_MODE = "hub.mode";

# Subscription change or intent verification request parameter `hub.topic` representing the topic relevant to the for
# which the request is initiated.
const string HUB_TOPIC = "hub.topic";

# Subscription change request parameter `hub.callback` representing the callback to which notification should happen.
const string HUB_CALLBACK = "hub.callback";

# Subscription request parameter `hub.lease_seconds` representing the period for which the subscription is expected to
# be active.
const string HUB_LEASE_SECONDS = "hub.lease_seconds";

# Subscription parameter `hub.secret` representing the secret key to use for authenticated content distribution.
const string HUB_SECRET = "hub.secret";

# `hub.mode` value indicating "subscription" mode, to subscribe to updates for a topic.
const string MODE_SUBSCRIBE = "subscribe";

# `hub.mode` value indicating "unsubscription" mode, to unsubscribe to updates for a topic.
const string MODE_UNSUBSCRIBE = "unsubscribe";

const string X_HUB_SIGNATURE = "X-Hub-Signature";

///////////////////////////////// Ballerina WebSub specific constants /////////////////////////////////
# `hub.mode` value indicating "publish" mode, used by a publisher to notify an update to a topic.
const string MODE_PUBLISH = "publish";

# `hub.mode` value indicating "register" mode, used by a publisher to register a topic at a hub.
const string MODE_REGISTER = "register";

# `hub.mode` value indicating "unregister" mode, used by a publisher to unregister a topic at a hub.
const string MODE_UNREGISTER = "unregister";

const string REMOTE_PUBLISHING_MODE_DIRECT = "direct";
const string REMOTE_PUBLISHING_MODE_FETCH = "fetch";

const string X_HUB_UUID = "X-Hub-Uuid";
const string X_HUB_TOPIC = "X-Hub-Topic";

const string CONTENT_TYPE = "Content-Type";

const string ANN_NAME_WEBSUB_SUBSCRIBER_SERVICE_CONFIG = "SubscriberServiceConfig";
const ANNOT_FIELD_TARGET = "target";
const ANNOT_FIELD_CALLBACK = "callback";
const ANNOT_FIELD_LEASE_SECONDS = "leaseSeconds";
const ANNOT_FIELD_SECRET = "secret";
const ANNOT_FIELD_SUBSCRIBE_ON_STARTUP = "subscribeOnStartUp";
const ANNOT_FIELD_HUB_CLIENT_CONFIG = "hubClientConfig";
const ANNOT_FIELD_PUBLISHER_CLIENT_CONFIG = "publisherClientConfig";

const string WEBSUB_MODULE_NAME = "ballerina/websub";

# The constant used to represent error code of WebSub module.
public const string WEBSUB_ERROR_CODE = "{ballerina/websub}WebSubError";

# The identifier to be used to identify the mode in which update content should be identified.
public type RemotePublishMode PUBLISH_MODE_DIRECT|PUBLISH_MODE_FETCH;

# `RemotePublishMode` indicating direct update content notification (fat-ping). The payload of the update
# notification request from the publisher to the hub would include be the update content.
public const PUBLISH_MODE_DIRECT = "PUBLISH_MODE_DIRECT";

# `RemotePublishMode` indicating that once the publisher notifies the hub that an update is available, the hub
# needs to fetch the topic URL to identify the update content.
public const PUBLISH_MODE_FETCH = "PUBLISH_MODE_FETCH";

# The identifier to be used to identify the cryptographic hash algorithm.
public type SignatureMethod SHA1|SHA256;

# The constant used to represent SHA-1 cryptographic hash algorithm
public const string SHA1 = "SHA1";

# The constant used to represent SHA-256 cryptographic hash algorithm
public const string SHA256 = "SHA256";

///////////////////////////////// Custom Webhook/Extension specific constants /////////////////////////////////
# The identifier to be used to identify the topic for dispatching with custom subscriber services.
public type TopicIdentifier TOPIC_ID_HEADER|TOPIC_ID_PAYLOAD_KEY|TOPIC_ID_HEADER_AND_PAYLOAD;

# `TopicIdentifier` indicating dispatching based solely on a header of the request.
public const TOPIC_ID_HEADER = "TOPIC_ID_HEADER";

# `TopicIdentifier` indicating dispatching based solely on a value for a key in the JSON payload of the request.
public const TOPIC_ID_PAYLOAD_KEY = "TOPIC_ID_PAYLOAD_KEY";

# `TopicIdentifier` indicating dispatching based on a combination of header and values specified for a key/key(s) in
# the JSON payload of the request.
public const TOPIC_ID_HEADER_AND_PAYLOAD = "TOPIC_ID_HEADER_AND_PAYLOAD";

///////////////////////////////////////////////////////////////////
//////////////////// WebSub Subscriber Commons ////////////////////
///////////////////////////////////////////////////////////////////
# Object representing an intent verification request received.
#
# + mode - The mode specified in the intent verification request, subscription or unsubscription
# + topic - The topic for which intent is verified to subscribe/unsubscribe
# + challenge - The challenge to be echoed to verify intent to subscribe/unsubscribe
# + leaseSeconds - The lease seconds period for which a subscription will be active if intent verification
#                  is being done for subscription
# + request - The HTTP request received for intent verification
public type IntentVerificationRequest object {

    public string mode = "";
    public string topic = "";
    public string challenge = "";
    public int leaseSeconds = 0;
    public http:Request request = new;

    # Builds the response for the request, verifying intention to subscribe, if the topic matches that expected.
    #
    # + expectedTopic - The topic for which subscription should be accepted
    # + return - `http:Response` The response to the hub verifying/denying intent to subscribe
    public function buildSubscriptionVerificationResponse(string expectedTopic) returns http:Response {
        return buildIntentVerificationResponse(self, MODE_SUBSCRIBE, expectedTopic);
    }

    # Builds the response for the request, verifying intention to unsubscribe, if the topic matches that expected.
    #
    # + expectedTopic - The topic for which unsubscription should be accepted
    # + return - `http:Response` The response to the hub verifying/denying intent to unsubscribe
    public function buildUnsubscriptionVerificationResponse(string expectedTopic) returns http:Response {
        return buildIntentVerificationResponse(self, MODE_UNSUBSCRIBE, expectedTopic);
    }
};

# Function to build intent verification response for subscription/unsubscription requests sent.
#
# + intentVerificationRequest - The intent verification request from the hub
# + mode - The mode (subscription/unsubscription) for which a request was sent
# + topic - The intended topic for which subscription change should be verified
# + return - `http:Response` The response to the hub verifying/denying intent to subscripe/unsubscribe
function buildIntentVerificationResponse(IntentVerificationRequest intentVerificationRequest, string mode,
                                         string topic)
    returns http:Response {

    http:Response response = new;
    var decodedTopic = encoding:decodeUriComponent(intentVerificationRequest.topic, "UTF-8");
    string reqTopic = decodedTopic is string ? decodedTopic : topic;

    string reqMode = intentVerificationRequest.mode;
    string challenge = <@untainted>intentVerificationRequest.challenge;

    if (reqMode == mode && reqTopic == topic) {
        response.statusCode = http:STATUS_ACCEPTED;
        response.setTextPayload(challenge);
    } else {
        response.statusCode = http:STATUS_NOT_FOUND;
    }
    return response;
}

# Function to build the data source and validate the signature for requests received at the callback.
#
# + request - The request received
# + serviceType - The service for which the request was rceived
# + return - `error`, if an error occurred in extraction or signature validation failed
function processWebSubNotification(http:Request request, service serviceType) returns @tainted error? {
    SubscriberServiceConfiguration? subscriberConfig = retrieveSubscriberServiceAnnotations(serviceType);
    string secret = subscriberConfig?.secret ?: "";
    // Build the data source before responding to the content delivery requests automatically
    var payload = request.getTextPayload();

    if (!request.hasHeader(X_HUB_SIGNATURE)) {
        if (secret != "") {
            error webSubError = error(WEBSUB_ERROR_CODE, message = X_HUB_SIGNATURE +
                                        " header not present for subscription added specifying " + HUB_SECRET);
            return webSubError;
        }
        return;
    }

    string xHubSignature = request.getHeader(X_HUB_SIGNATURE);
    if (secret == "" && xHubSignature != "") {
        log:printWarn("Ignoring " + X_HUB_SIGNATURE + " value since secret is not specified.");
        return;
    }

    if (payload is string) {
        return validateSignature(xHubSignature, payload, secret);
    } else {
        error err = payload;
        string errCause = <string> err.detail()?.message;
        error webSubError = error(WEBSUB_ERROR_CODE, message = "Error extracting notification payload as string " +
                                            "for signature validation: " + errCause);
        return webSubError;
    }
}

# Function to validate the signature header included in the notification.
#
# + xHubSignature - The X-Hub-Signature header included in the notification request from the hub
# + stringPayload - The string representation of the notification payload received
# + secret - The secret used when subscribing
# + return - `error` if an error occurs validating the signature or the signature is invalid
function validateSignature(string xHubSignature, string stringPayload, string secret) returns error? {
    string[] splitSignature = stringutils:split(xHubSignature, "=");
    string method = splitSignature[0];
    string signature = stringutils:replace(xHubSignature, method + "=", "");
    string generatedSignature = "";

    if (stringutils:equalsIgnoreCase(method, SHA1)) {
        generatedSignature = crypto:hmacSha1(stringPayload.toBytes(), secret.toBytes()).toBase16();
    } else if (stringutils:equalsIgnoreCase(method, SHA256)) {
        generatedSignature = crypto:hmacSha256(stringPayload.toBytes(), secret.toBytes()).toBase16();
    } else {
        error webSubError = error(WEBSUB_ERROR_CODE, message = "Unsupported signature method: " + method);
        return webSubError;
    }

    if (!stringutils:equalsIgnoreCase(signature, generatedSignature)) {
        error webSubError = error(WEBSUB_ERROR_CODE, message = "Signature validation failed: Invalid Signature!");
        return webSubError;
    }
    return;
}

# Object representing the WebSub Content Delivery Request received.
#
# + request - The HTTP POST request received as the notification
public type Notification object {

    private http:Request request = new;

    # Retrieves the query parameters of the content delivery request as a map.
    #
    # + return - String-constrained array map of the query params
    public function getQueryParams() returns map<string[]> {
        return self.request.getQueryParams();
    }

    # Retrieves the `Entity` associated with the content delivery request.
    #
    # + return - The `Entity` of the request. An `error` is returned, if entity construction fails
    public function getEntity() returns mime:Entity|error {
        return self.request.getEntity();
    }

    # Returns whether the requested header key exists in the header map of the content delivery request.
    #
    # + headerName - The header name
    # + return - Returns true if the specified header key exists
    public function hasHeader(string headerName) returns boolean {
        return self.request.hasHeader(headerName);
    }

    # Returns the value of the specified header. If the specified header key maps to multiple values, the first of
    # these values is returned.
    #
    # + headerName - The header name
    # + return - The first header value for the specified header name. An exception is thrown if no header is found.
    #            Ideally `hasHeader()` needs to be used to check the existence of header initially.
    public function getHeader(string headerName) returns @tainted string {
        return self.request.getHeader(headerName);
    }

    # Retrieves all the header values to which the specified header key maps to.
    #
    # + headerName - The header name
    # + return - The header values the specified header key maps to. An exception is thrown if no header is found.
    #            Ideally `hasHeader()` needs to be used to check the existence of header initially.
    public function getHeaders(string headerName) returns @tainted string[] {
        return self.request.getHeaders(headerName);
    }

    # Retrieves all the names of the headers present in the content delivery request.
    #
    # + return - An array of all the header names
    public function getHeaderNames() returns @tainted string[] {
        return self.request.getHeaderNames();
    }

    # Retrieves the type of the payload of the content delivery request (i.e: the `content-type` header value).
    #
    # + return - Returns the `content-type` header value as a string
    public function getContentType() returns @tainted string {
        return self.request.getContentType();
    }

    # Extracts `json` payload from the content delivery request.
    #
    # + return - The `json` payload or `error` in case of errors. If the content type is not JSON, an `error` is returned.
    public function getJsonPayload() returns @tainted json|error {
        return self.request.getJsonPayload();
    }

    # Extracts `xml` payload from the content delivery request.
    #
    # + return - The `xml` payload or `error` in case of errors. If the content type is not XML, an `error` is returned.
    public function getXmlPayload() returns @tainted xml|error {
        return self.request.getXmlPayload();
    }

    # Extracts `text` payload from the content delivery request.
    #
    # + return - The `text` payload or `error` in case of errors.
    #            If the content type is not of type text, an `error` is returned.
    public function getTextPayload() returns @tainted string|error {
        return self.request.getTextPayload();
    }

    # Retrieves the request payload as a `ByteChannel` except in the case of multiparts.
    #
    # + return - A byte channel from which the message payload can be read or `error` in case of errors
    public function getByteChannel() returns @tainted io:ReadableByteChannel|error {
        return self.request.getByteChannel();
    }

    # Retrieves the request payload as a `byte[]`.
    #
    # + return - The byte[] representation of the message payload or `error` in case of errors
    public function getBinaryPayload() returns @tainted byte[]|error {
        return self.request.getBinaryPayload();
    }

    # Retrieves the form parameters from the content delivery request as a `map`.
    #
    # + return - The map of form params or `error` in case of errors
    public function getFormParams() returns @tainted map<string>|error {
        return self.request.getFormParams();
    }
};

# Function to retrieve hub and topic URLs from the `http:response` from a publisher to a discovery request.
#
# + response - The `http:Response` received
# + return - `(topic, hubs)` if parsing and extraction is successful, `error` if not
public function extractTopicAndHubUrls(http:Response response) returns @tainted [string, string[]]|error {
    string[] linkHeaders = [];
    if (response.hasHeader("Link")) {
        linkHeaders = response.getHeaders("Link");
    }

    if (linkHeaders.length() == 0) {
        error websubError = error(WEBSUB_ERROR_CODE, message = "Link header unavailable in discovery response");
        return websubError;
    }

    int hubIndex = 0;
    string[] hubs = [];
    string topic = "";
    string[] linkHeaderConstituents = [];
    if (linkHeaders.length() == 1) {
        linkHeaderConstituents = stringutils:split(linkHeaders[0], ",");
    } else {
        linkHeaderConstituents = linkHeaders;
    }

    foreach var link in linkHeaderConstituents {
        string[] linkConstituents = stringutils:split(link, ";");
        if (linkConstituents[1] != "") {
            string url = linkConstituents[0].trim();
            url = stringutils:replace(url, "<", "");
            url = stringutils:replace(url, ">", "");
            if (stringutils:contains(linkConstituents[1], "rel=\"hub\"")) {
                hubs[hubIndex] = url;
                hubIndex += 1;
            } else if (stringutils:contains(linkConstituents[1], "rel=\"self\"")) {
                if (topic != "") {
                    error websubError = error(WEBSUB_ERROR_CODE, message = "Link Header contains > 1 self URLs");
                    return websubError;
                } else {
                    topic = url;
                }
            }
        }
    }

    if (hubs.length() > 0 && topic != "") {
        return [topic, hubs];
    }
    error websubError = error(WEBSUB_ERROR_CODE,
                            message = "Hub and/or Topic URL(s) not identified in link header of discovery response");
    return websubError;
}

# Record representing a WebSub subscription change request.
#
# + topic - The topic for which the subscription/unsubscription request is sent
# + callback - The callback which should be registered/unregistered for the subscription/unsubscription request sent
# + leaseSeconds - The lease period for which the subscription is expected to be active
# + secret - The secret to be used for authenticated content distribution with this subscription
public type SubscriptionChangeRequest record {|
    string topic = "";
    string callback = "";
    int leaseSeconds = 0;
    string secret = "";
|};

# Record representing subscription/unsubscription details if a subscription/unsubscription request is successful.
#
# + hub - The hub at which the subscription/unsubscription was successful
# + topic - The topic for which the subscription/unsubscription was successful
# + response - The response from the hub to the subscription/unsubscription request
public type SubscriptionChangeResponse record {|
    string hub = "";
    string topic = "";
    http:Response response;
|};

/////////////////////////////////////////////////////////////
//////////////////// WebSub Hub Commons /////////////////////
/////////////////////////////////////////////////////////////
# Record representing hub specific configurations.
#
# + leaseSeconds - The default lease seconds value to honour if not specified in subscription requests
# + signatureMethod - The signature method to use for authenticated content delivery (`SHA1`|`SHA256`)
# + remotePublish - The record representing configuration related to remote publishing allowance
# + topicRegistrationRequired - Whether a topic needs to be registered at the hub prior to publishing/subscribing
#                               to the topic
# + publicUrl - The URL for the hub to be included in content delivery requests, defaults to
#               `http(s)://localhost:{port}/websub/hub` if unspecified
# + clientConfig - The configuration for the hub to communicate with remote HTTP endpoints
# + hubPersistenceStore - The `HubPersistenceStore` to use to persist hub data
public type HubConfiguration record {|
    int leaseSeconds = 86400;
    SignatureMethod signatureMethod = SHA256;
    RemotePublishConfig remotePublish?;
    boolean topicRegistrationRequired = true;
    string publicUrl?;
    http:ClientConfiguration clientConfig?;
    HubPersistenceStore hubPersistenceStore?;
|};

# Record representing remote publishing allowance.
#
# + enabled - Whether remote publishers should be allowed to publish to this hub (HTTP requests)
# + mode - If remote publishing is allowed, the mode to use, `direct` (default) - fat ping with
#                          the notification payload specified or `fetch` - the hub fetches the topic URL
#                          specified in the "publish" request to identify the payload
public type RemotePublishConfig record {|
    boolean enabled = false;
    RemotePublishMode mode = PUBLISH_MODE_DIRECT;
|};

# Starts up the Ballerina Hub.
#
# + hubServiceListener - The `http:Listener` to which the hub service is attached
# + hubConfiguration - The hub specific configuration
# + return - `WebSubHub` The WebSubHub object representing the newly started up hub, or `HubStartedUpError` indicating
#            that the hub is already started, and including the WebSubHub object representing the
#            already started up hub
public function startHub(http:Listener hubServiceListener, HubConfiguration hubConfiguration = {})
                                                                    returns WebSubHub|HubStartedUpError {
    hubLeaseSeconds = config:getAsInt("b7a.websub.hub.leasetime",
                                      hubConfiguration.leaseSeconds);
    hubSignatureMethod = getSignatureMethod(hubConfiguration.signatureMethod);
    remotePublishConfig = getRemotePublishConfig(hubConfiguration["remotePublish"]);
    hubTopicRegistrationRequired = config:getAsBoolean("b7a.websub.hub.topicregistration",
                                    hubConfiguration.topicRegistrationRequired);

    // reset the hubUrl once the other parameters are set. if url is an empty string, create hub url with listener
    // configs in the native code
    hubPublicUrl = config:getAsString("b7a.websub.hub.url", hubConfiguration["publicUrl"] ?: "");
    hubClientConfig = hubConfiguration["clientConfig"];
    hubPersistenceStoreImpl = hubConfiguration["hubPersistenceStore"];

    if (hubPersistenceStoreImpl is HubPersistenceStore) {
        hubPersistenceEnabled = true;
    }

    startHubService(hubServiceListener);
    return startUpHubService(hubTopicRegistrationRequired, hubPublicUrl, hubServiceListener);
}

# Object representing a Ballerina WebSub Hub.
#
# + hubUrl - The URL of the started up Ballerina WebSub Hub
public type WebSubHub object {

    public string hubUrl;
    private http:Listener hubHttpListener;

    public function __init(string hubUrl, http:Listener hubHttpListener) {
         self.hubUrl = hubUrl;
         self.hubHttpListener = hubHttpListener;
    }

    # Stops the started up Ballerina WebSub Hub.
    #
    # + return - `boolean` indicating whether the internal Ballerina Hub was stopped
    public function stop() returns boolean {
        // TODO: return error
        var stopResult = self.hubHttpListener.__immediateStop();
        return stopHubService(self.hubUrl) && !(stopResult is error);
    }

    # Publishes an update against the topic in the initialized Ballerina Hub.
    #
    # + topic - The topic for which the update should happen
    # + payload - The update payload
    # + contentType - The content type header to set for the request delivering the payload
    # + return - `error` if the hub is not initialized or does not represent the internal hub
    public function publishUpdate(string topic, string|xml|json|byte[]|io:ReadableByteChannel payload,
                                  string? contentType = ()) returns error? {
        if (self.hubUrl == "") {
            error webSubError = error(WEBSUB_ERROR_CODE,
                                    message = "Internal Ballerina Hub not initialized or incorrectly referenced");
            return webSubError;
        }

        WebSubContent content = {};

        if (payload is io:ReadableByteChannel) {
            content.payload = constructByteArray(payload);
        } else {
            content.payload = payload;
        }

        if (contentType is string) {
            content.contentType = contentType;
        } else {
            if (payload is string) {
                content.contentType = mime:TEXT_PLAIN;
            } else if (payload is xml) {
                content.contentType = mime:APPLICATION_XML;
            } else if (payload is json) {
                content.contentType = mime:APPLICATION_JSON;
            } else {
                content.contentType = mime:APPLICATION_OCTET_STREAM;
            }
        }

        return validateAndPublishToInternalHub(self.hubUrl, topic, content);
    }

    # Registers a topic in the Ballerina Hub.
    #
    # + topic - The topic to register
    # + return - `error` if an error occurred with registration
    public function registerTopic(string topic) returns error? {
        if (!hubTopicRegistrationRequired) {
            error e = error(WEBSUB_ERROR_CODE, message = "Topic registration not allowed/not required at the Hub");
            return e;
        }
        return registerTopicAtHub(topic);
    }

    # Unregisters a topic in the Ballerina Hub.
    #
    # + topic - The topic to unregister
    # + return - `error` if an error occurred with unregistration
    public function unregisterTopic(string topic) returns error? {
        if (!hubTopicRegistrationRequired) {
            error e = error(WEBSUB_ERROR_CODE, message = "Topic unregistration not allowed/not required at the Hub");
            return e;
        }
        return unregisterTopicAtHub(topic);
    }

    # Retrieves topics currently recognized by the Hub.
    #
    # + return - An array of available topics
    public function getAvailableTopics() returns string[] = external;

    # Retrieves details of subscribers registered to receive updates for a particular topic.
    #
    # + topic - The topic for which details need to be retrieved
    # + return - An array of subscriber details
    public function getSubscribers(string topic) returns SubscriberDetails[] = external;
};

///////////////////////////////////////////////////////////////////
//////////////////// WebSub Publisher Commons /////////////////////
///////////////////////////////////////////////////////////////////
# Function to add link headers to a response to allow WebSub discovery.
#
# + response - The response being sent
# + hubs - The hubs the publisher advertises as the hubs that it publishes updates to
# + topic - The topic to which subscribers need to subscribe to, to receive updates for the resource
public function addWebSubLinkHeader(http:Response response, string[] hubs, string topic) {
    string hubLinkHeader = "";
    foreach var hub in hubs {
        hubLinkHeader = hubLinkHeader + "<" + hub + ">; rel=\"hub\", ";
    }
    response.setHeader("Link", hubLinkHeader + "<" + topic + ">; rel=\"self\"");
}

# Record to represent persisted Subscription Details retrieved.
#
# + topic - The topic for which the subscription is added
# + callback - The callback specified for the particular subscription
# + secret - The secret to be used for authenticated content distribution
# + leaseSeconds - The lease second period specified for the particular subscription
# + createdAt - The time at which the subscription was created
public type SubscriptionDetails record {|
    string topic = "";
    string callback = "";
    string secret = "";
    int leaseSeconds = 0;
    int createdAt = 0;
|};

function retrieveSubscriberServiceAnnotations(service serviceType) returns SubscriberServiceConfiguration? {
    typedesc<any> serviceTypedesc = typeof serviceType;
    return serviceTypedesc.@SubscriberServiceConfig;
}

# Record to represent a WebSub content delivery.
#
# + payload - The payload to be sent
# + contentType - The content-type of the payload
type WebSubContent record {|
    string|xml|json|byte[]|io:ReadableByteChannel payload = "";
    string contentType = "";
|};

function isSuccessStatusCode(int statusCode) returns boolean {
    return (200 <= statusCode && statusCode < 300);
}

# Error to represent that a WebSubHub is already started up, encapsulating the started up Hub.
#
# + message - The error message
# + cause - The cause of the `HubStartedUpError`, if available
# + startedUpHub - The `WebSubHub` object representing the started up Hub
public type HubStartedUpError record {|
    string message = "";
    error? cause = ();
    WebSubHub startedUpHub;
|};

# Record to represent Subscriber Details.
#
# + callback - The callback specified for the particular subscription
# + leaseSeconds - The lease second period specified for the particular subscription
# + createdAt - The time at which the subscription was created
public type SubscriberDetails record {|
    string callback = "";
    int leaseSeconds = 0;
    int createdAt = 0;
|};

type WebSubError record {
    string message = "";
};

