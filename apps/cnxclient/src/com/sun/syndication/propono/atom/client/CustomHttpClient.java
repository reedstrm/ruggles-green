/*
 * Copyright 2011 Google Inc.
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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

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

import com.google.common.io.CharStreams;

/**
 * Rome-propono uses commons-httpclient which does not work on AppEngine. So creating a custom HTTP
 * Client which wraps URL Fetch service offered by AppEngine.
 *
 * @author Arjun Satyapal
 */
public class CustomHttpClient extends HttpClient {
    private static final String FIELD_RESPONSE_BODY_NAME = "responseBody";
    private static final String FIELD_RESPONSE_STATUS = "statusLine";
    @SuppressWarnings("rawtypes")
    private final Class parent = HttpMethodBase.class;

    /**
     * This is the Generic executeMethod which will replace all the http-methods and in
     * turn depending on type of method will delegate to individual handlers.
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
        String response;
        URL url = new URL(method.getURI().toString());
        PutMethod putMethod = (PutMethod) method;

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoOutput(true);

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
        response = CharStreams.toString(new InputStreamReader(connection.getInputStream()));
        return response;
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
        String response;
        URL url = new URL(method.getURI().toString());
        PostMethod postMethod = (PostMethod) method;

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoOutput(true);
        // TODO(arjuns): reuse for put.
        connection.setRequestMethod("POST");

        for (NameValuePair currParam : postMethod.getParameters()) {
            System.out
                .println("name : " + currParam.getName() + " value = " + currParam.getValue());
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
        updateStatusLineInResponseBody(method, connection.getResponseCode());

        response = CharStreams.toString(new InputStreamReader(connection.getInputStream()));
        return response;
    }

    /**
     * This will handle HTTP Get Method.
     */
    private String handleGetMethod(HttpMethod method) throws IOException, URIException,
            NoSuchFieldException, IllegalAccessException {

        URL url = new URL(method.getURI().toString());
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        GetMethod getMethod = (GetMethod) method;
        InputStream inputStream = (InputStream)connection.getContent();
        String response = CharStreams.toString(new InputStreamReader(inputStream));

        Field responseBodyField = parent.getDeclaredField(FIELD_RESPONSE_BODY_NAME);
        responseBodyField.setAccessible(true);
        responseBodyField.set(method, response.getBytes());
        updateStatusLineInResponseBody(method, connection.getResponseCode());

        return response;
    }
}
