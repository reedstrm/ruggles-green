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

import java.io.IOException;
import java.io.PrintWriter;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cnx.repository.service.api.CnxRepositoryService;
import org.cnx.repository.service.api.AddCollectionResult;
import org.cnx.repository.service.api.RepositoryRequestContext;
import org.cnx.repository.service.api.RepositoryResponse;
import org.cnx.repository.service.impl.CnxRepositoryServiceImpl;

/**
 * A temp API servlet to create a new collection.
 * 
 * @author Tal Dayan
 */
@SuppressWarnings("serial")
public class AddCollectionServlet extends HttpServlet {
    private final CnxRepositoryService repository = CnxRepositoryServiceImpl.getService();

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        @Nullable
        String forcedId = req.getParameter("id");

        final RepositoryRequestContext context = new RepositoryRequestContext(null);
        final RepositoryResponse<AddCollectionResult> repositoryResponse =
            (forcedId == null) ? repository.addCollection(context) : repository
                .addCollectionForMigration(context, forcedId);

        // Map repository error to API error
        if (repositoryResponse.isError()) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    repositoryResponse.getExtendedDescription());
            return;
        }

        // Map repository OK to API OK
        final AddCollectionResult result = repositoryResponse.getResult();
        resp.setContentType("text/plain");
        PrintWriter out = resp.getWriter();

        out.println("collection id: " + result.getCollectionId());
    }
}
