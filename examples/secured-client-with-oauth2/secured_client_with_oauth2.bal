import ballerina/http;
import ballerina/log;
import ballerina/oauth2;

// Define the OAuth2 client endpoint to call the backend services.
// The OAuth2 authentication with client credentials grant type is enabled by
// creating an `oauth2:OutboundOAuth2Provider` with the relevant configurations
// passed as a record.
oauth2:OutboundOAuth2Provider oauth2Provider1 = new({
    tokenUrl: "https://bitbucket.org/site/oauth2/access_token",
    clientId: "mMNWS9PLmM93V5WHjC",
    clientSecret: "jLY6xPY3ER4bNTspaGu6fb7kahhs7kUa",
    clientConfig: {
        secureSocket: {
            trustStore: {
                 path: "${ballerina.home}/bre/security/ballerinaTruststore.p12",
                 password: "ballerina"
            }
        }
    }
});
http:BearerAuthHandler oauth2Handler1 = new(oauth2Provider1);

http:Client clientEP1 = new("https://api.bitbucket.org/2.0", {
    auth: {
        authHandler: oauth2Handler1
    },
    secureSocket: {
        trustStore: {
             path: "${ballerina.home}/bre/security/ballerinaTruststore.p12",
             password: "ballerina"
        }
    }
});

// Defines the OAuth2 client endpoint to call the backend services.
// The OAuth2 authentication with the password grant type is enabled by
// creating an `oauth2:OutboundOAuth2Provider` with the relevant
// configurations passed as a record. If the access token expires or
// becomes invalid, then it will be automatically refreshed with the provided
// `refreshConfig`.
oauth2:OutboundOAuth2Provider oauth2Provider2 = new({
    tokenUrl: "https://bitbucket.org/site/oauth2/access_token",
    username: "b7a.demo@gmail.com",
    password: "ballerina",
    clientId: "mMNWS9PLmM93V5WHjC",
    clientSecret: "jLY6xPY3ER4bNTspaGu6fb7kahhs7kUa",
    clientConfig: {
        secureSocket: {
            trustStore: {
                 path: "${ballerina.home}/bre/security/ballerinaTruststore.p12",
                 password: "ballerina"
            }
        }
    },
    refreshConfig: {
        refreshUrl: "https://bitbucket.org/site/oauth2/access_token",
        clientConfig: {
            secureSocket: {
                trustStore: {
                     path: "${ballerina.home}/bre/security/ballerinaTruststore.p12",
                     password: "ballerina"
                }
            }
        }
    }
});
http:BearerAuthHandler oauth2Handler2 = new(oauth2Provider2);

http:Client clientEP2 = new("https://api.bitbucket.org/2.0", {
    auth: {
        authHandler: oauth2Handler2
    },
    secureSocket: {
        trustStore: {
            path: "${ballerina.home}/bre/security/ballerinaTruststore.p12",
            password: "ballerina"
        }
    }
});

// Defines the OAuth2 client endpoint to call the backend services.
// The OAuth2 authentication with direct token mode is enabled by creating
// an `oauth2:OutboundOAuth2Provider` with the relevant configurations passed
// as a record. If the `accessToken` is invalid or not provided, it will
// be automatically refreshed with the provided `refreshConfig`.
oauth2:OutboundOAuth2Provider oauth2Provider3 = new({
    accessToken: "ya29.GlvQBkqJS0yn0zsZm4IIUUzLk3DH1rRiCMKnHiz6deycKmTFiDsuoFlFfrmXF8dCb0gyzLyXpnv3VcrIlauj3nMs61CbydaAqMl6RwVIU2r2qg1StVVvxRWT9_Or",
    refreshConfig: {
        clientId: "506144513496-dqm5vdqfrfhdjjom10rmvafb8e3h7rtm.apps.googleusercontent.com",
        clientSecret: "3hw2XN4MfiIRrv6mghX6m5gM",
        refreshToken: "1/UwH3YyYccKTrH9bqj35Y7hMYTK9f3HEC3uzlrleFwPE",
        refreshUrl: "https://www.googleapis.com/oauth2/v4/token",
        clientConfig: {
            secureSocket: {
                trustStore: {
                     path: "${ballerina.home}/bre/security/ballerinaTruststore.p12",
                     password: "ballerina"
                }
            }
        }
    }
});
http:BearerAuthHandler oauth2Handler3 = new(oauth2Provider3);

http:Client clientEP3 = new("https://www.googleapis.com/tasks/v1", {
    auth: {
        authHandler: oauth2Handler3
    },
    secureSocket: {
        trustStore: {
            path: "${ballerina.home}/bre/security/ballerinaTruststore.p12",
            password: "ballerina"
        }
    }
});

public function main() {
    // Sends a `GET` request to the specified endpoint.
    var response1 = clientEP1->get("/repositories/b7ademo");
    if (response1 is http:Response) {
        var result = response1.getJsonPayload();
        if (result is json) {
            var values = result.values;
            if (values is json[]) {
                var uuid = values[0].uuid;
                if (uuid is json) {
                    log:printInfo(uuid.toJsonString());
                }
            }
        } else {
            log:printError("Failed to retrieve payload for clientEP1.");
        }
    } else {
        log:printError("Failed to call the endpoint from clientEP1.", err = response1);
    }

    // Send a `GET` request to the specified endpoint.
    var response2 = clientEP2->get("/repositories/b7ademo");
    if (response2 is http:Response) {
        var result = response2.getJsonPayload();
        if (result is json) {
            var values = result.values;
            if (values is json[]) {
                var uuid = values[0].uuid;
                if (uuid is json) {
                    log:printInfo(uuid.toJsonString());
                }
            }
        } else {
            log:printError("Failed to retrieve payload for clientEP2.");
        }
    } else {
        log:printError("Failed to call the endpoint from clientEP2.", err = response2);
    }

    // Send a `GET` request to the specified endpoint.
    var response3 = clientEP3->get("/users/@me/lists");
    if (response3 is http:Response) {
        var result = response3.getJsonPayload();
        log:printInfo((result is error) ?
                                    "Failed to retrieve payload for clientEP3."
                                    : <string> result.kind);
    } else {
        log:printError("Failed to call the endpoint from clientEP3.", err = response3);
    }
}
