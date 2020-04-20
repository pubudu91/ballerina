/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.net.http;

import io.netty.handler.codec.http.HttpHeaderNames;
import org.ballerinalang.jvm.types.BArrayType;
import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.types.TypeTags;
import org.ballerinalang.jvm.util.exceptions.BallerinaConnectorException;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.ArrayValueImpl;
import org.ballerinalang.jvm.values.ErrorValue;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.XMLValue;
import org.ballerinalang.langlib.typedesc.ConstructFrom;
import org.ballerinalang.mime.util.EntityBodyHandler;
import org.ballerinalang.net.uri.URIUtil;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.ballerinalang.net.http.HttpConstants.DEFAULT_HOST;
import static org.ballerinalang.net.http.compiler.ResourceValidator.COMPULSORY_PARAM_COUNT;

/**
 * {@code HttpDispatcher} is responsible for dispatching incoming http requests to the correct resource.
 *
 * @since 0.94
 */
public class HttpDispatcher {

    public static HttpService findService(HTTPServicesRegistry servicesRegistry, HttpCarbonMessage inboundReqMsg) {
        try {
            Map<String, HttpService> servicesOnInterface;
            List<String> sortedServiceURIs;
            String hostName = inboundReqMsg.getHeader(HttpHeaderNames.HOST.toString());

            if (hostName != null && servicesRegistry.getServicesMapHolder(hostName) != null) {
                servicesOnInterface = servicesRegistry.getServicesByHost(hostName);
                sortedServiceURIs = servicesRegistry.getSortedServiceURIsByHost(hostName);
            } else if (servicesRegistry.getServicesMapHolder(DEFAULT_HOST) != null) {
                servicesOnInterface = servicesRegistry.getServicesByHost(DEFAULT_HOST);
                sortedServiceURIs = servicesRegistry.getSortedServiceURIsByHost(DEFAULT_HOST);
            } else {
                inboundReqMsg.setHttpStatusCode(404);
                String localAddress = inboundReqMsg.getProperty(HttpConstants.LOCAL_ADDRESS).toString();
                throw new BallerinaConnectorException("no service has registered for listener : " + localAddress);
            }

            String rawUri = (String) inboundReqMsg.getProperty(HttpConstants.TO);
            inboundReqMsg.setProperty(HttpConstants.RAW_URI, rawUri);
            Map<String, Map<String, String>> matrixParams = new HashMap<>();
            String uriWithoutMatrixParams = URIUtil.extractMatrixParams(rawUri, matrixParams);

            inboundReqMsg.setProperty(HttpConstants.TO, uriWithoutMatrixParams);
            inboundReqMsg.setProperty(HttpConstants.MATRIX_PARAMS, matrixParams);

            URI validatedUri = getValidatedURI(uriWithoutMatrixParams);

            String basePath = servicesRegistry.findTheMostSpecificBasePath(validatedUri.getRawPath(),
                                                                           servicesOnInterface, sortedServiceURIs);

            if (basePath == null) {
                inboundReqMsg.setHttpStatusCode(404);
                throw new BallerinaConnectorException("no matching service found for path : "
                                                              + validatedUri.getRawPath());
            }

            HttpService service = servicesOnInterface.get(basePath);
            setInboundReqProperties(inboundReqMsg, validatedUri, basePath);
            return service;
        } catch (Exception e) {
            throw new BallerinaConnectorException(e.getMessage());
        }
    }

    private static void setInboundReqProperties(HttpCarbonMessage inboundReqMsg, URI requestUri, String basePath) {
        String subPath = URIUtil.getSubPath(requestUri.getRawPath(), basePath);
        inboundReqMsg.setProperty(HttpConstants.BASE_PATH, basePath);
        inboundReqMsg.setProperty(HttpConstants.SUB_PATH, subPath);
        inboundReqMsg.setProperty(HttpConstants.QUERY_STR, requestUri.getQuery());
        //store query params comes with request as it is
        inboundReqMsg.setProperty(HttpConstants.RAW_QUERY_STR, requestUri.getRawQuery());
    }

    public static URI getValidatedURI(String uriStr) {
        URI requestUri;
        try {
            requestUri = URI.create(uriStr);
        } catch (IllegalArgumentException e) {
            throw new BallerinaConnectorException(e.getMessage());
        }
        return requestUri;
    }

    /**
     * This method finds the matching resource for the incoming request.
     *
     * @param servicesRegistry HTTP service registry
     * @param inboundMessage   incoming message.
     * @return matching resource.
     */
    public static HttpResource findResource(HTTPServicesRegistry servicesRegistry, HttpCarbonMessage inboundMessage) {
        String protocol = (String) inboundMessage.getProperty(HttpConstants.PROTOCOL);
        if (protocol == null) {
            throw new BallerinaConnectorException("protocol not defined in the incoming request");
        }

        try {
            // Find the Service TODO can be improved
            HttpService service = HttpDispatcher.findService(servicesRegistry, inboundMessage);
            if (service == null) {
                throw new BallerinaConnectorException("no Service found to handle the service request");
                // Finer details of the errors are thrown from the dispatcher itself, Ideally we shouldn't get here.
            }

            // Find the Resource
            return HttpResourceDispatcher.findResource(service, inboundMessage);
        } catch (Exception e) {
            throw new BallerinaConnectorException(e.getMessage());
        }
    }

    public static Object[] getSignatureParameters(HttpResource httpResource, HttpCarbonMessage httpCarbonMessage,
                                                  MapValue endpointConfig) {
        ObjectValue httpCaller = ValueCreatorUtils.createCallerObject();
        ObjectValue inRequest = ValueCreatorUtils.createRequestObject();
        ObjectValue inRequestEntity = ValueCreatorUtils.createEntityObject();

        HttpUtil.enrichHttpCallerWithConnectionInfo(httpCaller, httpCarbonMessage, httpResource, endpointConfig);
        HttpUtil.enrichHttpCallerWithNativeData(httpCaller, httpCarbonMessage, endpointConfig);

        HttpUtil.populateInboundRequest(inRequest, inRequestEntity, httpCarbonMessage);

        SignatureParams signatureParams = httpResource.getSignatureParams();
        int signatureParamCount = signatureParams.getSignatureParamTypes().size();
        Object[] paramValues = new Object[signatureParamCount * 2];
        int paramIndex = 0;
        paramValues[paramIndex++] = httpCaller;
        paramValues[paramIndex++] = true;
        paramValues[paramIndex++] = inRequest;
        paramValues[paramIndex] = true;

        if (signatureParamCount == COMPULSORY_PARAM_COUNT) {
            return paramValues;
        }

        try {
            populatePathParams(httpCarbonMessage, signatureParams, paramValues);
            populateQueryParams(httpCarbonMessage, signatureParams, paramValues);
            populateBodyParam(inRequest, inRequestEntity, signatureParams, paramValues);
        } catch (BallerinaConnectorException exception) {
            httpCarbonMessage.setHttpStatusCode(Integer.parseInt(HttpConstants.HTTP_BAD_REQUEST));
            throw exception;
        }
        return paramValues;
    }

    private static void populatePathParams(HttpCarbonMessage httpCarbonMessage, SignatureParams signatureParams,
                                           Object[] paramValues) {
        HttpResourceArguments resourceArgumentValues =
                (HttpResourceArguments) httpCarbonMessage.getProperty(HttpConstants.RESOURCE_ARGS);

        Map<String, Integer> pathParamOrder = signatureParams.getPathParamOrder();
        for (String paramName : pathParamOrder.keySet()) {
            String argumentValue = resourceArgumentValues.getMap().get(paramName);
            try {
                argumentValue = URLDecoder.decode(argumentValue, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                // we can simply ignore and send the value to application and let the
                // application deal with the value.
            }
            int actualPathParamIndex = pathParamOrder.get(paramName);
            int paramIndex = actualPathParamIndex * 2;
            BType signatureParamType = signatureParams.getSignatureParamTypes().get(actualPathParamIndex);
            try {
                switch (signatureParamType.getTag()) {
                    case TypeTags.INT_TAG:
                        paramValues[paramIndex++] = Long.parseLong(argumentValue);
                        break;
                    case TypeTags.FLOAT_TAG:
                        paramValues[paramIndex++] = Double.parseDouble(argumentValue);
                        break;
                    case TypeTags.BOOLEAN_TAG:
                        paramValues[paramIndex++] = Boolean.parseBoolean(argumentValue);
                        break;
                    default:
                        paramValues[paramIndex++] = argumentValue;
                }
                paramValues[paramIndex] = true;
            } catch (Exception ex) {
                throw new BallerinaConnectorException(
                        "path param value casting failed for '" + argumentValue + "' : " + ex.getMessage());
            }
        }
    }

    private static void populateQueryParams(HttpCarbonMessage httpCarbonMessage, SignatureParams signatureParams,
                                            Object[] paramValues) {
        Map<String, Integer> queryParamOrder = signatureParams.getQueryParamOrder();
        if (queryParamOrder.size() == 0) {
            return;
        }
        MapValue<String, Object> queryParams = signatureParams
                .getQueryParams(httpCarbonMessage.getProperty(HttpConstants.RAW_QUERY_STR));

        for (String paramName : queryParamOrder.keySet()) {
            int actualQueryParamIndex = queryParamOrder.get(paramName);
            int paramIndex = actualQueryParamIndex * 2;
            BType signatureParamType = signatureParams.getSignatureParamTypes().get(actualQueryParamIndex);
            try {
                Object queryValue = queryParams.get(paramName);
                if (queryValue == null) { //TODO can we set empty value instead responding 400
                    throw new BallerinaConnectorException("no query value found for `" + paramName + "`");
                }
                paramValues[paramIndex++] = signatureParamType.getTag() == TypeTags.ARRAY_TAG ? queryValue :
                        ((ArrayValueImpl) queryValue).getString(0);
                paramValues[paramIndex] = true;
            } catch (Exception ex) {
                throw new BallerinaConnectorException("query param retrieval failed : " + ex.getMessage());
            }
        }
    }

    private static void populateBodyParam(ObjectValue inRequest, ObjectValue inRequestEntity,
                                          SignatureParams signatureParams, Object[] paramValues) {
        if (!signatureParams.isBodyParamAvailable()) {
            return;
        }
        int actualBodyParamIndex = signatureParams.getBodyParamOrderIndex();
        int paramIndex = actualBodyParamIndex * 2;
        BType bodyParamType = signatureParams.getSignatureParamTypes().get(actualBodyParamIndex);
        try {
            paramValues[paramIndex++] = populateAndGetEntityBody(inRequest, inRequestEntity, bodyParamType);
            paramValues[paramIndex] = true;
        } catch (Exception ex) {
            throw new BallerinaConnectorException("data binding failed: " + ex.getMessage());
        }
    }

    private static Object populateAndGetEntityBody(ObjectValue inRequest, ObjectValue inRequestEntity,
                                                   org.ballerinalang.jvm.types.BType entityBodyType)
            throws IOException {
        HttpUtil.populateEntityBody(inRequest, inRequestEntity, true, true);
        try {
            switch (entityBodyType.getTag()) {
                case TypeTags.STRING_TAG:
                    String stringDataSource = EntityBodyHandler.constructStringDataSource(inRequestEntity);
                    EntityBodyHandler.addMessageDataSource(inRequestEntity, stringDataSource);
                    return stringDataSource;
                case TypeTags.JSON_TAG:
                    Object bjson = EntityBodyHandler.constructJsonDataSource(inRequestEntity);
                    EntityBodyHandler.addJsonMessageDataSource(inRequestEntity, bjson);
                    return bjson;
                case TypeTags.XML_TAG:
                    XMLValue bxml = EntityBodyHandler.constructXmlDataSource(inRequestEntity);
                    EntityBodyHandler.addMessageDataSource(inRequestEntity, bxml);
                    return bxml;
                case TypeTags.ARRAY_TAG:
                    if (((BArrayType) entityBodyType).getElementType().getTag() == TypeTags.BYTE_TAG) {
                        ArrayValue blobDataSource = EntityBodyHandler.constructBlobDataSource(inRequestEntity);
                        EntityBodyHandler.addMessageDataSource(inRequestEntity, blobDataSource);
                        return blobDataSource;
                    } else if (((BArrayType) entityBodyType).getElementType().getTag() == TypeTags.RECORD_TYPE_TAG) {
                        return getRecordEntity(inRequestEntity, entityBodyType);
                    } else {
                        throw new BallerinaConnectorException("Incompatible Element type found inside an array " +
                                ((BArrayType) entityBodyType).getElementType().getName());
                    }
                case TypeTags.RECORD_TYPE_TAG:
                    return getRecordEntity(inRequestEntity, entityBodyType);
                default:
                        //Do nothing
            }
        } catch (ErrorValue ex) {
            throw new BallerinaConnectorException(ex.toString());
        }
        return null;
    }

    private static Object getRecordEntity(ObjectValue inRequestEntity, BType entityBodyType) {
        Object result = getRecord(entityBodyType, getBJsonValue(inRequestEntity));
        if (result instanceof ErrorValue) {
            throw (ErrorValue) result;
        }
        return result;
    }

    /**
     * Convert a json to the relevant record type.
     *
     * @param entityBodyType Represents entity body type
     * @param bjson          Represents the json value that needs to be converted
     * @return the relevant ballerina record or object
     */
    private static Object getRecord(BType entityBodyType, Object bjson) {
        try {
            return ConstructFrom.convert(entityBodyType, bjson);
        } catch (NullPointerException ex) {
            throw new BallerinaConnectorException("cannot convert payload to record type: " + entityBodyType.getName());
        }
    }

    /**
     * Given an inbound request entity construct the ballerina json.
     *
     * @param inRequestEntity Represents inbound request entity
     * @return a ballerina json value
     */
    private static Object getBJsonValue(ObjectValue inRequestEntity) {
        Object bjson = EntityBodyHandler.constructJsonDataSource(inRequestEntity);
        EntityBodyHandler.addJsonMessageDataSource(inRequestEntity, bjson);
        return bjson;
    }

    public static boolean shouldDiffer(HttpResource httpResource) {
        return (httpResource != null && httpResource.getSignatureParams().isBodyParamAvailable());
    }

    private HttpDispatcher() {
    }
}
