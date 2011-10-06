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

package org.cnx.repository.tempservlets.modules;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cnx.repository.service.api.AddModuleVersionResult;
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
public class AddModuleVersionServlet extends HttpServlet {
    private final CnxRepositoryService repository = CnxRepositoryServiceImpl.getService();

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        final String cnxmlDoc =
                checkNotNull(req.getParameter("cnxml"), "Missing post param \"cnxml\"");
        final String resourceMapDoc =
                checkNotNull(req.getParameter("manifest"), "Missing post param \"manifest\"");
        final String moduleId =
                checkNotNull(req.getParameter("module_id"), "Missing post param \"module_id\"");
        final String expectedVersionParam =
                checkNotNull(req.getParameter("version"), "Missing post param \"version\"");
        final String migrationParam =
                checkNotNull(req.getParameter("migration"), "Missing post param \"migration\"");

        final Integer expectedVersionNumber =
                expectedVersionParam.equals("null") ? null : Integer.parseInt(expectedVersionParam);

        final boolean isMigration = migrationParam != null && migrationParam.equals("y");

        checkArgument(req.getParameterMap().size() == 5, "Expected 5 post parameters, found %s",
                req.getParameterMap().size());

        final RepositoryRequestContext context = new RepositoryRequestContext(null);

        final RepositoryResponse<AddModuleVersionResult> repositoryResponse;
        if (isMigration) {
            checkNotNull(expectedVersionNumber, "Missing param \"version\", required for migration.");
            repositoryResponse =
                    repository.addModuleVersionForMigration(context, moduleId, expectedVersionNumber,
                            cnxmlDoc, resourceMapDoc);
        } else {
            repositoryResponse =
                    repository.addModuleVersion(context, moduleId, expectedVersionNumber, cnxmlDoc,
                            resourceMapDoc);
        }

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
        final AddModuleVersionResult result = repositoryResponse.getResult();

        resp.setContentType("text/plain");
        PrintWriter out = resp.getWriter();

        out.println("is migration: " + isMigration);
        out.println("module id: " + result.getModuleId());
        out.println("new version number: " + result.getNewVersionNumber());
    }
}
