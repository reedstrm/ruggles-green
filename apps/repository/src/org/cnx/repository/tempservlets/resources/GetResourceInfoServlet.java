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
import java.io.PrintWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cnx.repository.service.api.GetResourceInfoResult;
import org.cnx.repository.service.api.RepositoryResponse;
import org.cnx.repository.service.api.UploadedResourceContentInfo;
import org.cnx.repository.service.impl.operations.Services;

/**
 * A temp API servlet to serve metadata of a resource.
 * 
 * TODO(tal): delete this servlet after implementing the real API.
 * 
 * @author Tal Dayan
 */
@SuppressWarnings("serial")
public class GetResourceInfoServlet extends HttpServlet {

    private static final Pattern uriPattern = Pattern.compile("/resource_info/([a-zA-Z0-9_-]+)");

    // private static final CnxRepositoryService repository = CnxRepositoryServiceImpl.getService();

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

        final RepositoryResponse<GetResourceInfoResult> repositoryResponse =
            Services.repository.getResourceInfo(null, resourceId);

        // Map repository error to API error.
        if (repositoryResponse.isError()) {
            switch (repositoryResponse.getStatus()) {
                case BAD_REQUEST:
                    resp.sendError(HttpServletResponse.SC_BAD_REQUEST, repositoryResponse
                        .getExtendedDescription());
                    return;
                case NOT_FOUND:
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND, repositoryResponse
                        .getExtendedDescription());
                    return;
                default:
                    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, repositoryResponse
                        .getExtendedDescription());
                    return;
            }
        }

        // Map repository OK to API OK
        checkState(repositoryResponse.isOk());
        final GetResourceInfoResult result = repositoryResponse.getResult();

        resp.setContentType("text/plain");
        PrintWriter out = resp.getWriter();

        out.println("Resource info:");

        out.println("* resource state: " + result.getResourceState());
        out.println("* resource id: " + resourceId);

        if (result.hasContent()) {
            out.println("* uploaded content info:");
            final UploadedResourceContentInfo contentInfo = result.getContentInfo();
            out.println("  - content type: " + contentInfo.getContentType());
            out.println("  - file name: " + contentInfo.getContentOriginalFileName());
            out.println("  - size: " + contentInfo.getContentSize());
            out.println("  - upload time: " + contentInfo.getContentUploadTime());
        }
    }
}
