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
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
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
import org.cnx.repository.service.api.AddModuleVersionResult;
import org.cnx.repository.service.api.CnxRepositoryService;
import org.cnx.repository.service.api.CreateModuleResult;
import org.cnx.repository.service.api.GetModuleVersionResult;
import org.cnx.repository.service.api.RepositoryRequestContext;
import org.cnx.repository.service.api.RepositoryResponse;
import org.cnx.repository.service.impl.CnxRepositoryServiceImpl;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import com.google.cloud.sql.jdbc.internal.Charsets;
import com.google.common.base.Throwables;
import com.sun.syndication.feed.atom.Content;
import com.sun.syndication.feed.atom.Entry;
import com.sun.syndication.feed.atom.Link;
import com.sun.syndication.io.impl.Atom10Parser;
import com.sun.syndication.propono.atom.server.AtomRequest;
import com.sun.syndication.propono.atom.server.AtomRequestImpl;

/**
 * Servlet to Handle CNX Resources.
 *
 * @author Arjun Satyapal
 */
@Path(CnxAtomPubConstants.COLLECTION_MODULE_REL_PATH)
public class CnxAtomModuleServlet {
    Logger logger = Logger.getLogger(CnxAtomModuleServlet.class.getName());
    private final String COLLECTION_MODULE_POST = "/";

    private final String MODULE_ID_PATH_PARAM = "moduleId";
    private final String MODULE_VERSION_PATH_PARAM = "moduleVersion";

    // URL Pattern = /module/<moduleId>/<version>
    private final String MODULE_VERSION_URL_PATTERN = "/{" + MODULE_ID_PATH_PARAM + "}/{"
            + MODULE_VERSION_PATH_PARAM + "}";
    private final String MODULE_VERSION_CNXML_URL = MODULE_VERSION_URL_PATTERN + "/xml";
    private final String MODULE_VERSION_RESOURCE_MAPPING_URL = MODULE_VERSION_URL_PATTERN
            + "/resources";

    private CnxAtomService atomPubService;
    private final CnxRepositoryService repositoryService = CnxRepositoryServiceImpl.getService();

    @POST
    @Produces(CnxMediaTypes.APPLICATION_ATOM_XML)
    @Path(COLLECTION_MODULE_POST)
    public Response createNewModule(@Context HttpServletRequest req,
            @Context HttpServletResponse res) {
        AtomRequest areq = new AtomRequestImpl(req);

        // TODO(arjuns) : See better way of getting URL.
        RepositoryRequestContext context = RepositoryUtils.getRepositoryContext();
        CnxAtomService atomPubService = new CnxAtomService(ServerUtil.computeHostUrl(req));

        RepositoryResponse<CreateModuleResult> createdModule =
                repositoryService.createModule(RepositoryUtils.getRepositoryContext(), null);

        if (createdModule.isOk()) {
            /*
             * TODO(arjuns): Repository service should return following : 1. date.
             */
            VersionWrapper firstVersion = CnxAtomPubConstants.NEW_MODULE_DEFAULT_VERSION;

            CreateModuleResult result = createdModule.getResult();
            Entry entry = new Entry();
            String atomPubId =
                    CnxAtomPubConstants.getAtomPubIdFromCnxIdAndVersion(result.getModuleId(),
                            firstVersion);
            entry.setId(atomPubId);

            // TODO(arjuns) : Change URL to URI.
            URL editUrl =
                    atomPubService.getConstants().getModuleVersionAbsPath(result.getModuleId(),
                            firstVersion);
            entry.setOtherLinks(RepositoryUtils.getListOfLinks(null /* selfUrl */, editUrl));

            try {
                URL modulePath =
                        atomPubService.getConstants().getModuleAbsPath(result.getModuleId());
                URI createdLocation = new URI(modulePath.toString());

                String stringEntry = PrettyXmlOutputter.prettyXmlOutputEntry(entry);
                logger.info("ResponseEntry = " + stringEntry);
                return Response.created(createdLocation).entity(stringEntry).build();
            } catch (URISyntaxException e) {
                logger.severe("Failed to create Module because : "
                        + Throwables.getStackTraceAsString(e));
            } catch (Exception e) {
                // TODO(arjuns): Auto-generated catch block
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
        return Response.serverError().build();
    }

    @PUT
    @Produces(CnxMediaTypes.APPLICATION_ATOM_XML)
    @Path(MODULE_VERSION_URL_PATTERN)
    public Response createNewModuleVersion(@Context HttpServletRequest req,
            @Context HttpServletResponse res, @PathParam(MODULE_ID_PATH_PARAM) String moduleId,
            @PathParam(MODULE_VERSION_PATH_PARAM) String version) throws JDOMException, IOException {
        // TODO(arjuns) : Have check with VersionWrapper.

        // TODO(arjuns) : Handle exceptions.
        AtomRequest areq = new AtomRequestImpl(req);

        // TODO(arjuns) : get a better way to get the context.
        RepositoryRequestContext repositoryContext = RepositoryUtils.getRepositoryContext();
        atomPubService = new CnxAtomService(ServerUtil.computeHostUrl(req));

        Entry postedEntry = null;
        try {
            postedEntry =
                    Atom10Parser.parseEntry(new BufferedReader(new InputStreamReader(req
                            .getInputStream(), Charsets.UTF_8.displayName())), null);
        } catch (Exception e) {
            // TODO(arjuns): Handle exception.
            throw new RuntimeException(e);
        }

        if (postedEntry == null) {
            return Response.serverError().build();
        }

        // TODO(arjuns) : Fix this. Move this to common.
        Content encodedModuleEntryContent = (Content) postedEntry.getContents().get(0);
        String decodedModuleEntryValue =
                atomPubService.getConstants().decodeFrom64BitEncodedString(
                        encodedModuleEntryContent.getValue());

        String cnxmlDoc = getCnxml(decodedModuleEntryValue);
        String resourceMappingDoc = getResourceMappingDoc(decodedModuleEntryValue);

        int newVersion = Integer.parseInt(version);
        RepositoryResponse<AddModuleVersionResult> createdModule =
                repositoryService.addModuleVersion(RepositoryUtils.getRepositoryContext(), moduleId,
                        newVersion, cnxmlDoc, resourceMappingDoc);

        if (createdModule.isOk()) {
            /*
             * TODO(arjuns): Repository service should return following : 1. date.
             */
            AddModuleVersionResult result = createdModule.getResult();
            Entry entry = new Entry();

            VersionWrapper repoVersion = new VersionWrapper(result.getNewVersionNumber());
            String atomPubId =
                    CnxAtomPubConstants.getAtomPubIdFromCnxIdAndVersion(result.getModuleId(),
                            repoVersion);
            entry.setId(atomPubId);
            entry.setPublished(new Date());

            // TODO(arjuns) : probably return by next
            VersionWrapper nextVersion = repoVersion.getNextVersion();

            // URL to fetch the Module published now.
            URL selfUrl =
                    atomPubService.getConstants().getModuleVersionAbsPath(result.getModuleId(),
                            repoVersion);

            // URL where client should PUT next time in order to publish new version.
            URL editUrl =
                    atomPubService.getConstants().getModuleVersionAbsPath(result.getModuleId(),
                            nextVersion);

            List<Link> listOfLinks = RepositoryUtils.getListOfLinks(selfUrl, editUrl);
            entry.setOtherLinks(listOfLinks);

            try {
                URL moduleVersionPath =
                        atomPubService.getConstants().getModuleVersionAbsPath(moduleId, repoVersion);
                URI createdLocation = new URI(moduleVersionPath.toString());

                String stringEntry = PrettyXmlOutputter.prettyXmlOutputEntry(entry);
                logger.info("ResponseEntry = " + stringEntry);
                return Response.created(createdLocation).entity(stringEntry).build();
            } catch (Exception e) {
                // TODO(arjuns) : handle exception.
                logger.severe("Failed to create Module because : "
                        + Throwables.getStackTraceAsString(e));
                throw new RuntimeException(e);
            }
        }

        // TODO(arjuns) : Add more details here in error message.
        return Response.serverError().build();
    }

    @GET
    @Produces(CnxMediaTypes.TEXT_XML)
    @Path(MODULE_VERSION_URL_PATTERN)
    public Response getModuleVersion(@Context HttpServletRequest req,
            @Context HttpServletResponse res, @PathParam(MODULE_ID_PATH_PARAM) String moduleId,
            @PathParam(MODULE_VERSION_PATH_PARAM) String versionString) throws JAXBException,
            JDOMException, IOException {
        // TODO(arjuns) : Have check with VersionWrapper.

        // TODO(arjuns) : Handle exceptions.

        AtomRequest areq = new AtomRequestImpl(req);
        // TODO(arjuns) : get a better way to get the context.
        RepositoryRequestContext repositoryContext = RepositoryUtils.getRepositoryContext();
        CnxAtomService atomPubService = new CnxAtomService(ServerUtil.computeHostUrl(req));

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

        RepositoryResponse<GetModuleVersionResult> moduleVersionResult =
                repositoryService.getModuleVersion(RepositoryUtils.getRepositoryContext(), moduleId,
                        versionInt);

        if (moduleVersionResult.isOk()) {
            GetModuleVersionResult result = moduleVersionResult.getResult();
            String cnxmlDoc = result.getCnxmlDoc();
            String resourceMappingDoc = result.getResourceMapDoc();

            VersionWrapper repoVersion = new VersionWrapper(result.getVersionNumber());
            Entry entry = new Entry();
            String atomPubId =
                    CnxAtomPubConstants.getAtomPubIdFromCnxIdAndVersion(moduleId, repoVersion);
            entry.setId(atomPubId);
            entry.setContents(atomPubService.getConstants().getAtomPubListOfContent(cnxmlDoc,
                    resourceMappingDoc));

            // URL to fetch the Module published now.
            URL selfUrl =
                    atomPubService.getConstants().getModuleVersionAbsPath(result.getModuleId(),
                            repoVersion);

            VersionWrapper nextVersion = repoVersion.getNextVersion();
            // URL where client should PUT next time in order to publish new version.
            URL editUrl =
                    atomPubService.getConstants().getModuleVersionAbsPath(result.getModuleId(),
                            nextVersion);

            List<Link> listOfLinks = RepositoryUtils.getListOfLinks(selfUrl, editUrl);
            entry.setOtherLinks(listOfLinks);

            try {
                String stringEntry = PrettyXmlOutputter.prettyXmlOutputEntry(entry);
                return Response.ok().entity(stringEntry).build();
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }

        // TODO(arjuns) : Add more details here in error message.
        return Response.serverError().build();
    }

    @GET
    @Produces(CnxMediaTypes.TEXT_XML)
    @Path(MODULE_VERSION_CNXML_URL)
    public Response getModuleVersionXml(@Context HttpServletRequest req,
            @Context HttpServletResponse res, @PathParam(MODULE_ID_PATH_PARAM) String moduleId,
            @PathParam(MODULE_VERSION_PATH_PARAM) String versionString) {
        // TODO(arjuns) : Have check with VersionWrapper.

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

        RepositoryResponse<GetModuleVersionResult> moduleVersionResult =
                repositoryService.getModuleVersion(RepositoryUtils.getRepositoryContext(), moduleId,
                        versionInt);

        if (moduleVersionResult.isOk()) {
            return Response.ok().entity(moduleVersionResult.getResult().getCnxmlDoc()).build();
        }

        // TODO(arjuns) : Add more details here in error message.
        return Response.serverError().build();
    }

    @GET
    @Produces(CnxMediaTypes.TEXT_XML)
    @Path(MODULE_VERSION_RESOURCE_MAPPING_URL)
    public Response getModuleVersionResourceMapping(@Context HttpServletRequest req,
            @Context HttpServletResponse res, @PathParam(MODULE_ID_PATH_PARAM) String moduleId,
            @PathParam(MODULE_VERSION_PATH_PARAM) String versionString) {
        // TODO(arjuns) : Have check with VersionWrapper.

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

        RepositoryResponse<GetModuleVersionResult> moduleVersionResult =
                repositoryService.getModuleVersion(RepositoryUtils.getRepositoryContext(), moduleId,
                        versionInt);

        if (moduleVersionResult.isOk()) {
            return Response.ok().entity(moduleVersionResult.getResult().getResourceMapDoc())
                    .build();
        }

        // TODO(arjuns) : Add more details here in error message.
        return Response.serverError().build();
    }

    // TODO(arjuns) : move this to common.
    private String getCnxml(String moduleEntryValue) throws JDOMException, IOException {
        return getDecodedChild("cnxml-doc", moduleEntryValue);
    }

    // TODO(arjuns) : move this to common.
    private String getResourceMappingDoc(String moduleEntryValue) throws JDOMException, IOException {
        return getDecodedChild("resource-mapping-doc", moduleEntryValue);
    }

    // TODO(arjuns) : move this to common.
    private String getDecodedChild(String childElement, String moduleEntryValue)
            throws JDOMException, IOException {
        // TODO(arjuns): Handle exceptions.
        SAXBuilder builder = new SAXBuilder();
        Document document = builder.build(new StringReader(moduleEntryValue));

        Element root = document.getRootElement();
        String encodedXml = root.getChild(childElement).getText();
        String originalXml = atomPubService.getConstants().decodeFrom64BitEncodedString(encodedXml);

        return originalXml;
    }
}
