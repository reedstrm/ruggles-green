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
import org.cnx.repository.service.api.GetCollectionVersionInfoResult;
import org.cnx.repository.service.api.RepositoryRequestContext;
import org.cnx.repository.service.api.RepositoryResponse;
import org.cnx.repository.service.impl.CnxRepositoryServiceImpl;

import com.google.appengine.repackaged.com.google.common.base.Join;

/**
 * A temp API servlet to get the general info of a collection version.
 * 
 * @author Tal Dayan
 */
@SuppressWarnings("serial")
public class GetCollectionVersionInfoServlet extends HttpServlet {
    private final CnxRepositoryService repository = CnxRepositoryServiceImpl.getService();

    private static final Logger log = Logger.getLogger(GetCollectionVersionInfoServlet.class
            .getName());

    private static final Pattern uriPattern = Pattern
            .compile("/collection_version_info/([a-zA-Z0-9_-]+)/(latest|[0-9]+)");

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // Parse request resource id from the query.
        final String collectionUri = req.getRequestURI();
        final Matcher matcher = uriPattern.matcher(collectionUri);
        if (!matcher.matches()) {
            final String message =
                    "Could not parse collection id in request URI [" + collectionUri + "]";
            log.log(Level.SEVERE, message);
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, message);
            return;
        }
        final String collectionId = matcher.group(1);
        final String collectionVersionString = matcher.group(2);

        // Determine version to serve. If latest, leave as null and we will set
        // it up later.
        final Integer collectionVersion =
                collectionVersionString.equals("latest") ? null : Integer
                    .valueOf(collectionVersionString);

        final RepositoryRequestContext context = new RepositoryRequestContext(null);
        final RepositoryResponse<GetCollectionVersionInfoResult> repositoryResponse =
                repository.getCollectionVersionInfo(context, collectionId, collectionVersion);

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
                case STATE_MISMATCH:
                    resp.sendError(HttpServletResponse.SC_NO_CONTENT,
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
        final GetCollectionVersionInfoResult result = repositoryResponse.getResult();

        resp.setContentType("text/plain");
        PrintWriter out = resp.getWriter();

        out.println();
        out.println("Collection Version:");

        out.println("* Collection =" + result.getCollectionId());
        out.println("* Version = " + result.getVersionNumber());
        out.println("* Created = " + result.getCreationTime());
        out.println("* Exports = {" + Join.join(", ", result.getExports()) + "}");
    }
}
