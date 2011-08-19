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

package org.cnx.repository.atompub.utils;

import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;

import com.google.appengine.api.utils.SystemProperty;

/**
 * Server related utils.
 *
 * @author Tal Dayan
 */
public class ServerUtil {
    /**
     * Compute local host base url from an incoming httpRequest.
     *
     * @param httpRequest an incoming HTTP request.
     * @return host URL (e.g. "http://myserver.com" or "http://localhost:8888"
     */
    public static String computeHostUrl(HttpServletRequest httpRequest) {
        final String scheme = httpRequest.getScheme();
        final int port = httpRequest.getLocalPort();

        // Is the default port for this scheme?
        final boolean isDefaultPort =
            ("http".equals(scheme) && port == 80) || ("https".equals(scheme) && port == 443);

        final URL serverUrl;
        try {

            if (SystemProperty.environment.value() == SystemProperty.Environment.Value.Development) {
                serverUrl = new URL(scheme, httpRequest.getLocalName(), port, "");
            } else {
                String requestUrl = httpRequest.getRequestURL().toString();
                String urlOfInterest = requestUrl.substring(0, requestUrl.indexOf(".appspot.com"));

                serverUrl = new URL(urlOfInterest + ".appspot.com");
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("Could not construct host url", e);
        }

        return serverUrl.toString();
    }

}
