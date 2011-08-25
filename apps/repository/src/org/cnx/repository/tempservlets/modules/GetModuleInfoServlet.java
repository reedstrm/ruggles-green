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

import static com.google.common.base.Preconditions.checkState;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cnx.repository.service.api.CnxRepositoryService;
import org.cnx.repository.service.api.GetModuleInfoResult;
import org.cnx.repository.service.api.RepositoryRequestContext;
import org.cnx.repository.service.api.RepositoryResponse;
import org.cnx.repository.service.impl.CnxRepositoryServiceImpl;

import com.google.appengine.repackaged.com.google.common.base.Join;

/**
 * A temp API servlet to get general information about a module.
 * 
 * @author Tal Dayan
 */
@SuppressWarnings("serial")
public class GetModuleInfoServlet extends HttpServlet {
    private final CnxRepositoryService repository = CnxRepositoryServiceImpl.getService();

    private static final Logger log = Logger.getLogger(GetModuleInfoServlet.class.getName());

    private static final Pattern uriPattern = Pattern.compile("/module_info/([a-zA-Z0-9_-]+)");

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // Parse request resource id from the query.
        final String moduleUri = req.getRequestURI();
        final Matcher matcher = uriPattern.matcher(moduleUri);
        if (!matcher.matches()) {
            final String message = "Could not parse module id in request URI [" + moduleUri + "]";
            log.log(Level.SEVERE, message);
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, message);
            return;
        }
        final String moduleId = matcher.group(1);

        final RepositoryRequestContext context = new RepositoryRequestContext(null);
        final RepositoryResponse<GetModuleInfoResult> repositoryResponse =
            repository.getModuleInfo(context, moduleId);

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
        final GetModuleInfoResult result = repositoryResponse.getResult();

        resp.setContentType("text/plain");
        PrintWriter out = resp.getWriter();

        out.println("Module Info");
        out.println("* ID = " + result.getModuleId());
        out.println("* Created = " + result.getCreationTime());
        out.println("* Versions = " + result.getVersionCount());
        out.println("* Exports = {" + Join.join(", ", result.getExports()) + "}");
    }
}
