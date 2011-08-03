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
package org.cnx.repository.atompub.servlets;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Date;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.cnx.repository.atompub.service.CnxAtomService;
import org.cnx.repository.atompub.utils.CnxAtomPubConstants;
import org.cnx.repository.atompub.utils.CustomMediaTypes;
import org.cnx.repository.atompub.utils.PrettyXmlOutputter;
import org.cnx.repository.service.api.CnxRepositoryService;
import org.cnx.repository.service.api.CreateResourceResult;
import org.cnx.repository.service.api.RepositoryResponse;
import org.cnx.repository.service.impl.CnxRepositoryServiceImpl;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.sun.syndication.feed.atom.Entry;
import com.sun.syndication.feed.atom.Link;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.propono.atom.server.AtomRequest;
import com.sun.syndication.propono.atom.server.AtomRequestImpl;

/**
 * Servlet to Handle CNX Resources.
 * 
 * @author Arjun Satyapal
 */
@Path(CnxAtomPubConstants.COLLECTION_RESOURCE_REL_PATH)
public class CnxAtomResourceServlet {
    Logger logger = Logger.getLogger(CnxAtomResourceServlet.class.getName());
    private final String COLLECTION_RESOURCE_POST = "/";

    private final String RESOURCE_GET_PATH_PARAM = "resourceId";
    private final String RESOURCE_GET_URL_PATTERN = "/{" + RESOURCE_GET_PATH_PARAM + "}";

    private CnxRepositoryService repositoryService = CnxRepositoryServiceImpl.getService();

    @POST
    @Produces(CustomMediaTypes.APPLICATION_ATOM_XML)
    @Path(COLLECTION_RESOURCE_POST)
    public Response postNewResource(@Context HttpServletRequest req,
        @Context HttpServletResponse res) {
        AtomRequest areq = new AtomRequestImpl(req);
        CnxAtomService atomPubService = new CnxAtomService(req);

        RepositoryResponse<CreateResourceResult> createdResource =
            repositoryService.createResource(atomPubService.getConstants().getRepositoryContext());

        if (createdResource.isOk()) {
            /*
             * TODO(arjuns): Repository service should return following : 1. date.
             */

            CreateResourceResult result = createdResource.getResult();
            Entry entry = new Entry();
            entry.setId(result.getResourceId());
            entry.setPublished(new Date());

            Link relatedLink = new Link();
            relatedLink.setRel(CnxAtomPubConstants.REL_TAG_FOR_BLOBSTORE_URL);
            relatedLink.setHref(result.getResourceUploadUrl());
            entry.setOtherLinks(Lists.newArrayList(relatedLink));

            URI createdLocation;
            try {
                URL resourcePath =
                    atomPubService.getConstants().getResourceAbsPath(result.getResourceId());
                createdLocation = new URI(resourcePath.toString());

                String stringEntry = PrettyXmlOutputter.prettyXmlOutputEntry(entry);
                logger.info("ResponseEntry = " + stringEntry);
                return Response.created(createdLocation).entity(stringEntry).build();
            } catch (URISyntaxException e) {
                logger.severe("Failed to create Resource because : "
                    + Throwables.getStackTraceAsString(e));
            } catch (IllegalArgumentException e) {
                // TODO(arjuns): Auto-generated catch block
                e.printStackTrace();
            } catch (FeedException e) {
                // TODO(arjuns): Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO(arjuns): Auto-generated catch block
                e.printStackTrace();
            }
        }
        return Response.serverError().build();
    }

    @GET
    @Path(RESOURCE_GET_URL_PATTERN)
    public Response postNewResource(@Context HttpServletRequest req,
        @Context HttpServletResponse res, @PathParam(RESOURCE_GET_PATH_PARAM) String resourceId) {
        AtomRequest areq = new AtomRequestImpl(req);
        CnxAtomService atomPubService = new CnxAtomService(req);

        repositoryService.serveResouce(atomPubService.getConstants().getRepositoryContext(),
            resourceId, res);

        return Response.ok().build();
    }
}
