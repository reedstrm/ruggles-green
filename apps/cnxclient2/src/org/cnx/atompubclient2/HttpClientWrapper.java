/*
 * Copyright (C) 2011 The CNX Authors
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
package org.cnx.atompubclient2;

import com.google.appengine.api.utils.SystemProperty;
import java.io.IOException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolException;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;
import org.cnx.common.exceptions.CnxException;
import org.cnx.common.http.HttpStatusEnum;
import org.esxx.js.protocol.GAEConnectionManager;

/**
 * 
 * @author Arjun Satyapal
 */
public class HttpClientWrapper {
    private final DefaultHttpClient httpClient;

    public static HttpClientWrapper getHttpClient() {
        return new HttpClientWrapper();
    }

    public HttpClientWrapper() {
        HttpParams httpParams = new BasicHttpParams();
        httpParams.setParameter("http.protocol.handle-redirects", true);

        // If running from UnitTest, then SystemProperty is null.
        if (SystemProperty.environment.value() != null) {
            httpClient = new DefaultHttpClient(new GAEConnectionManager(), httpParams);
        } else {
            httpClient = new DefaultHttpClient();
        }

        httpClient.setRedirectStrategy(new DefaultRedirectStrategy() {
            @Override
            public boolean isRedirected(HttpRequest request, HttpResponse response,
                    HttpContext context) {
                boolean isRedirect = false;
                try {
                    isRedirect = super.isRedirected(request, response, context);
                } catch (ProtocolException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                if (!isRedirect) {
                    int responseCode = response.getStatusLine().getStatusCode();
                    if (responseCode == HttpStatusEnum.MOVED_PERMANENTLY.getStatusCode()
                            || responseCode == HttpStatusEnum.FOUND.getStatusCode()) {
                        return true;
                    }
                }
                return false;
            }
        });
    }

    public HttpResponse execute(HttpUriRequest httpRequest) throws ClientProtocolException,
            IOException, CnxException {
        HttpResponse httpResponse = httpClient.execute(httpRequest);
        handleHttpResponse(httpRequest, httpResponse);
        return httpResponse;

    }

    protected void handleHttpResponse(HttpUriRequest httpRequest, HttpResponse httpResponse)
            throws CnxException {

        StatusLine statusLine = httpResponse.getStatusLine();
        HttpStatusEnum status =
                HttpStatusEnum.getHttpStatusEnumByStatusCode(statusLine.getStatusCode());

        StringBuilder errorStringBuilder =
                new StringBuilder("HttpStatus = ").append(status.getStatusCode()).append(", ");
        switch (status.getStatusCategories()) {
            case SUCCESSFUL:
                break;

            case REDIRECTION:
                errorStringBuilder.append("Redirection should have been handled automatically ")
                        .append("but did not happen for URI[")
                        .append(httpRequest.getURI())
                        .append("].");
                throw new CnxException(status, errorStringBuilder.toString(), null /* throwable */);

            default:
                errorStringBuilder.append("Failed for ")
                        .append(httpRequest.getMethod()).append(" : ").append(httpRequest.getURI());
                throw new CnxException(status, errorStringBuilder.toString(), null);
        }
    }
}
