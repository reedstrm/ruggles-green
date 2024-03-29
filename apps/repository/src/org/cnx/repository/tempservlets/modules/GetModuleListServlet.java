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

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cnx.repository.service.api.CnxRepositoryService;
import org.cnx.repository.service.api.GetModuleListResult;
import org.cnx.repository.service.api.RepositoryRequestContext;
import org.cnx.repository.service.api.RepositoryResponse;
import org.cnx.repository.service.impl.CnxRepositoryServiceImpl;

/**
 * A temp API servlet to get module list.
 * 
 * @author Tal Dayan
 */
@SuppressWarnings("serial")
public class GetModuleListServlet extends HttpServlet {
    private final CnxRepositoryService repository = CnxRepositoryServiceImpl.getService();

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        final String cursorParam = checkNotNull(req.getParameter("cursor"), "Missing cursor param");
        final String curosr = cursorParam.equals("null") ? null : cursorParam;

        final String maxResultsParam = checkNotNull(req.getParameter("max"), "Missing max param");
        final int maxResults = Integer.parseInt(maxResultsParam);

        final RepositoryRequestContext context = new RepositoryRequestContext(null);
        final RepositoryResponse<GetModuleListResult> repositoryResponse =
            repository.getModuleList(context, curosr, maxResults);

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
        final GetModuleListResult result = repositoryResponse.getResult();

        resp.setContentType("text/plain");
        PrintWriter out = resp.getWriter();

        out.println("Modules");
        for (String moduleId : result.getModuleIds()) {
            out.printf("Module [%s]\n", moduleId);
        }

        final String endCursor = result.isLast() ? null : result.getEndCursor();

        out.printf("\nEnd cursor: %s\n", endCursor);
    }
}
