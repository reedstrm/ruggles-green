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

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.internal.Nullable;
import com.google.inject.name.Named;
import java.net.URI;
import org.cnx.html.RenderTime;
import org.cnx.html.ResourceResolver;

/**
 *  DemoResourceResolver converts resource links to abstract URIs.  It is not intended for
 *  production use.
 */
@RenderTime public class DemoResourceResolver implements ResourceResolver {
    private Provider<String> moduleIdProvider;

    @Inject public DemoResourceResolver(@Named("moduleId") Provider<String> provider) {
        this.moduleIdProvider = provider;
    }

    public URI resolveURI(URI uri) throws Exception {
        if (uri.isAbsolute() || ("".equals(uri.getPath()) && !"".equals(uri.getFragment()))) {
            // Absolute URL or fragment
            return uri;
        }
        return resolveResource(null, null, uri.getPath());
    }

    public URI resolveDocument(String document, String version) throws Exception {
        if (document == null) {
            document = moduleIdProvider.get();
            if (document == null) {
                throw new IllegalStateException("No module ID provided");
            }
        }
        if (version == null) {
            version = "latest";
        }
        final URI uri = new URI("http://cnx.org/content/" + document + "/" + version + "/");
        return uri;
    }

    public URI resolveResource(String document, String version, String resource) throws Exception {
        return resolveDocument(document, version).resolve(resource);
    }
}
