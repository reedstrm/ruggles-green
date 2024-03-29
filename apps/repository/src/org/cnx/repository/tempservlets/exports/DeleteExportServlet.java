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

package org.cnx.repository.tempservlets.exports;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cnx.repository.service.api.CnxRepositoryService;
import org.cnx.repository.service.api.DeleteExportResult;
import org.cnx.repository.service.api.ExportReference;
import org.cnx.repository.service.api.ExportScopeType;
import org.cnx.repository.service.api.ExportType;
import org.cnx.repository.service.api.RepositoryRequestContext;
import org.cnx.repository.service.api.RepositoryResponse;
import org.cnx.repository.service.impl.CnxRepositoryServiceImpl;
import org.cnx.repository.service.impl.operations.ServletUtil;
import org.cnx.repository.service.impl.operations.Services;

/**
 * A temp API servlet to delete an export.
 * 
 * @author Tal Dayan
 */
@SuppressWarnings("serial")
public class DeleteExportServlet extends HttpServlet {
    private final CnxRepositoryService repository = CnxRepositoryServiceImpl.getService();

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        final ExportScopeType scopeType =
                ServletUtil.paramToEnum(ExportScopeType.class, req.getParameter("scope"));
        final String objectId = req.getParameter("id");
        final ExportType exportType =
                Services.config.getExportTypes().get(req.getParameter("type"));
        final String versionNumberParam = req.getParameter("version");
        final Integer versionNumber =
                (versionNumberParam == null || versionNumberParam.equals("null")) ? null : Integer
                    .valueOf(versionNumberParam);

        final ExportReference exportReference =
                new ExportReference(scopeType, objectId, versionNumber, exportType.getId());

        final RepositoryResponse<DeleteExportResult> repositoryResponse =
                repository.deleteExport(new RepositoryRequestContext(null), exportReference);

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
        resp.setContentType("text/plain");
        PrintWriter out = resp.getWriter();

        out.println(repositoryResponse.getDescription());
    }
}
