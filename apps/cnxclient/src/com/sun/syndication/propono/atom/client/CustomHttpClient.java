/*
 * Copyright (C) 2011 The CNX Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.sun.syndication.propono.atom.client;

import org.apache.commons.httpclient.URI;

import com.google.common.io.CharStreams;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import javax.ws.rs.core.Response.Status;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.StatusLine;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;

/**
 * Rome-propono uses commons-httpclient which does not work on AppEngine. So creating a custom HTTP
 * Client which wraps URL Fetch service offered by AppEngine.
 * 
 * @author Arjun Satyapal
 */
public class CustomHttpClient extends HttpClient {
    // Timeout = 30 ms.
    private static final int DEFAULT_CONNECTION_TIMEOUT_IN_MILLIS = 30 * 1000;
    private static final String FIELD_RESPONSE_BODY_NAME = "responseBody";
    private static final String FIELD_RESPONSE_STATUS = "statusLine";
    @SuppressWarnings("rawtypes")
    private final Class parent = HttpMethodBase.class;

    /**
     * This is the Generic executeMethod which will replace all the http-methods and in turn
     * depending on type of method will delegate to individual handlers.
     */
    @Override
    public int executeMethod(HttpMethod method) {
        try {
            String response = null;
            if (method instanceof GetMethod) {
                response = handleGetMethod(method);
            } else if (method instanceof PostMethod) {
                response = handlePostMethod(method);
            } else if (method instanceof PutMethod) {
                response = handlePutMethod(method);
            } else {
                throw new RuntimeException("Method[" + method.getName() + "] not yet implemented.");
            }

            // TODO(arjuns) : Add other methods if required.

            // NOTE(arjuns) : This is bit hacky but works.
            Field responseBodyField = parent.getDeclaredField(FIELD_RESPONSE_BODY_NAME);
            responseBodyField.setAccessible(true);
            responseBodyField.set(method, response.getBytes());

        } catch (Exception e) {
            // TODO(arjuns): Auto-generated catch block
            throw new RuntimeException(e);
        }
        return 0;
    }

    /**
     * This will handle HTTP Put.
     */
    private String handlePutMethod(HttpMethod method) throws MalformedURLException, URIException,
            IOException, ProtocolException, NoSuchFieldException, HttpException,
            IllegalAccessException {
        PutMethod putMethod = (PutMethod) method;
        HttpURLConnection connection = getHttpUrlConnection(method.getURI(), true /*isOutputAllowed*/);
        connection.setRequestMethod("PUT");

        RequestEntity requestEntity = putMethod.getRequestEntity();

        if (requestEntity instanceof MultipartRequestEntity) {
            MultipartRequestEntity multiPartEntity = (MultipartRequestEntity) requestEntity;
            connection.setRequestProperty("Connection", "Keep-Alive");
            connection.setRequestProperty("Content-Type", multiPartEntity.getContentType());
            OutputStream outputStream = connection.getOutputStream();
            multiPartEntity.writeRequest(outputStream);
            outputStream.flush();
            outputStream.close();
        } else if (requestEntity instanceof StringRequestEntity) {
            connection.setRequestProperty("Content-type", "text/xml");
            StringRequestEntity stringEntity = (StringRequestEntity) requestEntity;
            stringEntity.writeRequest(connection.getOutputStream());
        } else {
            throw new RuntimeException("EntityType[" + requestEntity.getClass()
                    + "] is not yet implemented.");
        }

        updateStatusLineInResponseBody(method, connection.getResponseCode());
        return CharStreams.toString(new InputStreamReader(connection.getInputStream()));
    }

    /**
     * This is used to update the StatusLine for a HTTP Response.
     */
    private void updateStatusLineInResponseBody(HttpMethod method, int responseCode)
            throws NoSuchFieldException, HttpException, IllegalAccessException {
        Field responseBodyField = parent.getDeclaredField(FIELD_RESPONSE_STATUS);
        responseBodyField.setAccessible(true);

        StringBuilder statusLineBuilder =
                new StringBuilder("HTTP/1.1 ").append(responseCode).append(" OK");
        StatusLine statusLine = new StatusLine(statusLineBuilder.toString());
        responseBodyField.set(method, statusLine);
    }

    /**
     * This will handle HTTP Post method.
     */
    private String handlePostMethod(HttpMethod method) throws MalformedURLException, URIException,
            IOException, ProtocolException, NoSuchFieldException, HttpException,
            IllegalAccessException {
        PostMethod postMethod = (PostMethod) method;
        HttpURLConnection connection = getHttpUrlConnection(method.getURI(), true /*isOutputAllowed*/);

        // TODO(arjuns): reuse for put.
        connection.setRequestMethod("POST");

        for (NameValuePair currParam : postMethod.getParameters()) {
            connection.setRequestProperty(currParam.getName(), currParam.getValue());
        }

        for (Header currHeader : postMethod.getRequestHeaders()) {
            connection.setRequestProperty(currHeader.getName(), currHeader.getValue());
        }

        RequestEntity requestEntity = postMethod.getRequestEntity();

        if (requestEntity instanceof MultipartRequestEntity) {
            MultipartRequestEntity multiPartEntity = (MultipartRequestEntity) requestEntity;
            connection.setRequestProperty("Connection", "Keep-Alive");
            connection.setRequestProperty("Content-Type", multiPartEntity.getContentType());
            OutputStream outputStream = connection.getOutputStream();
            multiPartEntity.writeRequest(outputStream);
            outputStream.flush();
            outputStream.close();
        } else if (requestEntity instanceof StringRequestEntity) {
            StringRequestEntity stringEntity = (StringRequestEntity) requestEntity;
            stringEntity.writeRequest(connection.getOutputStream());
        } else {
            throw new RuntimeException("EntityType[" + requestEntity.getClass()
                    + "] is not yet implemented.");
        }

        int responseCode = connection.getResponseCode();
        updateStatusLineInResponseBody(method, responseCode);

        String response = "";
        if (responseCode == Status.OK.getStatusCode()
                || responseCode == Status.CREATED.getStatusCode()) {
            response = CharStreams.toString(new InputStreamReader(connection.getInputStream()));
        }
        return response;
    }

    /**
     * This will handle HTTP Get Method.
     */
    private String handleGetMethod(HttpMethod method) throws IOException, URIException,
            NoSuchFieldException, IllegalAccessException {
        HttpURLConnection connection = getHttpUrlConnection(method.getURI(), 
                false /*isOutputAllowed*/);

        try {
            InputStream inputStream = (InputStream) connection.getContent();
            String response = CharStreams.toString(new InputStreamReader(inputStream));
            Field responseBodyField = parent.getDeclaredField(FIELD_RESPONSE_BODY_NAME);
            responseBodyField.setAccessible(true);
            responseBodyField.set(method, response.getBytes());
            updateStatusLineInResponseBody(method, connection.getResponseCode());
            return response;
        } catch (IOException e) {
            // TODO(arjuns) : Look for code.
            updateStatusLineInResponseBody(method, 404);
            return "";
        }
    }

    /**
     * Get HTTP Url Connection.
     * 
     * @param uri Destination URI with which connection needs to be established.
     * @param isOutputAllowed Indicates if this connection is going to be used for sending
     *      some data to destination.
     *      
     * @return {@link HttpURLConnection} for destination.
     * @throws IOException 
     */
    private HttpURLConnection getHttpUrlConnection(URI uri, boolean isOutputAllowed)
            throws IOException {
        URL url = new URL(uri.toString());
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setConnectTimeout(DEFAULT_CONNECTION_TIMEOUT_IN_MILLIS);
        
        if (isOutputAllowed) {
            connection.setDoOutput(true);
        }
        
        return connection;
    }
}
