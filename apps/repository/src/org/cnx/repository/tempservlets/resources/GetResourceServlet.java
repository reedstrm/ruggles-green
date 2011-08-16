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

package org.cnx.repository.tempservlets.resources;

import static com.google.common.base.Preconditions.checkState;

import java.io.IOException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cnx.repository.service.api.CnxRepositoryService;
import org.cnx.repository.service.api.RepositoryRequestContext;
import org.cnx.repository.service.api.RepositoryResponse;
import org.cnx.repository.service.api.ServeResourceResult;
import org.cnx.repository.service.impl.CnxRepositoryServiceImpl;

/**
 * A temp API servlet to serve a resource using a GET request.
 *
 * @author Tal Dayan
 */
@SuppressWarnings("serial")
public class GetResourceServlet extends HttpServlet {
    private final CnxRepositoryService repository = CnxRepositoryServiceImpl.getService();

    private static final Pattern uriPattern = Pattern.compile("/resource/([a-zA-Z0-9_-]+)");

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // Parse request resource id from the query.
        final String requestURI = req.getRequestURI();
        final Matcher matcher = uriPattern.matcher(requestURI);
        if (!matcher.matches()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "Could parse resource id in request URI [" + requestURI + "]");
            return;
        }
        final String resourceId = matcher.group(1);

        // TODO(arjuns) : This doesnt work.
        final RepositoryResponse<ServeResourceResult> repositoryResponse =
            repository.serveResouce(new RepositoryRequestContext(req, null), resourceId, resp);

        // Map repository error to API error.
        if (repositoryResponse.isError()) {
            switch (repositoryResponse.getStatus()) {
                case BAD_REQUEST:
                    resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                            repositoryResponse.getExtendedDescription());
                    return;
                case NOT_FOUND:
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND,
                            repositoryResponse.getExtendedDescription());
                    return;
                default:
                    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                            repositoryResponse.getExtendedDescription());
                    return;
            }
        }

        checkState(repositoryResponse.isOk());
        for (Map.Entry<String, String> header : repositoryResponse.getResult()
            .getAdditionalHeaders().entrySet()) {
            resp.addHeader(header.getKey(), header.getValue());
        }
        return;
    }
}