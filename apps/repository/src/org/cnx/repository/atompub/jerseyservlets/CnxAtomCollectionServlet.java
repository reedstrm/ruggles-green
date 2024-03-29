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

import static org.cnx.common.repository.atompub.CommonUtils.getURI;
import static org.cnx.repository.atompub.utils.AtomPubResponseUtils.fromRepositoryError;
import static org.cnx.repository.atompub.utils.AtomPubResponseUtils.logAndReturn;

import com.sun.syndication.feed.atom.Entry;
import com.sun.syndication.feed.atom.Link;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
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
import org.cnx.common.exceptions.CnxBadRequestException;
import org.cnx.common.exceptions.CnxException;
import org.cnx.common.exceptions.CnxInternalServerErrorException;
import org.cnx.common.repository.ContentType;
import org.cnx.common.repository.RepositoryConstants;
import org.cnx.common.repository.atompub.CnxAtomPubConstants;
import org.cnx.common.repository.atompub.CnxAtomPubUtils;
import org.cnx.common.repository.atompub.IdWrapper;
import org.cnx.common.repository.atompub.ServletUris;
import org.cnx.common.repository.atompub.VersionWrapper;
import org.cnx.repository.atompub.utils.RepositoryUtils;
import org.cnx.repository.atompub.utils.ServerUtil;
import org.cnx.repository.service.api.AddCollectionResult;
import org.cnx.repository.service.api.AddCollectionVersionResult;
import org.cnx.repository.service.api.CnxRepositoryService;
import org.cnx.repository.service.api.GetCollectionVersionResult;
import org.cnx.repository.service.api.RepositoryResponse;
import org.cnx.repository.service.impl.CnxRepositoryServiceImpl;
import org.jdom.JDOMException;

/**
 * Servlet to Handle CNX Collections..
 * 
 * @author Arjun Satyapal
 */
@Path(ServletUris.Collection.COLLECTION_SERVLET)
public class CnxAtomCollectionServlet {
    private Logger logger = Logger.getLogger(CnxAtomCollectionServlet.class.getName());

    private CnxAtomPubConstants cnxConstants;
    private final CnxRepositoryService repositoryService = CnxRepositoryServiceImpl.getService();

    /**
     * When Client does HTTP-POST on
     * {@link org.cnx.common.repository.atompub.ServletUris.Collection#COLLECTION_POST_NEW}, then
     * this method is invoked.
     * 
     * This method in turn sends request to {@link CnxRepositoryService#createCollection}.
     * 
     * This method is used to create a new CollectionId.
     * 
     * @param req HttpServletRequest.
     */
    @POST
    @Produces(ContentType.APPLICATION_ATOM_XML)
    @Path(ServletUris.Collection.COLLECTION_POST_NEW)
    public Response createNewCollection(@Context HttpServletRequest req) throws CnxException {
        cnxConstants = new CnxAtomPubConstants(ServerUtil.computeAtomPubUrl(req));

        RepositoryResponse<AddCollectionResult> createdCollection =
                repositoryService.addCollection(RepositoryUtils.getRepositoryContext());

        return handleCreateCollection(createdCollection);
    }

    /**
     * When Client does HTTP-POST on
     * {@link org.cnx.common.repository.atompub.ServletUris.Collection#COLLECTION_POST_MIGRATION},
     * then this method is invoked.
     * 
     * This is a special function provided in order to allow migration and retaining of old
     * CollectionIds from CNX. Once migration is complete, this method will be removed.
     * 
     * In functionality it is similar to {@link #createNewCnxCollection}
     * 
     * @param req HttpServletRequest.
     * @param collectionId Id that client wants to retain for Collection. This should be less than
     *            {@link RepositoryConstants#MIN_NON_RESERVED_KEY_ID}.
     */
    @POST
    @Produces(ContentType.APPLICATION_ATOM_XML)
    @Path(ServletUris.Collection.COLLECTION_POST_MIGRATION)
    public Response createNewCollectionForMigration(@Context HttpServletRequest req,
            @PathParam(ServletUris.COLLECTION_ID_PATH_PARAM) String collectionId)
            throws CnxException {
        final IdWrapper idWrapper = new IdWrapper(collectionId, IdWrapper.Type.COLLECTION);
        cnxConstants = new CnxAtomPubConstants(ServerUtil.computeAtomPubUrl(req));

        RepositoryResponse<AddCollectionResult> createdCollection =
                repositoryService.addCollectionForMigration(
                        RepositoryUtils.getRepositoryContext(), idWrapper.getId());

        return handleCreateCollection(createdCollection);
    }

    private Response handleCreateCollection(
            RepositoryResponse<AddCollectionResult> createdCollection) throws CnxException,
            CnxBadRequestException {
        if (createdCollection.isOk()) {
            /*
             * TODO(arjuns): Repository service should return following : 1. date.
             */
            AddCollectionResult result = createdCollection.getResult();
            Entry entry = new Entry();

            IdWrapper repoIdWrapper =
                    new IdWrapper(result.getCollectionId(), IdWrapper.Type.COLLECTION);
            VersionWrapper version = CnxAtomPubUtils.DEFAULT_VERSION;

            String atomPubId =
                    CnxAtomPubUtils.getAtomPubIdFromCnxIdAndVersion(repoIdWrapper, version);
            entry.setId(atomPubId);
            // TODO(arjuns) : repository should return this.
            entry.setPublished(new Date());

            VersionWrapper firstVersion = CnxAtomPubUtils.DEFAULT_EDIT_VERSION;
            URL editUrl = cnxConstants.getCollectionVersionAbsPath(repoIdWrapper,
                    firstVersion);

            List<Link> listOfLinks = RepositoryUtils.getListOfLinks(null /* selfUrl */, editUrl);
            entry.setOtherLinks(listOfLinks);

            return logAndReturn(logger, Status.CREATED, entry, getURI(editUrl.toString()));

        }
        return fromRepositoryError(logger, createdCollection);
    }

    /**
     * When Client does HTTP-PUT on
     * {@link org.cnx.common.repository.atompub.ServletUris.Collection#COLLECTION_VERSION_PATH},
     * then this method is invoked.
     * 
     * This method in turn calls {@link CnxRepositoryService#addCollectionVersion}.
     * 
     * This method is used to publish a new Version for an existing Collection.
     * 
     * @param req HttpServletRequest.
     * @param collectionId CollectionId for which client wants to publish a new version.
     * @param versionString New version that client wants to publish.
     * @return AtomEntry containing selfUri and editUri. SelfUri can be used to fetch the version
     *         that was published with this method Invocation, whereas EditUri should be used to
     *         publish versions in future.
     */
    @PUT
    @Produces(ContentType.APPLICATION_ATOM_XML)
    @Path(ServletUris.Collection.COLLECTION_VERSION_PATH)
    public Response createNewCollectionVersion(@Context HttpServletRequest req,
            @PathParam(ServletUris.COLLECTION_ID_PATH_PARAM) String collectionId,
            @PathParam(ServletUris.COLLECTION_VERSION_PATH_PARAM) String versionString)
            throws IOException, CnxException {
        return handleCreateCollectionVersion(req, collectionId, versionString, false /* isMigration */);
    }

    /**
     * When Client does HTTP-PUT on
     * {@link org.cnx.common.repository.atompub.ServletUris.Collection#COLLECTION_PUT_MIGRATION_VERSION}
     * , then this method is invoked.
     * 
     * This method in turn calls {@link CnxRepositoryService#addCollectionVersionForMigration}.
     * 
     * This method is used to publish a new Version for an existing Collection under Restricted
     * Range.
     * 
     * @param req HttpServletRequest.
     * @param collectionId CollectionId for which client wants to publish a new version.
     * @param versionString New version that client wants to publish.
     * @return AtomEntry containing selfUri and editUri. SelfUri can be used to fetch the version
     *         that was published with this method Invocation, whereas EditUri should be used to
     *         publish versions in future.
     */
    @PUT
    @Produces(ContentType.APPLICATION_ATOM_XML)
    @Path(ServletUris.Collection.COLLECTION_PUT_MIGRATION_VERSION)
    public Response createNewCollectionVersionForMigration(@Context HttpServletRequest req,
            @PathParam(ServletUris.COLLECTION_ID_PATH_PARAM) String collectionId,
            @PathParam(ServletUris.COLLECTION_VERSION_PATH_PARAM) String versionString)
            throws IOException, CnxException {
        return handleCreateCollectionVersion(req, collectionId, versionString, true /* isMigration */);
    }

    private Response handleCreateCollectionVersion(HttpServletRequest req, String collectionId,
            String versionString, boolean isMigration) throws CnxException, CnxBadRequestException,
            UnsupportedEncodingException {
        final IdWrapper idWrapper = new IdWrapper(collectionId, IdWrapper.Type.COLLECTION);
        final VersionWrapper newVersion = new VersionWrapper(versionString);

        cnxConstants = new CnxAtomPubConstants(ServerUtil.computeAtomPubUrl(req));

        Entry postedEntry = ServerUtil.getPostedEntry(logger, req);
        if (postedEntry == null) {
            throw new CnxBadRequestException("Invalid Request : Missing Collection XML.", null);
        }

        String decodedCollXml =
                CnxAtomPubUtils.getCollXmlDocFromAtomPubCollectionEntry(postedEntry);

        RepositoryResponse<AddCollectionVersionResult> createdCollection;

        if (isMigration) {
            createdCollection =
                    repositoryService.addCollectionVersionForMigration(
                            RepositoryUtils.getRepositoryContext(),
                            idWrapper.getId(), newVersion.getVersionInt(), decodedCollXml);
        } else {
            createdCollection =
                    repositoryService.addCollectionVersion(RepositoryUtils.getRepositoryContext(),
                            idWrapper.getId(), newVersion.getVersionInt(), decodedCollXml);
        }

        if (createdCollection.isOk()) {
            /*
             * TODO(arjuns): Repository service should return following : 1. date.
             */
            AddCollectionVersionResult repoResult = createdCollection.getResult();
            Entry entry = new Entry();
            entry.setId(repoResult.getCollectionId());

            // TODO(arjuns) : Move this to repository.
            IdWrapper repoIdWrapper =
                    new IdWrapper(repoResult.getCollectionId(), IdWrapper.Type.COLLECTION);
            VersionWrapper repoVersion = new VersionWrapper(repoResult.getNewVersionNumber());

            String atomPubId =
                    CnxAtomPubUtils.getAtomPubIdFromCnxIdAndVersion(idWrapper, repoVersion);

            entry.setId(atomPubId);
            // TODO(arjuns) : Repository should return date.
            entry.setPublished(new Date());
            entry.setOtherLinks(getListOfLinks(repoIdWrapper, repoVersion));

            URL selfUrl =
                    cnxConstants.getCollectionVersionAbsPath(repoIdWrapper,
                            repoVersion);
            return logAndReturn(logger, Status.CREATED, entry, getURI(selfUrl.toString()));
        }
        return fromRepositoryError(logger, createdCollection);
    }

    /**
     * When Client does HTTP-GET on
     * {@link org.cnx.common.repository.atompub.ServletUris.Collection#COLLECTION_VERSION_PATH},
     * then this method is invoked.
     * 
     * This method in turn calls {@link CnxRepositoryService#getCollectionVersion}.
     * 
     * This method is used to fetch CollXml for a Collection-Version.
     * 
     * @param req HttpServletRequest.
     * @param collectionId Id of desired Collection.
     * @param versionString Version of desired Collection.
     * @return AtomEntry containing selfUri and editUri. SelfUri can be used to fetch the version
     *         that was published with this method Invocation, whereas EditUri should be used to
     *         publish versions in future.
     */
    @GET
    @Produces(ContentType.TEXT_XML_UTF8)
    @Path(ServletUris.Collection.COLLECTION_VERSION_PATH)
    public Response getCollectionVersion(@Context HttpServletRequest req,
            @PathParam(ServletUris.COLLECTION_ID_PATH_PARAM) String collectionId,
            @PathParam(ServletUris.COLLECTION_VERSION_PATH_PARAM) String versionString)
            throws CnxException {
        final IdWrapper idWrapper = new IdWrapper(collectionId, IdWrapper.Type.COLLECTION);
        final VersionWrapper versionWrapper = new VersionWrapper(versionString);
        cnxConstants = new CnxAtomPubConstants(ServerUtil.computeAtomPubUrl(req));

        RepositoryResponse<GetCollectionVersionResult> collectionVersionResult =
                repositoryService.getCollectionVersion(RepositoryUtils.getRepositoryContext(),
                        idWrapper.getId(), versionWrapper.getVersionInt());

        if (collectionVersionResult.isOk()) {
            GetCollectionVersionResult repoResult = collectionVersionResult.getResult();
            String collXmlDoc = repoResult.getColxmlDoc();

            IdWrapper repoIdWrapper =
                    new IdWrapper(repoResult.getCollectionId(), IdWrapper.Type.COLLECTION);
            VersionWrapper repoVersion = new VersionWrapper(repoResult.getVersionNumber());

            Entry entry = new Entry();
            String atomPubId =
                    CnxAtomPubUtils.getAtomPubIdFromCnxIdAndVersion(idWrapper, repoVersion);
            entry.setId(atomPubId);

            // TODO(arjuns) : See if this can be refactored.
            try {
                entry.setContents(CnxAtomPubUtils
                        .getAtomPubListOfContentForCollectionEntry(collXmlDoc));
            } catch (JAXBException e) {
                throw new CnxInternalServerErrorException("JAXBException", e);
            } catch (JDOMException e) {
                throw new CnxInternalServerErrorException("JDomException", e);
            } catch (IOException e) {
                throw new CnxInternalServerErrorException("IOException", e);
            }

            entry.setOtherLinks(getListOfLinks(repoIdWrapper, repoVersion));
            return logAndReturn(logger, Status.OK, entry, null);
        }

        return fromRepositoryError(logger, collectionVersionResult);
    }

    /**
     * When Client does HTTP-GET on
     * {@link org.cnx.common.repository.atompub.ServletUris.Collection#COLLECTION_VERSION_COLLXML},
     * then this method is invoked.
     * 
     * This method is used to fetch CNXML.
     * 
     * @param collectionId Id of desired Collection.
     * @param versionString Version of desired Collection.
     * @return CNXML
     */
    @GET
    @Produces(ContentType.TEXT_XML_UTF8)
    @Path(ServletUris.Collection.COLLECTION_VERSION_COLLXML)
    public Response getCollectionVersionXml(
            @PathParam(ServletUris.COLLECTION_ID_PATH_PARAM) String collectionId,
            @PathParam(ServletUris.COLLECTION_VERSION_PATH_PARAM) String versionString) {
        final IdWrapper idWrapper = new IdWrapper(collectionId, IdWrapper.Type.COLLECTION);
        final VersionWrapper versionWrapper = new VersionWrapper(versionString);

        RepositoryResponse<GetCollectionVersionResult> collectionVersionResult =
                repositoryService.getCollectionVersion(RepositoryUtils.getRepositoryContext(),
                        idWrapper.getId(), versionWrapper.getVersionInt());

        if (collectionVersionResult.isOk()) {
            return Response.ok().entity(collectionVersionResult.getResult().getColxmlDoc()).build();
        }

        return fromRepositoryError(logger, collectionVersionResult);
    }

    private List<Link> getListOfLinks(IdWrapper id, VersionWrapper version) {
        // URL to fetch the Collection which was published now.
        URL selfUrl = cnxConstants.getCollectionVersionAbsPath(id, version);

        // URL where client should do HTTP PUT next time in order to publish new version.
        URL editUrl = cnxConstants.getCollectionVersionAbsPath(id,
                version.getNextVersion());

        List<Link> listOfLinks = RepositoryUtils.getListOfLinks(selfUrl, editUrl);

        return listOfLinks;
    }
}
