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
package org.cnx.web.servlets;

import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.xml.parsers.DocumentBuilder;

import org.cnx.atompubclient.CnxAtomPubClient;
import org.cnx.cnxml.Module;
import org.cnx.cnxml.ModuleFactory;
import org.cnx.cnxml.ModuleHTMLGenerator;
import org.cnx.mdml.Actor;
import org.cnx.repository.atompub.CnxMediaTypes;
import org.cnx.repository.atompub.VersionWrapper;
import org.cnx.resourcemapping.Resources;
import org.cnx.util.RenderScope;
import org.cnx.web.CommonHack;
import org.cnx.web.Utils;
import org.cnx.web.WebViewConfiguration;
import org.cnx.web.WebViewTemplate;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.template.soy.data.SoyMapData;
import com.google.template.soy.tofu.SoyTofu;
import com.sun.syndication.propono.atom.client.ClientEntry;

/**
 * Servlet to Handle CNX Resources.
 *
 * @author Arjun Satyapal
 */
// TODO(arjuns) : Fix this hardcoding.
@Path("/module")
public class RenderModuleServlet {
    private static final Logger logger = Logger.getLogger(RenderModuleServlet.class.getName());

    static final String MODULE_ID_PATH_PARAM = "moduleId";
    // TODO(arjuns) : move these to common.
    static final String MODULE_VERSION_PATH_PARAM = "moduleVersion";
    static final String LATEST_VERSION = "latest";

    /**
     * This will fetch the specific version. Possible value for version = [{version}, {latest}]
     *
     * URL Pattern wrt /module = /<moduleId>/<version>
     */
    static final String MODULE_VERSION_URL_PATTERN = "/{" + MODULE_ID_PATH_PARAM + "}/{"
        + MODULE_VERSION_PATH_PARAM + "}";

    private Injector injector;
    // TODO(arjuns) : Move this to a better place.
    private CnxAtomPubClient cnxClient;
    private WebViewConfiguration configuration;

    public RenderModuleServlet(@Context ServletContext context) {
        URL url = null;
        try {
            injector = (Injector)context.getAttribute(Injector.class.getName());
            configuration = injector.getInstance(WebViewConfiguration.class);
            url = new URL(configuration.getRepositoryAtomPubUrl());
            cnxClient = new CnxAtomPubClient(url);

        } catch (Exception e) {
            // TODO(arjuns): Auto-generated catch block
            throw new RuntimeException(e);
        }
    }

    @GET
    @Produces(CnxMediaTypes.TEXT_HTML_UTF8)
    @Path(MODULE_VERSION_URL_PATTERN)
    public Response getModuleVersion(@Context HttpServletRequest req,
            @Context HttpServletResponse res,
            @Context ServletContext context,
            @PathParam(MODULE_ID_PATH_PARAM) String moduleId,
            @PathParam(MODULE_VERSION_PATH_PARAM) String moduleVersionString) throws Exception {

        WebViewConfiguration configuration = new WebViewConfiguration();


        // TODO(arjuns) : Handle exception.
        StringBuilder builder = new StringBuilder();

        if (!VersionWrapper.isValidVersion(moduleVersionString)) {
            return Response.status(Status.NOT_FOUND).build();
        }
        final Injector injector = (Injector)context.getAttribute(Injector.class.getName());

        final VersionWrapper moduleVersionInt = new VersionWrapper(moduleVersionString);

        final String moduleContentHtmlCacheKey = "moduleContentHtml " + " " + moduleId;

        String finalHtml = null;

        if (finalHtml == null) {
            // TODO(arjuns) : Add a URL for accessing resources with HTTP redirect.
            ClientEntry moduleVersionEntry =
                cnxClient.getModuleVersionEntry(moduleId, moduleVersionInt);
            String cnxml = cnxClient.getCnxml(moduleVersionEntry);
            String resourceMappingXml = cnxClient.getResourceMappingXml(moduleVersionEntry);

            Module module = injector.getInstance(ModuleFactory.class).create(
                    moduleId,
                    CommonHack.parseXmlString(injector.getInstance(DocumentBuilder.class), cnxml),
                    CommonHack.getResourcesFromResourceMappingDoc(resourceMappingXml));

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
                ModuleHTMLGenerator generator = injector.getInstance(ModuleHTMLGenerator.class);
                contentHtml = generator.generate(module);
            } catch (Exception e) {
                // TODO(arjuns) : Handle Exception.
                throw new RuntimeException(e);
            } finally {
                renderScope.exit();
            }
            final SoyMapData params = new SoyMapData(
                    "module", new SoyMapData(
                            "id", moduleId,
                            "version", moduleVersionString,
                            "title", title,
                            "authors", Utils.convertActorListToSoyData(authors),
                            "contentHtml", contentHtml));

            SoyTofu tofu = injector.getInstance(Key.get(SoyTofu.class, WebViewTemplate.class));

            finalHtml = tofu.render(CommonHack.MODULE_TEMPLATE_NAME, params, null);
        }

        builder.append(finalHtml);

        ResponseBuilder myresponse = Response.ok();
        myresponse.entity(builder.toString());
        return myresponse.build();
    }

}
