/*
 *  Copyright 2011 Google Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.cnx.web;

import java.net.URI;
import java.util.logging.Logger;

import javax.annotation.Nullable;

import org.cnx.cnxml.LinkResolver;
import org.cnx.cnxml.Module;
import org.cnx.common.collxml.Collection;
import org.cnx.repository.atompub.CnxAtomPubConstants;
import org.cnx.resourcemapping.Resource;
import org.cnx.resourcemapping.Resources;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

/**
 * Resolves links for Modules and Collections.
 *
 * @author Arjun Satyapal
 */
@Singleton
public class WebViewLinkResolver implements LinkResolver {
    private final static Logger logger = Logger.getLogger(WebViewLinkResolver.class.getName());

    private final Provider<Collection> collectionProvider;
    private final Provider<Module> moduleProvider;
    private final WebViewConfiguration configuration;

    @Inject
    public WebViewLinkResolver(Provider<Collection> collectionProvider,
        Provider<Module> moduleProvider, WebViewConfiguration configuration) {
        this.collectionProvider = collectionProvider;
        this.moduleProvider = moduleProvider;
        this.configuration = configuration;
    }

    @Override
    public URI resolveURI(URI uri) throws Exception {
        if (uri.isAbsolute() || uri.toString().startsWith("#")) {
            return uri;
        } else {
            // TODO(arjuns) : ensure that this is not called in context of collection parsing..
            return resolveResource(null, null, uri.toString());
        }
    }

    @Override
    public URI resolveDocument(String moduleId, @Nullable String moduleVersion) throws Exception {
        final Collection collection = collectionProvider.get();

        if (moduleVersion == null) {
            moduleVersion = CnxAtomPubConstants.LATEST_VERSION_STRING;
        }

        StringBuilder uriBuilder = new StringBuilder(CommonHack.CONTENT_NAME_SPACE);
        if (collection != null && collection.hasModule(moduleId)) {
            // TODO(arjuns): TODO(light) collection version
            final String collectionVersion = "1";

            uriBuilder
                    .append("/collection/")
                    .append(collection.getId())
                    .append("/")
                    .append(collectionVersion);
        }

        // TODO(arjuns) : See how to get rid of this string.
        uriBuilder.append("/module/").append(moduleId).append("/").append(moduleVersion);
        return new URI(uriBuilder.toString());
    }

    @Override
    public URI resolveResource(@Nullable String document, @Nullable String version, String resource)
            throws Exception {
        if (document == null && version == null) {
            final Resources resources = moduleProvider.get().getResources();
            for (Resource currResource : resources.getResource()) {
                if (currResource.getName().equals(resource)) {
                    final String resourceId =
                            currResource.getLocationInformation().getRepository().getResourceId();
                    return new URI(configuration.getRepositoryAtomPubUrl()
                            + "/resource/" + resourceId);
                }
            }
            logger.severe("****Returning badUrl : " + resource);
            return new URI(resource);
        }

        // TODO(light): handle resources for other documents
        return null;
    }
}
