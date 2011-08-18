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
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;

import org.cnx.repository.atompub.CnxAtomPubConstants;
import org.cnx.repository.atompub.service.CnxAtomService;
import org.cnx.repository.atompub.utils.CustomMediaTypes;
import org.cnx.repository.atompub.utils.PrettyXmlOutputter;
import org.cnx.repository.atompub.utils.RepositoryUtils;
import org.cnx.repository.atompub.utils.ServerUtil;
import org.cnx.repository.service.api.AddCollectionVersionResult;
import org.cnx.repository.service.api.CnxRepositoryService;
import org.cnx.repository.service.api.CreateCollectionResult;
import org.cnx.repository.service.api.GetCollectionVersionResult;
import org.cnx.repository.service.api.RepositoryResponse;
import org.cnx.repository.service.impl.CnxRepositoryServiceImpl;
import org.jdom.JDOMException;

import com.google.cloud.sql.jdbc.internal.Charsets;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.sun.syndication.feed.atom.Entry;
import com.sun.syndication.feed.atom.Link;
import com.sun.syndication.io.impl.Atom10Parser;
import com.sun.syndication.propono.atom.server.AtomRequest;
import com.sun.syndication.propono.atom.server.AtomRequestImpl;

/**
 * Servlet to Handle CNX Collections..
 * 
 * @author Arjun Satyapal
 */
@Path(CnxAtomPubConstants.COLLECTION_CNX_COLLECTION_REL_PATH)
public class CnxAtomCollectionServlet {
    Logger logger = Logger.getLogger(CnxAtomCollectionServlet.class.getName());
    private final String COLLECTION_CNX_COLLECTION_POST = "/";

    private final String COLLECTION_ID_PATH_PARAM = "collectionId";
    private final String COLLECTION_VERSION_PATH_PARAM = "collectionVersion";
    private final String COLLECTION_VERSION_URL_PATTERN = "/{" + COLLECTION_ID_PATH_PARAM + "}/{"
            + COLLECTION_VERSION_PATH_PARAM + "}";

    private CnxAtomService atomPubService;
    private final CnxRepositoryService repositoryService = CnxRepositoryServiceImpl.getService();

    @POST
    @Produces(CustomMediaTypes.APPLICATION_ATOM_XML)
    @Path(COLLECTION_CNX_COLLECTION_POST)
    public Response createNewCnxCollection(@Context HttpServletRequest req,
            @Context HttpServletResponse res) {
        AtomRequest areq = new AtomRequestImpl(req);

        // TODO(arjuns) : See better way of getting URL.
        CnxAtomService atomPubService = new CnxAtomService(ServerUtil.computeHostUrl(req));

        RepositoryResponse<CreateCollectionResult> createdCollection =
                repositoryService.createCollection(RepositoryUtils.getRepositoryContext());

        if (createdCollection.isOk()) {
            /*
             * TODO(arjuns): Repository service should return following : 1. date.
             */
            CreateCollectionResult result = createdCollection.getResult();
            Entry entry = new Entry();
            // TODO(arjuns) : Refactor this to CnxAtomPubConstants.
            entry.setId(result.getCollectionId() + CnxAtomPubConstants.DELIMITER_ID_VERSION
                    + CnxAtomPubConstants.NEW_CNX_COLLECTION_DEFAULT_VERSION);

            // TODO(arjuns) : Refactor this to a function.
            URL editUrl =
                    atomPubService.getConstants().getCollectionVersionAbsPath(result.getCollectionId(),
                            Integer.toString(CnxAtomPubConstants.NEW_CNX_COLLECTION_DEFAULT_VERSION));

            List<Link> listOfLinks = RepositoryUtils.getListOfLinks(null /* selfUrl */, editUrl);
            entry.setOtherLinks(listOfLinks);

            try {
                URI createdLocation = new URI(editUrl.toString());

                String stringEntry = PrettyXmlOutputter.prettyXmlOutputEntry(entry);
                logger.info("ResponseEntry = " + stringEntry);
                return Response.created(createdLocation).entity(stringEntry).build();
            } catch (Exception e) {
                logger.severe("Failed to create Collection because : "
                        + Throwables.getStackTraceAsString(e));
                throw new RuntimeException(e);
            }
        }
        return Response.serverError().build();
    }

    @PUT
    @Produces(CustomMediaTypes.APPLICATION_ATOM_XML)
    @Path(COLLECTION_VERSION_URL_PATTERN)
    public Response createNewCnxCollectionVersion(@Context HttpServletRequest req,
            @Context HttpServletResponse res,
            @PathParam(COLLECTION_ID_PATH_PARAM) String collectionId,
            @PathParam(COLLECTION_VERSION_PATH_PARAM) String version) throws IOException {
        // TODO(arjuns) : Handle exceptions.
        AtomRequest areq = new AtomRequestImpl(req);
        logger.info(areq.getContentType());

        // TODO(arjuns) : get a better way to get the context.
        atomPubService = new CnxAtomService(ServerUtil.computeHostUrl(req));

        Entry postedEntry = null;
        try {
            postedEntry =
                    Atom10Parser.parseEntry(
                            new BufferedReader(new InputStreamReader(req.getInputStream(),
                                    Charsets.UTF_8.displayName())), null);
        } catch (Exception e) {
            // TODO(arjuns): Auto-generated catch block
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        if (postedEntry == null) {
            // TODO(arjuns) : Add more error message here.
            return Response.serverError().build();
        }

        // TODO(arjuns) : Fix this.
        String decodedCollXml =
                atomPubService.getConstants().getCollXmlDocFromAtomPubCollectionEntry(postedEntry);

        int newVersion = Integer.parseInt(version);
        RepositoryResponse<AddCollectionVersionResult> createdCollection =
                repositoryService.addCollectionVersion(RepositoryUtils.getRepositoryContext(),
                        collectionId, newVersion, decodedCollXml);

        if (createdCollection.isOk()) {
            /*
             * TODO(arjuns): Repository service should return following : 1. date.
             */
            AddCollectionVersionResult result = createdCollection.getResult();
            Entry entry = new Entry();
            entry.setId(result.getCollectionId());
            // TODO(arjuns) : See what is the proper value here.
            entry.setId(collectionId + ":" + result.getNewVersionNumber());
            entry.setPublished(new Date());

            // TODO(arjuns) : probably return by next
            int nextVersion = newVersion + 1;

            // URL to fetch the Module published now.
            URL selfUrl =
                    atomPubService.getConstants().getCollectionVersionAbsPath(result.getCollectionId(),
                            Integer.toString(result.getNewVersionNumber()));

            // TODO(arjuns) : Refactor this to a function.
            // URL where client should PUT next time in order to publish new version.
            URL editUrl =
                    atomPubService.getConstants().getCollectionVersionAbsPath(result.getCollectionId(),
                            Integer.toString(nextVersion));

            // TODO(arjuns0 : Create a function for this.
            Link selfLink = new Link();
            selfLink.setRel(CnxAtomPubConstants.LINK_RELATION_SELF_TAG);
            selfLink.setHref(selfUrl.toString());

            Link editLink = new Link();
            editLink.setRel(CnxAtomPubConstants.LINK_RELATION_EDIT_TAG);
            editLink.setHref(editUrl.toString());

            entry.setOtherLinks(Lists.newArrayList(selfLink, editLink));

            URI createdLocation;
            try {
                createdLocation = new URI(selfLink.getHrefResolved());

                String stringEntry = PrettyXmlOutputter.prettyXmlOutputEntry(entry);
                logger.info("ResponseEntry = " + stringEntry);
                return Response.created(createdLocation).entity(stringEntry).build();
            } catch (Exception e) {
                logger.severe("Failed to create Module because : "
                        + Throwables.getStackTraceAsString(e));
                throw new RuntimeException(e);
            }
        }
        // TODO(arjuns) : Add more error message here.
        return Response.serverError().build();
    }

    // TODO(arjuns) : Fix this hardcoding.
    @GET
    @Produces("text/xml; charset=UTF-8")
    @Path(COLLECTION_VERSION_URL_PATTERN)
    public Response getCnxCollectionVersion(@Context HttpServletRequest req,
            @Context HttpServletResponse res,
            @PathParam(COLLECTION_ID_PATH_PARAM) String collectionId,
            @PathParam(COLLECTION_VERSION_PATH_PARAM) String version) throws JAXBException,
            JDOMException, IOException {
        // TODO(arjuns) : Handle exceptions.

        AtomRequest areq = new AtomRequestImpl(req);
        // TODO(arjuns) : get a better way to get the context.
        CnxAtomService atomPubService = new CnxAtomService(ServerUtil.computeHostUrl(req));

        RepositoryResponse<GetCollectionVersionResult> collectionVersionResult =
                repositoryService.getCollectionVersion(RepositoryUtils.getRepositoryContext(),
                        collectionId, Integer.parseInt(version));

        if (collectionVersionResult.isOk()) {
            GetCollectionVersionResult result = collectionVersionResult.getResult();
            String collXmlDoc = result.getColxmlDoc();

            Entry entry = new Entry();
            entry.setId(collectionId + ":" + version);
            entry.setContents(atomPubService.getConstants()
                    .getAtomPubListOfContentForCollectionEntry(collXmlDoc));

            // URL to fetch the Module published now.
            int currVersion = result.getVersionNumber();
            int nextVersion = currVersion + 1;
            URL selfUrl =
                    atomPubService.getConstants().getCollectionVersionAbsPath(result.getCollectionId(),
                            Integer.toString(currVersion));

            // URL where client should PUT next time in order to publish new version.
            URL editUrl =
                    atomPubService.getConstants().getCollectionVersionAbsPath(result.getCollectionId(),
                            Integer.toString(nextVersion));
            List<Link> listOfLinks = RepositoryUtils.getListOfLinks(selfUrl, editUrl);
            entry.setOtherLinks(listOfLinks);

            try {
                String stringEntry = PrettyXmlOutputter.prettyXmlOutputEntry(entry);
                return Response.ok().entity(stringEntry).build();
            } catch (Exception e) {
                // TODO(arjuns): Auto-generated catch block
                throw new RuntimeException(e);
            }
        }

        // TODO(arjuns) : Add more error message here.
        return Response.serverError().build();
    }
}
