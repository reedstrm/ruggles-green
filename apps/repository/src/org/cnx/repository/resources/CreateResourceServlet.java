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

package org.cnx.repository.resources;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cnx.repository.common.Services;
import org.cnx.repository.service.api.CreateResourceResult;
import org.cnx.repository.service.api.RepositoryResponse;

/**
 * An API servlet to create a new resource.
 * 
 * TODO(tal): describe in more details.
 * 
 * @author Tal Dayan
 */
@SuppressWarnings("serial")
public class CreateResourceServlet extends HttpServlet {

    // private static final CnxRepositoryService repository = CnxRepositoryServiceImpl.getService();

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        RepositoryResponse<CreateResourceResult> repositoryResponse =
            Services.repository.CreateResource(null);

        // Map repository error to API error
        if (repositoryResponse.isError()) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                repositoryResponse.getDescription());
            return;
        }

        // Map repository OK to API OK
        final CreateResourceResult result = repositoryResponse.getResult();
        resp.setContentType("text/plain");
        PrintWriter out = resp.getWriter();

        out.println("resource id: " + result.getResourceId());
        out.println("upload url: " + result.getResourceUploadUrl());
    }
}