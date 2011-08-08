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

package org.cnx.repository.service.api;

import static com.google.common.base.Preconditions.checkNotNull;

import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;

import org.cnx.util.Nullable;

/**
 * Common context that is passed to each repository operation.
 * 
 * @author Tal Dayan
 */
public class RepositoryRequestContext {

    public final String hostUrl;

    @Nullable
    public final String authenticatedUserId;

    // TODO(tal): add here more attributes (e.g. debug level, debug logger, etc)

    /**
     * @param hostUrl the prefix of the server URL up to the path (not including). Examples:
     *            "http://localhost:8888", "http://my_app-appstope.com". Used to construct URLs
     *            returned in repository responses.
     * 
     * @param authenticatedUserId an optional string with the user id. Null if no user id is
     *            associated with this request. It is the responsibility of the caller to
     *            authenticate the user. The repository service uses this value to authorize the
     *            operation.
     */
    public RepositoryRequestContext(String hostUrl, @Nullable String authenticatedUserId) {
        this.hostUrl = checkNotNull(hostUrl);
        this.authenticatedUserId = authenticatedUserId;
    }

    /**
     * Convenience constructor to construct form httpRequest.
     */
    public RepositoryRequestContext(HttpServletRequest httpRequest,
        @Nullable String authenticatedUserId) {
        this(computeHostUrl(httpRequest), authenticatedUserId);
    }

    public String getHostUrl() {
        return hostUrl;
    }

    @Nullable
    public String getAuthenticatedUserId() {
        return authenticatedUserId;
    }

    /**
     * Compute local host base url from an incoming httpRequest.
     * 
     * @param httpRequest an incoming HTTP request.
     * @return host URL (e.g. "http://myserver.com" or "http://localhost:8888"
     */
    private static String computeHostUrl(HttpServletRequest httpRequest) {
        final String scheme = httpRequest.getScheme();
        final int port = httpRequest.getLocalPort();

        // Is the default port for this scheme?
        final boolean isDefaultPort =
            ("http".equals(scheme) && port == 80) || ("https".equals(scheme) && port == 443);

        final URL url;
        try {
            url = new URL(scheme, httpRequest.getLocalName(), isDefaultPort ? -1 : port, "");
        } catch (MalformedURLException e) {
            throw new RuntimeException("Could not construct host url", e);
        }

        // ApiProxy.Environment env = ApiProxy.getCurrentEnvironment();
        // Object value =
        // env.getAttributes().get("com.google.appengine.runtime.default_version_hostname");
        // System.out.println("**** value: " + value);
        // for (Object key: env.getAttributes().keySet()) {
        // System.out.println("  [" + key + "]");
        // }

        return url.toString();
    }
}
