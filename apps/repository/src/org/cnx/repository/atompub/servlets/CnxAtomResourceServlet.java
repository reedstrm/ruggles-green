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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Map;
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
import javax.ws.rs.core.Response.ResponseBuilder;

import org.cnx.repository.atompub.CnxAtomPubConstants;
import org.cnx.repository.atompub.service.CnxAtomService;
import org.cnx.repository.atompub.utils.CustomMediaTypes;
import org.cnx.repository.atompub.utils.PrettyXmlOutputter;
import org.cnx.repository.atompub.utils.RepositoryUtils;
import org.cnx.repository.atompub.utils.ServerUtil;
import org.cnx.repository.service.api.CnxRepositoryService;
import org.cnx.repository.service.api.CreateResourceResult;
import org.cnx.repository.service.api.RepositoryRequestContext;
import org.cnx.repository.service.api.RepositoryResponse;
import org.cnx.repository.service.api.ServeResourceResult;
import org.cnx.repository.service.impl.CnxRepositoryServiceImpl;

import com.google.common.base.Charsets;
import com.google.common.base.Throwables;
import com.sun.syndication.feed.atom.Entry;
import com.sun.syndication.feed.atom.Link;
import com.sun.syndication.io.impl.Atom10Parser;
import com.sun.syndication.propono.atom.server.AtomRequest;
import com.sun.syndication.propono.atom.server.AtomRequestImpl;

/**
 * Jersey Servlets for Cnx Resources.
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
        // TODO(arjuns): Handle exceptions.
        AtomRequest areq = new AtomRequestImpl(req);
        // TODO(arjuns) : get a better way to get the context.

        CnxAtomService atomPubService = new CnxAtomService(ServerUtil.computeHostUrl(req));

        Entry postedEntry = null;
        try {
            postedEntry =
                Atom10Parser.parseEntry(
                        new BufferedReader(new InputStreamReader(req.getInputStream(),
                            Charsets.UTF_8.displayName())), null);
            logger.info(PrettyXmlOutputter.prettyXmlOutputEntry(postedEntry));
        } catch (Exception e1) {
            // TODO(arjuns): Auto-generated catch block
            throw new RuntimeException(e1);
        }

        RepositoryResponse<CreateResourceResult> createdResource =
            repositoryService.createResource(RepositoryUtils.getRepositoryContext());

        if (createdResource.isOk()) {
            /*
             * TODO(arjuns): Repository service should return following : 1. date.
             */

            CreateResourceResult result = createdResource.getResult();
            Entry entry = new Entry();
            entry.setId(result.getResourceId());
            entry.setTitle(postedEntry.getTitle());
            entry.setPublished(new Date());

            try {
                // TODO(arjuns) : Create a function for this.
                // URL to fetch the Module published now.
                URL selfUrl =
                    atomPubService.getConstants().getResourceAbsPath(result.getResourceId());
                List<Link> listOfLinks = RepositoryUtils.getListOfLinks(selfUrl, null/* editUrl */);

                // TODO(arjuns) : Temporary hack.
                // URL client is expected to post the blob.
                String uploadUrl = result.getResourceUploadUrl();
                if (!uploadUrl.startsWith("http")) {
                    uploadUrl = ServerUtil.computeHostUrl(req) + uploadUrl;
                }

                URL editUrl = new URL(uploadUrl);
                Link blobstoreLink = new Link();
                blobstoreLink.setRel(CnxAtomPubConstants.REL_TAG_FOR_BLOBSTORE_URL);
                blobstoreLink.setHref(editUrl.toString());

                listOfLinks.add(blobstoreLink);
                entry.setOtherLinks(listOfLinks);

                URI createdLocation = new URI(selfUrl.toString());
                String stringEntry = PrettyXmlOutputter.prettyXmlOutputEntry(entry);
                logger.info("ResponseEntry = " + stringEntry);
                return Response.created(createdLocation).entity(stringEntry).build();
            } catch (Exception e) {
                logger.severe("Failed to create Resource because : "
                    + Throwables.getStackTraceAsString(e));
                // TODO(arjuns) : Handle exceptions.
                throw new RuntimeException(e);
            }
        }

        // TODO(arjuns) : Add more errors here.
        return Response.serverError().build();
    }

    // TODO(arjuns) : Do we need URL to return AtomEntry for Resources?

    @GET
    @Path(RESOURCE_GET_URL_PATTERN)
    public Response getResource(@Context HttpServletRequest req, @Context HttpServletResponse res,
            @PathParam(RESOURCE_GET_PATH_PARAM) String resourceId) {
        AtomRequest areq = new AtomRequestImpl(req);
        // TODO(arjuns) : get a better way to get the context.
        RepositoryRequestContext repositoryContext = RepositoryUtils.getRepositoryContext();
        CnxAtomService atomPubService = new CnxAtomService(ServerUtil.computeHostUrl(req));

        RepositoryResponse<ServeResourceResult> serveResourceResult =
            repositoryService.serveResouce(RepositoryUtils.getRepositoryContext(), resourceId, res);

        if (serveResourceResult.isOk()) {
            ServeResourceResult result = serveResourceResult.getResult();
            ResponseBuilder responseBuilder = Response.ok();
            for (Map.Entry<String, String> header : result.getAdditionalHeaders().entrySet()) {
                responseBuilder.header(header.getKey(), header.getValue());
            }

            return responseBuilder.build();
        }

        // TODO(arjuns) : Add more errors here.
        return Response.serverError().build();
    }
}
