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
import javax.ws.rs.core.Response.Status;
import javax.xml.bind.JAXBException;

import org.cnx.repository.atompub.CnxAtomPubConstants;
import org.cnx.repository.atompub.CnxMediaTypes;
import org.cnx.repository.atompub.VersionWrapper;
import org.cnx.repository.atompub.service.CnxAtomService;
import org.cnx.repository.atompub.utils.PrettyXmlOutputter;
import org.cnx.repository.atompub.utils.RepositoryUtils;
import org.cnx.repository.atompub.utils.ServerUtil;
import org.cnx.repository.service.api.AddCollectionVersionResult;
import org.cnx.repository.service.api.CnxRepositoryService;
import org.cnx.repository.service.api.CreateCollectionResult;
import org.cnx.repository.service.api.GetCollectionVersionResult;
import org.cnx.repository.service.api.RepositoryRequestContext;
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

    /**
     * URL Pattern wrt {@link CnxAtomPubConstants.COLLECTION_CNX_COLLECTION_REL_PATH}
     * /<collectionId>/<collectionVersion>
     */
    private final String COLLECTION_VERSION_URL_PATTERN = "/{" + COLLECTION_ID_PATH_PARAM + "}/{"
            + COLLECTION_VERSION_PATH_PARAM + "}";

    private final String COLLECTION_VERSION_XML_URL = COLLECTION_VERSION_URL_PATTERN + "/xml";

    private CnxAtomService atomPubService;
    private final CnxRepositoryService repositoryService = CnxRepositoryServiceImpl.getService();

    @POST
    @Produces(CnxMediaTypes.APPLICATION_ATOM_XML)
    @Path(COLLECTION_CNX_COLLECTION_POST)
    public Response createNewCnxCollection(@Context HttpServletRequest req,
            @Context HttpServletResponse res) {
        AtomRequest areq = new AtomRequestImpl(req);

        // TODO(arjuns) : See better way of getting URL.
        RepositoryRequestContext context = RepositoryUtils.getRepositoryContext();
        CnxAtomService atomPubService = new CnxAtomService(ServerUtil.computeHostUrl(req));

        RepositoryResponse<CreateCollectionResult> createdCollection =
                repositoryService.createCollection(RepositoryUtils.getRepositoryContext(), null);

        if (createdCollection.isOk()) {
            /*
             * TODO(arjuns): Repository service should return following : 1. date.
             */
            CreateCollectionResult result = createdCollection.getResult();
            Entry entry = new Entry();

            VersionWrapper firstVersion = CnxAtomPubConstants.NEW_CNX_COLLECTION_DEFAULT_VERSION;
            String atomPubId =
                    CnxAtomPubConstants.getAtomPubIdFromCnxIdAndVersion(result.getCollectionId(),
                            firstVersion);
            entry.setId(atomPubId);

            URL editUrl =
                    atomPubService.getConstants().getCollectionVersionAbsPath(result.getCollectionId(),
                            firstVersion);

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
    @Produces(CnxMediaTypes.APPLICATION_ATOM_XML)
    @Path(COLLECTION_VERSION_URL_PATTERN)
    public Response createNewCnxCollectionVersion(@Context HttpServletRequest req,
            @Context HttpServletResponse res,
            @PathParam(COLLECTION_ID_PATH_PARAM) String collectionId,
            @PathParam(COLLECTION_VERSION_PATH_PARAM) String version) throws IOException {
        // TODO(arjuns) : Handle exceptions.
        // TODO(arjuns) : Have check with VersionWrapper.

        AtomRequest areq = new AtomRequestImpl(req);
        logger.info(areq.getContentType());

        // TODO(arjuns) : get a better way to get the context.
        RepositoryRequestContext repositoryContext = RepositoryUtils.getRepositoryContext();
        atomPubService = new CnxAtomService(ServerUtil.computeHostUrl(req));

        Entry postedEntry = null;
        try {
            postedEntry =
                    Atom10Parser.parseEntry(new BufferedReader(new InputStreamReader(req
                            .getInputStream(), Charsets.UTF_8.displayName())), null);
        } catch (Exception e) {
            // TODO(arjuns): Auto-generated catch block
            throw new RuntimeException(e);
        }

        if (postedEntry == null) {
            // TODO(arjuns) : Add more error message here.
            return Response.serverError().build();
        }

        // TODO(arjuns) : Fix this.
        String decodedCollXml =
                atomPubService.getConstants().getCollXmlDocFromAtomPubCollectionEntry(postedEntry);

        // TODO(arjuns) : Add validation for version.
        VersionWrapper newVersion = new VersionWrapper(version);
        RepositoryResponse<AddCollectionVersionResult> createdCollection =
                repositoryService.addCollectionVersion(RepositoryUtils.getRepositoryContext(),
                        collectionId, newVersion.getVersionInt(), decodedCollXml);

        if (createdCollection.isOk()) {
            /*
             * TODO(arjuns): Repository service should return following : 1. date.
             */
            AddCollectionVersionResult result = createdCollection.getResult();
            Entry entry = new Entry();
            entry.setId(result.getCollectionId());

            // TODO(arjuns) : Move this to repository.
            VersionWrapper createdVersion = new VersionWrapper(result.getNewVersionNumber());

            String atomPubId =
                    CnxAtomPubConstants.getAtomPubIdFromCnxIdAndVersion(collectionId, createdVersion);

            entry.setId(atomPubId);
            entry.setPublished(new Date());

            // TODO(arjuns) : probably return by next
            VersionWrapper nextVersion = createdVersion.getNextVersion();

            // URL to fetch the Module published now.
            URL selfUrl =
                    atomPubService.getConstants().getCollectionVersionAbsPath(result.getCollectionId(),
                            createdVersion);

            // TODO(arjuns) : Refactor this to a function.
            // URL where client should PUT next time in order to publish new version.
            URL editUrl =
                    atomPubService.getConstants().getCollectionVersionAbsPath(result.getCollectionId(),
                            nextVersion);

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

    @GET
    @Produces(CnxMediaTypes.TEXT_HTML_UTF8)
    @Path(COLLECTION_VERSION_URL_PATTERN)
    public Response getCnxCollectionVersion(@Context HttpServletRequest req,
            @Context HttpServletResponse res,
            @PathParam(COLLECTION_ID_PATH_PARAM) String collectionId,
            @PathParam(COLLECTION_VERSION_PATH_PARAM) String versionString) throws JAXBException,
            JDOMException, IOException {
        // TODO(arjuns) : Handle exceptions.
        // TODO(arjuns) : Have check with VersionWrapper.

        AtomRequest areq = new AtomRequestImpl(req);
        // TODO(arjuns) : get a better way to get the context.
        RepositoryRequestContext repositoryContext = RepositoryUtils.getRepositoryContext();
        CnxAtomService atomPubService = new CnxAtomService(ServerUtil.computeHostUrl(req));

        // TODO(arjuns) : Replace this with versionwrapper.
        final Integer versionInt;
        if (versionString.equals(CnxAtomPubConstants.LATEST_VERSION_STRING)) {
            versionInt = null;
        } else {
            try {
                versionInt = Integer.parseInt(versionString);
            } catch (NumberFormatException e) {
                return Response.status(Status.NOT_FOUND).build();
            }
        }

        RepositoryResponse<GetCollectionVersionResult> collectionVersionResult =
                repositoryService.getCollectionVersion(RepositoryUtils.getRepositoryContext(),
                        collectionId, versionInt);

        if (collectionVersionResult.isOk()) {
            GetCollectionVersionResult result = collectionVersionResult.getResult();
            String collXmlDoc = result.getColxmlDoc();

            VersionWrapper repoVersion = new VersionWrapper(result.getVersionNumber());
            Entry entry = new Entry();
            String atomPubId =
                    CnxAtomPubConstants.getAtomPubIdFromCnxIdAndVersion(collectionId, repoVersion);
            entry.setId(atomPubId);

            entry.setContents(atomPubService.getConstants()
                    .getAtomPubListOfContentForCollectionEntry(collXmlDoc));

            // URL to fetch the Module published now.
            URL selfUrl =
                    atomPubService.getConstants().getCollectionVersionAbsPath(result.getCollectionId(),
                            repoVersion);

            VersionWrapper nextVersion = repoVersion.getNextVersion();
            // URL where client should PUT next time in order to publish new version.
            URL editUrl =
                    atomPubService.getConstants().getCollectionVersionAbsPath(result.getCollectionId(),
                            nextVersion);
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

    @GET
    @Produces(CnxMediaTypes.TEXT_HTML_UTF8)
    @Path(COLLECTION_VERSION_XML_URL)
    public Response getCnxCollectionVersionXml(@Context HttpServletRequest req,
            @Context HttpServletResponse res,
            @PathParam(COLLECTION_ID_PATH_PARAM) String collectionId,
            @PathParam(COLLECTION_VERSION_PATH_PARAM) String versionString) {
        // TODO(arjuns) : Handle exceptions.
        // TODO(arjuns) : Have check with VersionWrapper.

        // TODO(arjuns) : Replace this with versionwrapper.
        final Integer versionInt;
        if (versionString.equals(CnxAtomPubConstants.LATEST_VERSION_STRING)) {
            versionInt = null;
        } else {
            try {
                versionInt = Integer.parseInt(versionString);
            } catch (NumberFormatException e) {
                return Response.status(Status.NOT_FOUND).build();
            }
        }

        RepositoryResponse<GetCollectionVersionResult> collectionVersionResult =
                repositoryService.getCollectionVersion(RepositoryUtils.getRepositoryContext(),
                        collectionId, versionInt);

        if (collectionVersionResult.isOk()) {
            return Response.ok().entity(collectionVersionResult.getResult().getColxmlDoc()).build();
        }

        // TODO(arjuns) : Add more error message here.
        return Response.serverError().build();
    }

}
