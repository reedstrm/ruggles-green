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

import com.sun.syndication.feed.atom.Content;
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
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;
import org.cnx.common.exceptions.CnxBadRequestException;
import org.cnx.common.exceptions.CnxException;
import org.cnx.common.exceptions.CnxInternalServerErrorException;
import org.cnx.common.repository.ContentType;
import org.cnx.common.repository.RepositoryConstants;
import org.cnx.common.repository.atompub.CnxAtomPubUtils;
import org.cnx.common.repository.atompub.IdWrapper;
import org.cnx.common.repository.atompub.ServletUris;
import org.cnx.common.repository.atompub.VersionWrapper;
import org.cnx.repository.atompub.service.CnxAtomService;
import org.cnx.repository.atompub.utils.RepositoryUtils;
import org.cnx.repository.atompub.utils.ServerUtil;
import org.cnx.repository.service.api.AddModuleVersionResult;
import org.cnx.repository.service.api.CnxRepositoryService;
import org.cnx.repository.service.api.AddModuleResult;
import org.cnx.repository.service.api.GetModuleVersionResult;
import org.cnx.repository.service.api.RepositoryResponse;
import org.cnx.repository.service.impl.CnxRepositoryServiceImpl;
import org.jdom.JDOMException;

/**
 * Servlet to Handle CNX Modules.
 * 
 * @author Arjun Satyapal
 */
@Path(ServletUris.Module.MODULE_SERVLET)
public class CnxAtomModuleServlet {
    private final Logger logger = Logger.getLogger(CnxAtomModuleServlet.class.getName());
    private CnxAtomService atomPubService;
    private final CnxRepositoryService repositoryService = CnxRepositoryServiceImpl.getService();

    /**
     * When Client does HTTP-POST on
     * {@link org.cnx.common.repository.atompub.ServletUris.Module#MODULE_POST_NEW}, then this method is
     * invoked.
     * 
     * This method in turn sends request to {@link CnxRepositoryService#createModule}.
     * 
     * This method is used to create a new ModuleId.
     * 
     * @param req HttpServletRequest.
     */
    @POST
    @Produces(ContentType.APPLICATION_ATOM_XML)
    @Path(ServletUris.Module.MODULE_POST_NEW)
    public Response createNewModule(@Context HttpServletRequest req) throws CnxException {
        atomPubService = new CnxAtomService(ServerUtil.computeHostUrl(req));

        RepositoryResponse<AddModuleResult> createdModule =
                repositoryService.addModule(RepositoryUtils.getRepositoryContext());

        return handleCreationOfModule(atomPubService, createdModule);
    }

    /**
     * When Client does HTTP-POST on
     * {@link org.cnx.common.repository.atompub.ServletUris.Module#MODULE_POST_MIGRATION}, then this method
     * is invoked.
     * 
     * This is a special function provided in order to allow migration and retaining of old
     * ModulesIds from CNX. Once migration is complete, this method will be removed.
     * 
     * In functionality it is similar to {@link #createNewModule}.
     * 
     * @param req HttpServletRequest.
     * @param moduleId Id that client wants to retain for Module. This should be less than
     *            {@link RepositoryConstants#MIN_NON_RESERVED_KEY_ID}.
     */
    @POST
    @Produces(ContentType.APPLICATION_ATOM_XML)
    @Path(ServletUris.Module.MODULE_POST_MIGRATION)
    public Response createNewModuleForMigration(@Context HttpServletRequest req,
            @PathParam(ServletUris.MODULE_ID_PATH_PARAM) String moduleId) throws CnxException {
        final IdWrapper idWrapper = new IdWrapper(moduleId, IdWrapper.Type.MODULE);
        atomPubService = new CnxAtomService(ServerUtil.computeHostUrl(req));

        RepositoryResponse<AddModuleResult> createdModule =
                repositoryService.addModuleForMigration(
                        RepositoryUtils.getRepositoryContext(), idWrapper.getId());
        return handleCreationOfModule(atomPubService, createdModule);
    }

    private Response handleCreationOfModule(CnxAtomService atomPubService,
            RepositoryResponse<AddModuleResult> createdModule) throws CnxException {
        if (createdModule.isOk()) {
            AddModuleResult repoResult = createdModule.getResult();
            /*
             * TODO(arjuns): Repository service should return following : 1. date.
             */
            IdWrapper repoIdWrapper = new IdWrapper(repoResult.getModuleId(), IdWrapper.Type.MODULE);
            VersionWrapper version = CnxAtomPubUtils.DEFAULT_VERSION;

            Entry entry = new Entry();
            String atomPubId =
                    CnxAtomPubUtils.getAtomPubIdFromCnxIdAndVersion(repoIdWrapper, version);
            entry.setId(atomPubId);

            // TODO(arjuns) : This date should be returned from repository.
            entry.setPublished(new Date());

            // TODO(arjuns) : Change URL to URI.
            VersionWrapper firstVersion = CnxAtomPubUtils.DEFAULT_EDIT_VERSION;
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
     * When Client does HTTP-PUT on
     * {@link org.cnx.common.repository.atompub.ServletUris.Module#MODULE_VERSION_PATH}, then this method
     * is invoked.
     * 
     * This method in turn calls {@link CnxRepositoryService#addModuleVersion}.
     * 
     * This method is used to publish a new Version for an existing Module.
     * 
     * @param req HttpServletRequest.
     * @param moduleId ModuleId for which client wants to publish a new version.
     * @param versionString New version that client wants to publish.
     * @return AtomEntry containing selfUri and editUri. SelfUri can be used to fetch the version
     *         that was published with this method Invocation, whereas EditUri should be used to
     *         publish versions in future.
     */
    @PUT
    @Produces(ContentType.APPLICATION_ATOM_XML)
    @Path(ServletUris.Module.MODULE_VERSION_PATH)
    public Response createNewModuleVersion(@Context HttpServletRequest req,
            @PathParam(ServletUris.MODULE_ID_PATH_PARAM) String moduleId,
            @PathParam(ServletUris.MODULE_VERSION_PATH_PARAM) String versionString)
                    throws CnxException {
        final IdWrapper idWrapper = new IdWrapper(moduleId, IdWrapper.Type.MODULE);
        final VersionWrapper versionWrapper = new VersionWrapper(versionString);
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
                    CnxAtomPubUtils.decodeFromBase64EncodedString(encodedModuleEntryContent
                            .getValue());
            cnxmlDoc = CnxAtomPubUtils.getCnxmlFromModuleEntryXml(decodedModuleEntryValue);
            resourceMappingDoc =
                    CnxAtomPubUtils
                    .getResourceMappingDocFromModuleEntryXml(decodedModuleEntryValue);
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

            IdWrapper repoIdWrapper = new IdWrapper(repoResult.getModuleId(), IdWrapper.Type.MODULE);

            VersionWrapper repoVersion = new VersionWrapper(repoResult.getNewVersionNumber());
            String atomPubId =
                    CnxAtomPubUtils.getAtomPubIdFromCnxIdAndVersion(repoIdWrapper, repoVersion);

            entry.setId(atomPubId);
            entry.setPublished(new Date());
            entry.setOtherLinks(getListOfLinks(repoIdWrapper, repoVersion));

            URL selfUrl =
                    atomPubService.getConstants().getModuleVersionAbsPath(repoIdWrapper,
                            repoVersion);
            return logAndReturn(logger, Status.CREATED, entry, getURI(selfUrl.toString()));
        }

        return fromRepositoryError(logger, createdModule);
    }

    /**
     * When Client does HTTP-GET on
     * {@link org.cnx.common.repository.atompub.ServletUris.Module#MODULE_VERSION_PATH}, then this method
     * is invoked.
     * 
     * This method in turn calls {@link CnxRepositoryService#getModuleVersion}.
     * 
     * This method is used to fetch CNXML and ResourceMappingDoc for a Module-Version.
     * 
     * @param req HttpServletRequest.
     * @param moduleId Id of desired Module.
     * @param versionString Version of desired Module.
     * @return AtomEntry containing selfUri and editUri. SelfUri can be used to fetch the version
     *         that was published with this method Invocation, whereas EditUri should be used to
     *         publish versions in future.
     * @throws FactoryConfigurationError 
     * @throws XMLStreamException 
     */
    @GET
    @Produces(ContentType.TEXT_XML_UTF8)
    @Path(ServletUris.Module.MODULE_VERSION_PATH)
    public Response getModuleVersion(@Context HttpServletRequest req,
            @PathParam(ServletUris.MODULE_ID_PATH_PARAM) String moduleId,
            @PathParam(ServletUris.MODULE_VERSION_PATH_PARAM) String versionString)
            throws CnxException, XMLStreamException, FactoryConfigurationError {
        final IdWrapper idWrapper = new IdWrapper(moduleId, IdWrapper.Type.MODULE);
        final VersionWrapper versionWrapper = new VersionWrapper(versionString);

        atomPubService = new CnxAtomService(ServerUtil.computeHostUrl(req));

        RepositoryResponse<GetModuleVersionResult> moduleVersionResult =
                repositoryService.getModuleVersion(RepositoryUtils.getRepositoryContext(),
                        idWrapper.getId(), versionWrapper.getVersionInt());

        if (moduleVersionResult.isOk()) {
            GetModuleVersionResult repoResult = moduleVersionResult.getResult();
            String cnxmlDoc = repoResult.getCnxmlDoc();
            String resourceMappingDoc = repoResult.getResourceMapDoc();

            IdWrapper repoIdWrapper = new IdWrapper(repoResult.getModuleId(), IdWrapper.Type.MODULE);

            VersionWrapper repoVersion = new VersionWrapper(repoResult.getVersionNumber());
            Entry entry = new Entry();
            String atomPubId =
                    CnxAtomPubUtils.getAtomPubIdFromCnxIdAndVersion(repoIdWrapper, repoVersion);
            entry.setId(atomPubId);
            // TODO(arjuns) : See if this can be refactored.
            try {
                entry.setContents(CnxAtomPubUtils.getAtomPubListOfContent(cnxmlDoc,
                        resourceMappingDoc));
            } catch (JAXBException e) {
                throw new CnxInternalServerErrorException("JAXBException", e);
            } catch (JDOMException e) {
                throw new CnxInternalServerErrorException("JDomException", e);
            } catch (IOException e) {
                throw new CnxInternalServerErrorException("IOException", e);
            }

            entry.setOtherLinks(getListOfLinks(repoIdWrapper, repoVersion));

            return logAndReturn(logger, Status.OK, entry, null /* locationUrl */);
        }

        return fromRepositoryError(logger, moduleVersionResult);
    }

    /**
     * When Client does HTTP-GET on
     * {@link org.cnx.common.repository.atompub.ServletUris.Module#MODULE_VERSION_CNXML}, then this method
     * is invoked.
     * 
     * This method is used to fetch CNXML.
     * 
     * @param moduleId Id of desired Module.
     * @param versionString Version of desired Module.
     * @return CNXML
     */
    @GET
    @Produces(ContentType.TEXT_XML_UTF8)
    @Path(ServletUris.Module.MODULE_VERSION_CNXML)
    public Response getModuleVersionXml(
            @PathParam(ServletUris.MODULE_ID_PATH_PARAM) String moduleId,
            @PathParam(ServletUris.MODULE_VERSION_PATH_PARAM) String versionString) {
        final IdWrapper idWrapper = new IdWrapper(moduleId, IdWrapper.Type.MODULE);
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
     * When Client does HTTP-GET on
     * {@link org.cnx.common.repository.atompub.ServletUris.Module#MODULE_VERSION_RESOURCE_MAPPING}, then
     * this method is invoked.
     * 
     * This method is used to fetch ResourceMapping XML.
     * 
     * @param moduleId Id of desired Module.
     * @param versionString Version of desired Module.
     * @return ResourceMappingXml
     */
    @GET
    @Produces(ContentType.TEXT_XML_UTF8)
    @Path(ServletUris.Module.MODULE_VERSION_RESOURCE_MAPPING)
    public Response getModuleVersionResourcesXml(
            @PathParam(ServletUris.MODULE_ID_PATH_PARAM) String moduleId,
            @PathParam(ServletUris.MODULE_VERSION_PATH_PARAM) String versionString) {
        final IdWrapper idWrapper = new IdWrapper(moduleId, IdWrapper.Type.MODULE);
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

    private List<Link> getListOfLinks(IdWrapper id, VersionWrapper version) {
        // URL to fetch the Module which was published now.
        URL selfUrl = atomPubService.getConstants().getModuleVersionAbsPath(id, version);

        // URL where client should do HTTP PUT next time in order to publish new version.
        URL editUrl =
                atomPubService.getConstants().getModuleVersionAbsPath(id, version.getNextVersion());

        List<Link> listOfLinks = RepositoryUtils.getListOfLinks(selfUrl, editUrl);

        return listOfLinks;
    }
}
