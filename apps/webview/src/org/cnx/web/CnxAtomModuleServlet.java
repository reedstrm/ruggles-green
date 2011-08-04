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
package org.cnx.web;

import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

/**
 * Servlet to Handle CNX Resources.
 *
 * @author Arjun Satyapal
 */
@Path("/module")
public class CnxAtomModuleServlet {
    Logger logger = Logger.getLogger(CnxAtomModuleServlet.class.getName());
    private final String COLLECTION_MODULE_POST = "/";

    private final String MODULE_ID_PATH_PARAM = "moduleId";
    private final String VERSION_PATH_PARAM = "version";
    private final String MODULE_VERSION_URL_PATTERN = "/{" + MODULE_ID_PATH_PARAM + "}/version/{"
        + VERSION_PATH_PARAM + "}";



    @GET
    @Path(MODULE_VERSION_URL_PATTERN)
    public Response getModuleVersion(@Context HttpServletRequest req,
            @Context HttpServletResponse res, @PathParam(MODULE_ID_PATH_PARAM) String moduleId,
            @PathParam(VERSION_PATH_PARAM) String version) {
        String helloWorld = "hello world.";

        return Response.ok().entity(helloWorld).build();
    }
}
