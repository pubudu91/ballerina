## Module Overview

This module provides functions to encapsulate multiple body parts, such as attachments in a single message. The
 communication of such messages follow the MIME (Multipurpose Internet Mail Extensions) specification as specified in
  the [RFC 2045 standard](https://www.ietf.org/rfc/rfc2045.txt).

> Entity refers to the header fields and the content of a message, or a part of the body in a multipart entity. 

### Modify and retrieve the data in an entity
The module provides functions to set and get an entity body from different kinds of message types, such as XML, text, JSON, blob, and body parts. Headers can be modified through functions such as `addHeader()`, `setHeader()`, `removeHeader()`, etc. 
## Samples
### Handle multipart request
The sample service given below handles a multipart request. It extracts the body content from each part, converts it to a `string`, and sends a response.

``` ballerina
import ballerina/http;
import ballerina/log;
import ballerina/mime;

@http:ServiceConfig {
    basePath: "/test"
}
service test on new http:Listener(9090) {

    @http:ResourceConfig {
        methods: ["POST"],
        path: "/multipleparts"
    }
    // The resource that handles multipart requests.
    resource function multipartResource(http:Caller caller, http:Request request) {
        http:Response response = new;

        // Get the body parts from the request.
        var bodyParts = request.getBodyParts();
        if (bodyParts is mime:Entity[]) {
            string content = "";
            // Iterate through each body part and handle the content.
            foreach var part in bodyParts {
                content = content + " -- " + handleContent(part);
            }
            response.setPayload(<@untainted> content);
        } else {
            // If there is an error while getting the body parts, set the 
            // response code as 500 and set the error message as the 
            // response message.
            response.statusCode = 500;
            response.setPayload(<@untainted> bodyParts.reason());
        }

        var result = caller->respond(response);
        if (result is error) {
            log:printError("Error in responding", err = result);
        }
    }
}

// The function that handles the content based on the body part type.
function handleContent(mime:Entity bodyPart) returns @tainted string {
    var mediaType = mime:getMediaType(bodyPart.getContentType());
    if (mediaType is mime:MediaType) {
        // Get the base type of the specific body part.
        string baseType = mediaType.getBaseType();
        // If the base type is ‘application/xml’ or ‘text/xml’, get the XML 
        // content from body part.
        if (mime:APPLICATION_XML == baseType || mime:TEXT_XML == baseType) {
            var payload = bodyPart.getXml();
            if (payload is xml) {
                return payload.getTextValue();
            } else {
                return "Error in parsing xml payload";
            }
        } else if (mime:APPLICATION_JSON == baseType) {
            // If the base type is ‘application/json’, get the JSON content 
            // from body part.
            var payload = bodyPart.getJson();
            if (payload is json) {
                return payload.toJsonString();
            } else {
                return "Error in parsing json payload";
            }
        }
    } else {
        return mediaType.reason();
    }
    return "";
}
```

The sample request that is sent to the above service is shown below.

```
curl -v -F "request={\"param1\": \"value1\"};type=application/json" -F "language=ballerina;type=text/plain" -F "upload=@/home/path-to-file/encode.txt;type=application/octet-stream"  http://localhost:9090/test/multipleparts -H "Expect:"
```
### Create a multipart request
Following code snippet creates a multipart request. It includes two body parts with `application/json` and `application/xml` content type.

``` ballerina
// Create a JSON body part.
mime:Entity bodyPart1 = new;
// Set the JSON content.
bodyPart1.setJson({"bodyPart": "jsonPart"});

// Create another body part using an XML file.
mime:Entity bodyPart2 = new;
bodyPart2.setFileAsEntityBody("ballerina/mime/file.xml", contentType = mime:APPLICATION_XML);

//Create an array to hold all the body parts.
mime:Entity[] bodyParts = [bodyPart1, bodyPart2];

// Set the body parts to the outbound response.
http:Request outRequest = new;
// Set the content type as ‘multipart/mixed’ and set the body parts.
outRequest.setBodyParts(bodyParts, contentType = mime:MULTIPART_MIXED);
```
