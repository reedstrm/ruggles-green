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
package org.cnx.repository.atompub.servlets;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;

import com.sun.syndication.feed.atom.Content;
import com.sun.syndication.feed.atom.Entry;
import com.sun.syndication.feed.atom.Link;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.impl.Atom10Parser;
import com.sun.syndication.propono.atom.server.AtomRequest;
import com.sun.syndication.propono.atom.server.AtomRequestImpl;

import org.cnx.repository.atompub.service.CnxAtomService;
import org.cnx.repository.atompub.utils.CnxAtomPubConstants;
import org.cnx.repository.atompub.utils.CustomMediaTypes;
import org.cnx.repository.atompub.utils.PrettyXmlOutputter;
import org.cnx.repository.service.api.AddModuleVersionResult;
import org.cnx.repository.service.api.CnxRepositoryService;
import org.cnx.repository.service.api.CreateModuleResult;
import org.cnx.repository.service.api.GetModuleVersionResult;
import org.cnx.repository.service.api.RepositoryResponse;
import org.cnx.repository.service.impl.CnxRepositoryServiceImpl;
import org.jdom.JDOMException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Date;
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
    private final String VERSION_PATH_PARAM = "version";
    private final String MODULE_VERSION_URL_PATTERN = "/{" + MODULE_ID_PATH_PARAM + "}/version/{"
        + VERSION_PATH_PARAM + "}";

    private CnxRepositoryService repositoryService = CnxRepositoryServiceImpl.getService();

    @POST
    @Produces(CustomMediaTypes.APPLICATION_ATOM_XML)
    @Path(COLLECTION_MODULE_POST)
    public Response createNewModule(@Context HttpServletRequest req,
            @Context HttpServletResponse res) {
        AtomRequest areq = new AtomRequestImpl(req);
        CnxAtomService atomPubService = new CnxAtomService(req);

        RepositoryResponse<CreateModuleResult> createdModule =
            repositoryService.createModule(atomPubService.getConstants().getRepositoryContext());

        if (createdModule.isOk()) {
            /*
             * TODO(arjuns): Repository service should return following : 1. date.
             */
            CreateModuleResult result = createdModule.getResult();
            Entry entry = new Entry();
            // TODO(arjuns) : Refactor this to CnxAtomPubConstants.
            entry.setId(result.getModuleId() + CnxAtomPubConstants.DELIMITER_ID_VERSION
                + CnxAtomPubConstants.NEW_MODULE_DEFAULT_VERSION);

            // TODO(arjuns) : Refactor this to a function.
            URL editUrl =
                atomPubService.getConstants().getModuleVersionAbsPath(result.getModuleId(),
                    Integer.toString(CnxAtomPubConstants.NEW_MODULE_DEFAULT_VERSION));
            Link link = new Link();
            link.setRel(CnxAtomPubConstants.LINK_RELATION_EDIT_TAG);
            link.setHref(editUrl.toString());

            entry.setOtherLinks(Lists.newArrayList(link));

            URI createdLocation;
            try {
                URL modulePath =
                    atomPubService.getConstants().getModuleAbsPath(result.getModuleId());
                createdLocation = new URI(modulePath.toString());

                String stringEntry = PrettyXmlOutputter.prettyXmlOutputEntry(entry);
                logger.info("ResponseEntry = " + stringEntry);
                return Response.created(createdLocation).entity(stringEntry).build();
            } catch (URISyntaxException e) {
                logger.severe("Failed to create Module because : "
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

    @PUT
    @Produces(CustomMediaTypes.APPLICATION_ATOM_XML)
    @Path(MODULE_VERSION_URL_PATTERN)
    public Response createNewModule(@Context HttpServletRequest req,
            @Context HttpServletResponse res, @PathParam(MODULE_ID_PATH_PARAM) String moduleId,
            @PathParam(VERSION_PATH_PARAM) String version) {
        AtomRequest areq = new AtomRequestImpl(req);
        CnxAtomService atomPubService = new CnxAtomService(req);

        // TODO(arjuns) : get cnxml and resource map from client.
        // TODO(arjuns) : repository should accept version.

        // String input = null;
        // try {
        // InputStream inputStream = req.getInputStream();
        // input = CharStreams.toString( new InputStreamReader( inputStream, "UTF-8" ) );
        // } catch (UnsupportedEncodingException e1) {
        // // TODO(arjuns): Auto-generated catch block
        // e1.printStackTrace();
        // return Response.serverError().build();
        //
        // } catch (IOException e1) {
        // // TODO(arjuns): Auto-generated catch block
        // e1.printStackTrace();
        // return Response.serverError().build();
        //
        // }

        Entry postedEntry = null;
        try {
            postedEntry =
                Atom10Parser.parseEntry(new BufferedReader(new InputStreamReader(req
                    .getInputStream(), "UTF-8")), null);
        } catch (IllegalArgumentException e1) {
            // TODO(arjuns): Auto-generated catch block
            e1.printStackTrace();
        } catch (UnsupportedEncodingException e1) {
            // TODO(arjuns): Auto-generated catch block
            e1.printStackTrace();
        } catch (JDOMException e1) {
            // TODO(arjuns): Auto-generated catch block
            e1.printStackTrace();
        } catch (IOException e1) {
            // TODO(arjuns): Auto-generated catch block
            e1.printStackTrace();
        } catch (FeedException e1) {
            // TODO(arjuns): Auto-generated catch block
            e1.printStackTrace();
        }

        if (postedEntry == null) {
            return Response.serverError().build();
        }

        // TODO(arjuns) : Fix this.
        String cnxmlDoc =
            CnxAtomPubConstants.getCnxmlDocFromContent((Content) postedEntry.getContents().get(0));

        String resourceMappingDoc =
            CnxAtomPubConstants.getResourceMappingDocFromContent((Content) postedEntry
                .getContents().get(0));

        RepositoryResponse<AddModuleVersionResult> createdModule =
            repositoryService.addModuleVersion(
                atomPubService.getConstants().getRepositoryContext(), moduleId, cnxmlDoc,
                resourceMappingDoc);

        if (createdModule.isOk()) {
            /*
             * TODO(arjuns): Repository service should return following : 1. date.
             */

            AddModuleVersionResult result = createdModule.getResult();
            Entry entry = new Entry();
            entry.setId(result.getModuleId());
            // TODO(arjuns) : See what is the proper value here.
            entry.setId(moduleId + ":" + result.getNewVersionNumber());
            entry.setPublished(new Date());

            // TODO(arjuns) : Refactor this to a function.
            URL editUrl =
                atomPubService.getConstants().getModuleVersionAbsPath(result.getModuleId(),
                    Integer.toString(result.getNewVersionNumber()));
            Link link = new Link();
            link.setRel(CnxAtomPubConstants.LINK_RELATION_EDIT_TAG);
            link.setHref(editUrl.toString());

            entry.setOtherLinks(Lists.newArrayList(link));

            URI createdLocation;
            try {
                URL moduleVersionPath =
                    atomPubService.getConstants().getModuleVersionAbsPath(moduleId,
                        Integer.toString(result.getNewVersionNumber()));
                createdLocation = new URI(moduleVersionPath.toString());

                String stringEntry = PrettyXmlOutputter.prettyXmlOutputEntry(entry);
                logger.info("ResponseEntry = " + stringEntry);
                return Response.created(createdLocation).entity(stringEntry).build();
            } catch (URISyntaxException e) {
                logger.severe("Failed to create Module because : "
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
    @Path(MODULE_VERSION_URL_PATTERN)
    public Response getModuleVersion(@Context HttpServletRequest req,
            @Context HttpServletResponse res, @PathParam(MODULE_ID_PATH_PARAM) String moduleId,
            @PathParam(VERSION_PATH_PARAM) String version) {
        AtomRequest areq = new AtomRequestImpl(req);
        CnxAtomService atomPubService = new CnxAtomService(req);

        RepositoryResponse<GetModuleVersionResult> moduleVersionResult =
            repositoryService.getModuleVersion(
                atomPubService.getConstants().getRepositoryContext(), moduleId, Integer
                    .parseInt(version));

        if (moduleVersionResult.isOk()) {
            GetModuleVersionResult result = moduleVersionResult.getResult();
            String cnxmlDoc = result.getCnxmlDoc();
            String resourceMappingDoc = result.getResourceMapDoc();

            Entry entry = new Entry();
            entry.setId(moduleId + ":" + version);
            entry.setContents(atomPubService.getConstants().getAtomPubListOfContent(cnxmlDoc,
                resourceMappingDoc));
            // TODO(arjuns): Refactor this.

            String stringEntry;
            try {
                stringEntry = PrettyXmlOutputter.prettyXmlOutputMyEntry(entry);
                return Response.ok().entity(stringEntry).build();
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

            // TODO(arjuns) : Way to get Edit-URI.
        }

        return Response.serverError().build();
    }
}
