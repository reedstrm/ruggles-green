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
package org.cnx.repository.atompub.jerseyservlets;

import static org.cnx.repository.atompub.CnxAtomPubConstants.COLLECTION_CNX_COLLECTION_REL_PATH;
import static org.cnx.repository.atompub.CommonUtils.getURI;
import static org.cnx.repository.atompub.utils.AtomPubResponseUtils.fromRepositoryError;
import static org.cnx.repository.atompub.utils.AtomPubResponseUtils.logAndReturn;

import com.sun.syndication.feed.atom.Entry;
import com.sun.syndication.feed.atom.Link;

import org.cnx.exceptions.CnxBadRequestException;
import org.cnx.exceptions.CnxException;
import org.cnx.exceptions.CnxInternalServerErrorException;
import org.cnx.repository.atompub.CnxAtomPubConstants;
import org.cnx.repository.atompub.CnxMediaTypes;
import org.cnx.repository.atompub.IdWrapper;
import org.cnx.repository.atompub.VersionWrapper;
import org.cnx.repository.atompub.service.CnxAtomService;
import org.cnx.repository.atompub.utils.RepositoryUtils;
import org.cnx.repository.atompub.utils.ServerUtil;
import org.cnx.repository.service.api.AddCollectionVersionResult;
import org.cnx.repository.service.api.CnxRepositoryService;
import org.cnx.repository.service.api.CreateCollectionResult;
import org.cnx.repository.service.api.GetCollectionVersionResult;
import org.cnx.repository.service.api.RepositoryResponse;
import org.cnx.repository.service.impl.CnxRepositoryServiceImpl;
import org.jdom.JDOMException;

import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
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

/**
 * Servlet to Handle CNX Collections..
 * 
 * @author Arjun Satyapal
 */
@Path(COLLECTION_CNX_COLLECTION_REL_PATH)
public class CnxAtomCollectionServlet {
    private Logger logger = Logger.getLogger(CnxAtomCollectionServlet.class.getName());
    // TODO(arjuns): Move these URLs to common.
    private final String COLLECTION_ID_PATH_PARAM = "collectionId";
    private final String COLLECTION_VERSION_PATH_PARAM = "collectionVersion";

    private final String COLLECTION_NEW_POST = "/";
    private final String COLLECTION_MIGRATION_POST = "/migration/{" + COLLECTION_ID_PATH_PARAM
            + "}";

    /**
     * URL Pattern wrt {@link #COLLECTION_CNX_COLLECTION_REL_PATH}
     * /<collectionId>/<collectionVersion>
     */
    private final String COLLECTION_VERSION_URL_PATTERN = "/{" + COLLECTION_ID_PATH_PARAM + "}/{"
            + COLLECTION_VERSION_PATH_PARAM + "}";

    private final String COLLECTION_VERSION_XML_URL = COLLECTION_VERSION_URL_PATTERN + "/xml";

    private CnxAtomService atomPubService;
    private final CnxRepositoryService repositoryService = CnxRepositoryServiceImpl.getService();

    /**
     * Client should post to this URL when it wants to create a new Collection Id.
     */
    @POST
    @Produces(CnxMediaTypes.APPLICATION_ATOM_XML)
    @Path(COLLECTION_NEW_POST)
    public Response createNewCnxCollection(@Context HttpServletRequest req) throws CnxException {
        CnxAtomService atomPubService = new CnxAtomService(ServerUtil.computeHostUrl(req));

        RepositoryResponse<CreateCollectionResult> createdCollection =
                repositoryService.createCollection(RepositoryUtils.getRepositoryContext());

        return handleCreateCollection(atomPubService, createdCollection);
    }

    /**
     * Client should post to this when it wants to migrate an existing CNX Collection and want to
     * retain old CollectionId.
     */
    @POST
    @Produces(CnxMediaTypes.APPLICATION_ATOM_XML)
    @Path(COLLECTION_MIGRATION_POST)
    public Response createNewCnxCollectionForMigration(@Context HttpServletRequest req,
            @PathParam(COLLECTION_ID_PATH_PARAM) String collectionId) throws CnxException {
        final IdWrapper idWrapper = IdWrapper.getIdWrapperFromUrlId(collectionId);
        CnxAtomService atomPubService = new CnxAtomService(ServerUtil.computeHostUrl(req));

        RepositoryResponse<CreateCollectionResult> createdCollection =
                repositoryService.migrationCreateCollectionWithId(
                        RepositoryUtils.getRepositoryContext(), idWrapper.getIdForRepository());

        return handleCreateCollection(atomPubService, createdCollection);
    }

    private Response handleCreateCollection(CnxAtomService atomPubService,
            RepositoryResponse<CreateCollectionResult> createdCollection) throws CnxException,
            CnxBadRequestException {
        if (createdCollection.isOk()) {
            /*
             * TODO(arjuns): Repository service should return following : 1. date.
             */
            CreateCollectionResult result = createdCollection.getResult();
            Entry entry = new Entry();

            VersionWrapper firstVersion = CnxAtomPubConstants.NEW_CNX_COLLECTION_DEFAULT_VERSION;

            IdWrapper repoIdWrapper =
                    IdWrapper.getIdWrapperFromRepositoryId(result.getCollectionId());

            String atomPubId =
                    CnxAtomPubConstants
                            .getAtomPubIdFromCnxIdAndVersion(repoIdWrapper, firstVersion);
            entry.setId(atomPubId);

            URL editUrl =
                    atomPubService.getConstants().getCollectionVersionAbsPath(repoIdWrapper,
                            firstVersion);

            List<Link> listOfLinks = RepositoryUtils.getListOfLinks(null /* selfUrl */, editUrl);
            entry.setOtherLinks(listOfLinks);

            return logAndReturn(logger, Status.CREATED, entry, getURI(editUrl.toString()));

        }
        return fromRepositoryError(logger, createdCollection);
    }

    /**
     * Client should post to this URL when it wants to publish a new Collection Version.
     * 
     * @param req HttpServletRequest
     * @param collectionId Id of desired collection whose new version needs to be published.
     * @param versionString New version that client desires to be published. Possible values are
     *            similar to ones defined for Module
     *            {@link CnxAtomModuleServlet#createNewModuleVersion}
     */
    @PUT
    @Produces(CnxMediaTypes.APPLICATION_ATOM_XML)
    @Path(COLLECTION_VERSION_URL_PATTERN)
    public Response createNewCnxCollectionVersion(@Context HttpServletRequest req,
            @PathParam(COLLECTION_ID_PATH_PARAM) String collectionId,
            @PathParam(COLLECTION_VERSION_PATH_PARAM) String versionString) throws IOException,
            CnxException {
        final IdWrapper idWrapper = IdWrapper.getIdWrapperFromUrlId(collectionId);
        final VersionWrapper newVersion = new VersionWrapper(versionString);

        atomPubService = new CnxAtomService(ServerUtil.computeHostUrl(req));

        Entry postedEntry = ServerUtil.getPostedEntry(logger, req);
        if (postedEntry == null) {
            throw new CnxBadRequestException("Invalid Request : Missing Collection XML.", null);
        }

        String decodedCollXml =
                atomPubService.getConstants().getCollXmlDocFromAtomPubCollectionEntry(postedEntry);

        RepositoryResponse<AddCollectionVersionResult> createdCollection =
                repositoryService.addCollectionVersion(RepositoryUtils.getRepositoryContext(),
                        idWrapper.getIdForRepository(), newVersion.getVersionInt(), decodedCollXml);

        if (createdCollection.isOk()) {
            /*
             * TODO(arjuns): Repository service should return following : 1. date.
             */
            AddCollectionVersionResult repoResult = createdCollection.getResult();
            Entry entry = new Entry();
            entry.setId(repoResult.getCollectionId());

            // TODO(arjuns) : Move this to repository.
            IdWrapper repoIdWrapper =
                    IdWrapper.getIdWrapperFromRepositoryId(repoResult.getCollectionId());
            VersionWrapper createdVersion = new VersionWrapper(repoResult.getNewVersionNumber());
         
            String atomPubId =
                    CnxAtomPubConstants.getAtomPubIdFromCnxIdAndVersion(idWrapper, createdVersion);

            entry.setId(atomPubId);
            // TODO(arjuns) : Repository should return date.
            entry.setPublished(new Date());

            VersionWrapper nextVersion = createdVersion.getNextVersion();

            // URL to fetch the Module published now.
            URL selfUrl =
                    atomPubService.getConstants().getCollectionVersionAbsPath(repoIdWrapper,
                            createdVersion);

            // URL where client should Put next time in order to publish new version.
            URL editUrl =
                    atomPubService.getConstants().getCollectionVersionAbsPath(repoIdWrapper,
                            nextVersion);

            // TODO(arjuns0 : Create a function for this.
            List<Link> listOfLinks = RepositoryUtils.getListOfLinks(selfUrl, editUrl);
            entry.setOtherLinks(listOfLinks);

            return logAndReturn(logger, Status.CREATED, entry, getURI(selfUrl.toString()));
        }
        return fromRepositoryError(logger, createdCollection);
    }

    /**
     * In order to get Collection Version, client should do HTTP Get on this URL.
     * 
     * @param req HttpServletRequest.
     * @param collectionId Id of desired Collection.
     * @param versionString Version of desired collection.
     * @return AtomEntry containing Collection XML.
     * @throws CnxException
     */
    @GET
    @Produces(CnxMediaTypes.TEXT_XML)
    @Path(COLLECTION_VERSION_URL_PATTERN)
    public Response getCnxCollectionVersion(@Context HttpServletRequest req,
            @PathParam(COLLECTION_ID_PATH_PARAM) String collectionId,
            @PathParam(COLLECTION_VERSION_PATH_PARAM) String versionString) throws CnxException {
        final IdWrapper idWrapper = IdWrapper.getIdWrapperFromUrlId(collectionId);
        final VersionWrapper versionWrapper = new VersionWrapper(versionString);
        CnxAtomService atomPubService = new CnxAtomService(ServerUtil.computeHostUrl(req));

        RepositoryResponse<GetCollectionVersionResult> collectionVersionResult =
                repositoryService.getCollectionVersion(RepositoryUtils.getRepositoryContext(),
                        idWrapper.getIdForRepository(), versionWrapper.getVersionInt());

        if (collectionVersionResult.isOk()) {
            GetCollectionVersionResult repoResult = collectionVersionResult.getResult();
            String collXmlDoc = repoResult.getColxmlDoc();

            IdWrapper repoIdWrapper =
                    IdWrapper.getIdWrapperFromRepositoryId(repoResult.getCollectionId());
            VersionWrapper repoVersion = new VersionWrapper(repoResult.getVersionNumber());
            
            Entry entry = new Entry();
            String atomPubId =
                    CnxAtomPubConstants.getAtomPubIdFromCnxIdAndVersion(idWrapper, repoVersion);
            entry.setId(atomPubId);

            // TODO(arjuns) : See if this can be refactored.
            try {
                entry.setContents(atomPubService.getConstants()
                        .getAtomPubListOfContentForCollectionEntry(collXmlDoc));
            } catch (JAXBException e) {
                throw new CnxInternalServerErrorException("JAXBException", e);
            } catch (JDOMException e) {
                throw new CnxInternalServerErrorException("JDomException", e);
            } catch (IOException e) {
                throw new CnxInternalServerErrorException("IOException", e);
            }

            // URL to fetch the Module published now.
            URL selfUrl =
                    atomPubService.getConstants().getCollectionVersionAbsPath(repoIdWrapper,
                            repoVersion);

            VersionWrapper nextVersion = repoVersion.getNextVersion();
            // URL where client should PUT next time in order to publish new version.
            URL editUrl =
                    atomPubService.getConstants().getCollectionVersionAbsPath(repoIdWrapper,
                            nextVersion);
            List<Link> listOfLinks = RepositoryUtils.getListOfLinks(selfUrl, editUrl);
            entry.setOtherLinks(listOfLinks);

            return logAndReturn(logger, Status.OK, entry, null);
        }

        return fromRepositoryError(logger, collectionVersionResult);
    }

    /**
     * In order to get Collection XML, client should do HTTP Get on this URL.
     * 
     * @param collectionId Id of desired Collection.
     * @param versionString Id of desired Version.
     * @return Collection XML.
     */
    @GET
    @Produces(CnxMediaTypes.TEXT_XML)
    @Path(COLLECTION_VERSION_XML_URL)
    public Response getCnxCollectionVersionXml(
            @PathParam(COLLECTION_ID_PATH_PARAM) String collectionId,
            @PathParam(COLLECTION_VERSION_PATH_PARAM) String versionString) {
        final IdWrapper idWrapper = IdWrapper.getIdWrapperFromUrlId(collectionId);
        final VersionWrapper versionWrapper = new VersionWrapper(versionString);

        RepositoryResponse<GetCollectionVersionResult> collectionVersionResult =
                repositoryService.getCollectionVersion(RepositoryUtils.getRepositoryContext(),
                        idWrapper.getIdForRepository(), versionWrapper.getVersionInt());

        if (collectionVersionResult.isOk()) {
            return Response.ok().entity(collectionVersionResult.getResult().getColxmlDoc()).build();
        }

        return fromRepositoryError(logger, collectionVersionResult);
    }
}
