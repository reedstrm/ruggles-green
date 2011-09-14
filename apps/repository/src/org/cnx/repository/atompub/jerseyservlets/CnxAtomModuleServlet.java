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

import static org.cnx.repository.atompub.CommonUtils.getURI;
import static org.cnx.repository.atompub.utils.AtomPubResponseUtils.fromRepositoryError;
import static org.cnx.repository.atompub.utils.AtomPubResponseUtils.logAndReturn;

import com.sun.syndication.feed.atom.Content;
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
import org.cnx.repository.service.api.AddModuleVersionResult;
import org.cnx.repository.service.api.CnxRepositoryService;
import org.cnx.repository.service.api.CreateModuleResult;
import org.cnx.repository.service.api.GetModuleVersionResult;
import org.cnx.repository.service.api.RepositoryResponse;
import org.cnx.repository.service.impl.CnxRepositoryServiceImpl;
import org.jdom.JDOMException;

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

/**
 * Servlet to Handle CNX Resources.
 * 
 * @author Arjun Satyapal
 */
@Path(CnxAtomPubConstants.COLLECTION_MODULE_REL_PATH)
public class CnxAtomModuleServlet {
    Logger logger = Logger.getLogger(CnxAtomModuleServlet.class.getName());
    private final String MODULE_ID_PATH_PARAM = "moduleId";
    private final String MODULE_VERSION_PATH_PARAM = "moduleVersion";

    // In order to create a new Module, client should post to this URL.
    private final String MODULE_NEW_POST = "/";

    // In order to create Modules in restricted Id Range, client should post to this URL.
    private final String MODULE_MIGRATION_POST = "/migration/{" + MODULE_ID_PATH_PARAM + "}";

    // URL Pattern = /module/<moduleId>/<version>
    private final String MODULE_VERSION_URL_PATTERN = "/{" + MODULE_ID_PATH_PARAM + "}/{"
            + MODULE_VERSION_PATH_PARAM + "}";
    private final String MODULE_VERSION_CNXML_URL = MODULE_VERSION_URL_PATTERN + "/xml";
    private final String MODULE_VERSION_RESOURCE_MAPPING_URL = MODULE_VERSION_URL_PATTERN
            + "/resources";

    private CnxAtomService atomPubService;
    private final CnxRepositoryService repositoryService = CnxRepositoryServiceImpl.getService();

    /**
     * Client should post to this when it wants to create a new ModuleId.
     */
    @POST
    @Produces(CnxMediaTypes.APPLICATION_ATOM_XML)
    @Path(MODULE_NEW_POST)
    public Response createNewModule(@Context HttpServletRequest req) throws CnxException {
        CnxAtomService atomPubService = new CnxAtomService(ServerUtil.computeHostUrl(req));

        RepositoryResponse<CreateModuleResult> createdModule =
                repositoryService.createModule(RepositoryUtils.getRepositoryContext());

        return handleCreationOfModule(atomPubService, createdModule);
    }

    /**
     * Client should post to this when it wants to migrate an existing CNX module and want to retain
     * old ModuleId.
     */
    @POST
    @Produces(CnxMediaTypes.APPLICATION_ATOM_XML)
    @Path(MODULE_MIGRATION_POST)
    public Response createNewModuleForMigration(@Context HttpServletRequest req,
            @PathParam(MODULE_ID_PATH_PARAM) String moduleId) throws CnxException {
        final IdWrapper idWrapper = IdWrapper.getIdWrapper(moduleId);
        CnxAtomService atomPubService = new CnxAtomService(ServerUtil.computeHostUrl(req));

        RepositoryResponse<CreateModuleResult> createdModule =
                repositoryService.migrationCreateModuleWithId(
                        RepositoryUtils.getRepositoryContext(), idWrapper.getId());
        return handleCreationOfModule(atomPubService, createdModule);
    }

    private Response handleCreationOfModule(CnxAtomService atomPubService,
            RepositoryResponse<CreateModuleResult> createdModule) throws CnxException {
        if (createdModule.isOk()) {
            CreateModuleResult repoResult = createdModule.getResult();
            /*
             * TODO(arjuns): Repository service should return following : 1. date.
             */
            IdWrapper repoIdWrapper = IdWrapper.getIdWrapper(repoResult.getModuleId());
            VersionWrapper firstVersion = CnxAtomPubConstants.NEW_MODULE_DEFAULT_VERSION;

            Entry entry = new Entry();
            String atomPubId =
                    CnxAtomPubConstants
                            .getAtomPubIdFromCnxIdAndVersion(repoIdWrapper, firstVersion);
            entry.setId(atomPubId);

            // TODO(arjuns) : Change URL to URI.
            URL editUrl =
                    atomPubService.getConstants().getModuleVersionAbsPath(repoIdWrapper,
                            firstVersion);
            entry.setOtherLinks(RepositoryUtils.getListOfLinks(null /* selfUrl */, editUrl));

            URL modulePath = atomPubService.getConstants().getModuleAbsPath(repoIdWrapper);

            return logAndReturn(logger, Status.CREATED, entry, getURI(modulePath.toString()));
        }

        return fromRepositoryError(logger, createdModule);
    }

    /**
     * When client wants to publish a new Module Version, it should do HTTP PUT to this URL.
     * 
     * @param req HttpServlet Request.
     * @param moduleId ModuleId for which client wants to publish a new version.
     * @param version New version that client wants to publish. Possible values are : * <integer> :
     *            Some integer value > 0. * latest : In that case Repository will publish it as the
     *            latest version.
     * @return AtomEntry containing links for the Current Published version, and Link where client
     *         should do HTTP Put for publishing next version.
     */
    @PUT
    @Produces(CnxMediaTypes.APPLICATION_ATOM_XML)
    @Path(MODULE_VERSION_URL_PATTERN)
    public Response createNewModuleVersion(@Context HttpServletRequest req,
            @PathParam(MODULE_ID_PATH_PARAM) String moduleId,
            @PathParam(MODULE_VERSION_PATH_PARAM) String version) throws CnxException {
        final IdWrapper idWrapper = IdWrapper.getIdWrapper(moduleId);
        final VersionWrapper versionWrapper = new VersionWrapper(version);
        atomPubService = new CnxAtomService(ServerUtil.computeHostUrl(req));
        Entry postedEntry = ServerUtil.getPostedEntry(logger, req);

        if (postedEntry == null) {
            throw new CnxBadRequestException(
                    "Invalid Request : Missing data for CNXML and ResourceMapping XML.", null);
        }

        // TODO(arjuns) : Fix this. Move this to common.
        Content encodedModuleEntryContent = (Content) postedEntry.getContents().get(0);
        String decodedModuleEntryValue;
        String cnxmlDoc;
        String resourceMappingDoc;
        try {
            decodedModuleEntryValue =
                    atomPubService.getConstants().decodeFromBase64EncodedString(
                            encodedModuleEntryContent.getValue());
            cnxmlDoc =
                    atomPubService.getConstants().getCnxmlFromModuleEntryXml(
                            decodedModuleEntryValue);
            resourceMappingDoc =
                    atomPubService.getConstants().getResourceMappingDocFromModuleEntryXml(
                            decodedModuleEntryValue);
        } catch (UnsupportedEncodingException e) {
            throw new CnxBadRequestException("Invalid Encoding", e);
        } catch (JDOMException e) {
            throw new CnxBadRequestException("JDom exception", e);
        } catch (IOException e) {
            throw new CnxBadRequestException("IOExcepiton", e);
        }

        RepositoryResponse<AddModuleVersionResult> createdModule =
                repositoryService.addModuleVersion(RepositoryUtils.getRepositoryContext(),
                        idWrapper.getId(), versionWrapper.getVersionInt(), cnxmlDoc,
                        resourceMappingDoc);

        if (createdModule.isOk()) {
            /*
             * TODO(arjuns): Repository service should return following : 1. date.
             */
            AddModuleVersionResult repoResult = createdModule.getResult();
            Entry entry = new Entry();

            IdWrapper repoIdWrapper = IdWrapper.getIdWrapper(repoResult.getModuleId());

            VersionWrapper repoVersion = new VersionWrapper(repoResult.getNewVersionNumber());
            VersionWrapper nextVersion = repoVersion.getNextVersion();
            
            String atomPubId =
                    CnxAtomPubConstants.getAtomPubIdFromCnxIdAndVersion(repoIdWrapper, repoVersion);
            entry.setId(atomPubId);
            entry.setPublished(new Date());

            // URL to fetch the Module which was published now.
            URL selfUrl =
                    atomPubService.getConstants().getModuleVersionAbsPath(repoIdWrapper,
                            repoVersion);

            // URL where client should do HTTP PUT next time in order to publish new version.
            URL editUrl =
                    atomPubService.getConstants().getModuleVersionAbsPath(repoIdWrapper,
                            nextVersion);

            List<Link> listOfLinks = RepositoryUtils.getListOfLinks(selfUrl, editUrl);
            entry.setOtherLinks(listOfLinks);

            return logAndReturn(logger, Status.CREATED, entry, getURI(selfUrl.toString()));
        }

        return fromRepositoryError(logger, createdModule);
    }

    /**
     * In order to fetch CNXML and ResourceMapping together, client should do get on this URL.
     * 
     * @param moduleId Id of desired Module.
     * @param versionString version for the desired Module.
     * @return Returns AtomEntry containing AtomPubResource Entry which contains both CNXML and
     *         ResourceMapping XML.
     */
    @GET
    @Produces(CnxMediaTypes.TEXT_XML)
    @Path(MODULE_VERSION_URL_PATTERN)
    public Response getModuleVersion(@Context HttpServletRequest req,
            @PathParam(MODULE_ID_PATH_PARAM) String moduleId,
            @PathParam(MODULE_VERSION_PATH_PARAM) String versionString) throws CnxException {
        final IdWrapper idWrapper = IdWrapper.getIdWrapper(moduleId);
        final VersionWrapper versionWrapper = new VersionWrapper(versionString);
        CnxAtomService atomPubService = new CnxAtomService(ServerUtil.computeHostUrl(req));

        RepositoryResponse<GetModuleVersionResult> moduleVersionResult =
                repositoryService.getModuleVersion(RepositoryUtils.getRepositoryContext(),
                        idWrapper.getId(), versionWrapper.getVersionInt());

        if (moduleVersionResult.isOk()) {
            GetModuleVersionResult repoResult = moduleVersionResult.getResult();
            String cnxmlDoc = repoResult.getCnxmlDoc();
            String resourceMappingDoc = repoResult.getResourceMapDoc();

            IdWrapper repoIdWrapper = IdWrapper.getIdWrapper(repoResult.getModuleId());

            VersionWrapper repoVersion = new VersionWrapper(repoResult.getVersionNumber());
            Entry entry = new Entry();
            String atomPubId =
                    CnxAtomPubConstants.getAtomPubIdFromCnxIdAndVersion(repoIdWrapper, repoVersion);
            entry.setId(atomPubId);
            // TODO(arjuns) : See if this can be refactored.
            try {
                entry.setContents(atomPubService.getConstants().getAtomPubListOfContent(cnxmlDoc,
                        resourceMappingDoc));
            } catch (JAXBException e) {
                throw new CnxInternalServerErrorException("JAXBException", e);
            } catch (JDOMException e) {
                throw new CnxInternalServerErrorException("JDomException", e);
            } catch (IOException e) {
                throw new CnxInternalServerErrorException("IOException", e);
            }

            // URL to fetch the Module published now.
            URL selfUrl =
                    atomPubService.getConstants().getModuleVersionAbsPath(repoIdWrapper,
                            repoVersion);

            VersionWrapper nextVersion = repoVersion.getNextVersion();
            // URL where client should PUT next time in order to publish new version.
            URL editUrl =
                    atomPubService.getConstants().getModuleVersionAbsPath(repoIdWrapper,
                            nextVersion);

            List<Link> listOfLinks = RepositoryUtils.getListOfLinks(selfUrl, editUrl);
            entry.setOtherLinks(listOfLinks);

            return logAndReturn(logger, Status.OK, entry, null /* locationUrl */);
        }

        return fromRepositoryError(logger, moduleVersionResult);
    }

    /**
     * In order to fetch CNXML for a given Module, client should do HTTP Get on this URL.
     * 
     * @param moduleId Id of desired Module.
     * @param versionString version for the desired Module.
     * @return CNXML of desired Module.
     */
    @GET
    @Produces(CnxMediaTypes.TEXT_XML_UTF8)
    @Path(MODULE_VERSION_CNXML_URL)
    public Response getModuleVersionXml(@PathParam(MODULE_ID_PATH_PARAM) String moduleId,
            @PathParam(MODULE_VERSION_PATH_PARAM) String versionString) {
        final IdWrapper idWrapper = IdWrapper.getIdWrapper(moduleId);
        final VersionWrapper versionWrapper = new VersionWrapper(versionString);

        RepositoryResponse<GetModuleVersionResult> moduleVersionResult =
                repositoryService.getModuleVersion(RepositoryUtils.getRepositoryContext(),
                        idWrapper.getId(), versionWrapper.getVersionInt());

        if (moduleVersionResult.isOk()) {
            return Response.ok().entity(moduleVersionResult.getResult().getCnxmlDoc()).build();
        }

        return fromRepositoryError(logger, moduleVersionResult);
    }

    /**
     * In order to fetch ResourceMapping XML for a given Module, client should do HTTP Get on this
     * URL.
     * 
     * @param moduleId Id of desired Module.
     * @param versionString Version of desired Module.
     * @return ResourceMapping XML.
     */
    @GET
    @Produces(CnxMediaTypes.TEXT_XML_UTF8)
    @Path(MODULE_VERSION_RESOURCE_MAPPING_URL)
    public Response getModuleVersionResourcesXml(
            @PathParam(MODULE_ID_PATH_PARAM) String moduleId,
            @PathParam(MODULE_VERSION_PATH_PARAM) String versionString) {
        final IdWrapper idWrapper = IdWrapper.getIdWrapper(moduleId);
        final VersionWrapper versionWrapper = new VersionWrapper(versionString);
        RepositoryResponse<GetModuleVersionResult> moduleVersionResult =
                repositoryService.getModuleVersion(RepositoryUtils.getRepositoryContext(),
                        idWrapper.getId(), versionWrapper.getVersionInt());

        if (moduleVersionResult.isOk()) {
            return Response.ok().entity(moduleVersionResult.getResult().getResourceMapDoc())
                    .build();
        }

        return fromRepositoryError(logger, moduleVersionResult);
    }
}
