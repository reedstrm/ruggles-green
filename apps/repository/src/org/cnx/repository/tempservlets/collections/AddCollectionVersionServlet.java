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

package org.cnx.repository.tempservlets.collections;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.io.IOException;
import java.io.PrintWriter;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cnx.repository.service.api.AddCollectionVersionResult;
import org.cnx.repository.service.api.CnxRepositoryService;
import org.cnx.repository.service.api.RepositoryRequestContext;
import org.cnx.repository.service.api.RepositoryResponse;
import org.cnx.repository.service.impl.CnxRepositoryServiceImpl;

/**
 * A temp API servlet to add a version for an existing module.
 * 
 * @author Tal Dayan
 */
@SuppressWarnings("serial")
public class AddCollectionVersionServlet extends HttpServlet {
    private final CnxRepositoryService repository = CnxRepositoryServiceImpl.getService();

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        final String colxmlDoc =
                checkNotNull(req.getParameter("colxml"), "Missing post param \"colxml\"");
        final String moduleId =
                checkNotNull(req.getParameter("collection_id"), "Missing post param \"collection_id\"");
        final String expectedVersionParam =
                checkNotNull(req.getParameter("version"), "Missing post param \"version\"");
        @Nullable
        final Integer expectedVersionNumber =
        expectedVersionParam.equals("null") ? null : Integer.parseInt(expectedVersionParam);

        checkArgument(req.getParameterMap().size() == 3, "Expected 3 post parameters, found %s",
                req.getParameterMap().size());

        final RepositoryRequestContext context = new RepositoryRequestContext(null);
        final RepositoryResponse<AddCollectionVersionResult> repositoryResponse =
                repository.addCollectionVersion(context, moduleId, expectedVersionNumber, colxmlDoc);

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

        // Map repository OK to API OK
        checkState(repositoryResponse.isOk());
        final AddCollectionVersionResult result = repositoryResponse.getResult();

        resp.setContentType("text/plain");
        PrintWriter out = resp.getWriter();

        out.println("collection id: " + result.getCollectionId());
        out.println("new version number: " + result.getNewVersionNumber());
    }
}