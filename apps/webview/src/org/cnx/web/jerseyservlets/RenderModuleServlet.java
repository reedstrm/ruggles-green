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
package org.cnx.web.jerseyservlets;

import static org.cnx.web.CommonHack.fetchFromRepositoryAndReturn;
import static org.cnx.web.CommonHack.handleCnxInvalidUrlException;

import com.sun.syndication.io.FeedException;
import java.net.URISyntaxException;
import org.cnx.common.exceptions.CnxException;

import org.cnx.common.repository.atompub.objects.ModuleVersionWrapper;

import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.template.soy.data.SoyMapData;
import com.google.template.soy.tofu.SoyTofu;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.xml.parsers.SAXParser;
import org.cnx.atompubclient2.CnxClient;
import org.cnx.cnxml.Module;
import org.cnx.cnxml.ModuleFactory;
import org.cnx.cnxml.ModuleHtmlGenerator;
import org.cnx.common.exceptions.CnxInvalidUrlException;
import org.cnx.common.repository.ContentType;
import org.cnx.common.repository.atompub.IdWrapper;
import org.cnx.common.repository.atompub.ServletUris;
import org.cnx.common.repository.atompub.VersionWrapper;
import org.cnx.mdml.Actor;
import org.cnx.util.RenderScope;
import org.cnx.web.CommonHack;
import org.cnx.web.Utils;
import org.cnx.web.WebViewConfiguration;
import org.cnx.web.WebViewTemplate;
import org.jdom.JDOMException;
import org.xml.sax.SAXException;

/**
 * Servlet to Handle CNX Modules.
 * 
 * @author Arjun Satyapal
 */
@Path(ServletUris.Module.MODULE_SERVLET)
public class RenderModuleServlet {
    private final Injector injector;
    private final SAXParser saxParser;
    // TODO(arjuns) : Move this to a better place.
    private final CnxClient cnxClient;
    private final WebViewConfiguration configuration;

    public RenderModuleServlet(@Context ServletContext context) {
        URL url = null;
        try {
            injector = (Injector) context.getAttribute(Injector.class.getName());
            configuration = injector.getInstance(WebViewConfiguration.class);
            saxParser = injector.getInstance(SAXParser.class);
            url = new URL(configuration.getRepositoryAtomPubUrl());
            cnxClient = new CnxClient(url);

        } catch (Exception e) {
            // TODO(arjuns): Auto-generated catch block
            throw new RuntimeException(e);
        }
    }

    /**
     * When client does HTTP-GET on
     * {@link org.cnx.common.repository.atompub.ServletUris.Module#MODULE_VERSION_PATH}, then this
     * method is triggered.
     * 
     * Purpose of this method is to fetch CNXML and ResourceMapping Doc from Repository, then create
     * a HTML and then serve it back to client.
     * 
     * @param moduleId Id of desired Module.
     * @param moduleVersionString Version of desiredModule.
     * @return Response which contains HTML representation of the desired module.
     * @throws IOException
     * @throws JDOMException
     * @throws SAXException
     * @throws CnxException
     * @throws FeedException
     * @throws URISyntaxException
     * @throws IllegalArgumentException
     * @throws IllegalStateException
     */
    @GET
    @Produces(ContentType.TEXT_HTML_UTF8)
    @Path(ServletUris.Module.MODULE_VERSION_PATH)
    public Response getModuleVersion(@PathParam(ServletUris.MODULE_ID_PATH_PARAM) String moduleId,
            @PathParam(ServletUris.MODULE_VERSION_PATH_PARAM) String moduleVersionString)
            throws JDOMException, IOException, SAXException, IllegalStateException,
            IllegalArgumentException, URISyntaxException, FeedException, CnxException {
        // TODO(arjuns) : Handle exception.
        StringBuilder builder = new StringBuilder();

        final IdWrapper idWrapper = new IdWrapper(moduleId, IdWrapper.Type.MODULE);
        final VersionWrapper versionWrapper = new VersionWrapper(moduleVersionString);

        String finalHtml = null;

        if (finalHtml == null) {
            // TODO(arjuns) : Add a URL for accessing resources with HTTP redirect.

            ModuleVersionWrapper moduleVersionWrapper = null;
            try {
                moduleVersionWrapper = cnxClient.getModuleVersion(idWrapper, versionWrapper);
            } catch (CnxInvalidUrlException e) {
                handleCnxInvalidUrlException(idWrapper, e);
            }

            final Module module =
                    injector.getInstance(ModuleFactory.class).create(
                            moduleId,
                            moduleVersionString,
                            CommonHack.parseXmlString(saxParser, moduleVersionWrapper.getCnxml()),
                            CommonHack.getResourcesFromResourceMappingDoc(moduleVersionWrapper
                                    .getResourceMappingXml()));

            RenderScope renderScope = injector.getInstance(RenderScope.class);

            // Render content
            String title, contentHtml;
            List<Actor> authors;
            renderScope.enter();
            try {
                renderScope.seed(Module.class, module);
                title = module.getTitle();
                if (module.getMetadata() != null) {
                    authors = module.getMetadata().getAuthors();
                } else {
                    authors = Collections.<Actor> emptyList();
                }
                ModuleHtmlGenerator generator = injector.getInstance(ModuleHtmlGenerator.class);
                contentHtml = generator.generate(module);
            } catch (Exception e) {
                // TODO(arjuns) : Handle Exception.
                throw new RuntimeException(e);
            } finally {
                renderScope.exit();
            }
            final SoyMapData params =
                    new SoyMapData("module", new SoyMapData("id", moduleId, "version",
                            moduleVersionString, "title", title, "authors",
                            Utils.convertActorListToSoyData(authors), "contentHtml", contentHtml));

            SoyTofu tofu = injector.getInstance(Key.get(SoyTofu.class, WebViewTemplate.class));

            finalHtml = tofu.render(CommonHack.MODULE_TEMPLATE_NAME, params, null);
        }

        builder.append(finalHtml);

        ResponseBuilder myresponse = Response.ok();
        myresponse.entity(builder.toString());
        return myresponse.build();
    }

    /**
     * When client does HTTP-GET on
     * {@link org.cnx.common.repository.atompub.ServletUris.Module#MODULE_VERSION_CNXML}, then this
     * method is triggered.
     * 
     * This is a helper method which is used for providing CNXML of desired module to client.
     * 
     * @param moduleId Id of desired Module.
     * @param moduleVersionString Version of desiredModule.
     * @return Response containing CNXML.
     * @throws IOException
     */
    @GET
    @Produces(ContentType.TEXT_XML_UTF8)
    @Path(ServletUris.Module.MODULE_VERSION_CNXML)
    public Response getModuleVersionXml(
            @PathParam(ServletUris.MODULE_ID_PATH_PARAM) String moduleId,
            @PathParam(ServletUris.MODULE_VERSION_PATH_PARAM) String moduleVersionString)
            throws IOException {
        // TODO(arjuns) : Handle exceptions.
        final IdWrapper idWrapper = new IdWrapper(moduleId, IdWrapper.Type.MODULE);
        final VersionWrapper versionWrapper = new VersionWrapper(moduleVersionString);
        URL url = cnxClient.getConstants().getModuleVersionXmlAbsPath(idWrapper, versionWrapper);

        return fetchFromRepositoryAndReturn(url);
    }

    /**
     * When client does HTTP-GET on
     * {@link org.cnx.common.repository.atompub.ServletUris.Module#MODULE_VERSION_RESOURCE_MAPPING},
     * then this method is triggered.
     * 
     * This is a helper method which is used for providing ResourceMappingXml of desired module to
     * client.
     * 
     * @param moduleId Id of desired Module.
     * @param moduleVersionString Version of desiredModule.
     * @return Response containing CNXML.
     * @throws IOException
     */
    @GET
    @Produces(ContentType.TEXT_XML_UTF8)
    @Path(ServletUris.Module.MODULE_VERSION_RESOURCE_MAPPING)
    public Response getModuleVersionResourcesXml(
            @PathParam(ServletUris.MODULE_ID_PATH_PARAM) String moduleId,
            @PathParam(ServletUris.MODULE_VERSION_PATH_PARAM) String moduleVersionString)
            throws IOException {
        final IdWrapper idWrapper = new IdWrapper(moduleId, IdWrapper.Type.MODULE);
        final VersionWrapper versionWrapper = new VersionWrapper(moduleVersionString);
        URL url =
                cnxClient.getConstants().getModuleVersionResourceMappingAbsPath(idWrapper,
                        versionWrapper);

        return fetchFromRepositoryAndReturn(url);
    }
}
