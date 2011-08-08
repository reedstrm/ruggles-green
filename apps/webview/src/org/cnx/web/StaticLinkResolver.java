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

import com.google.inject.Singleton;
import com.google.inject.Inject;
import com.google.inject.Provider;
import java.net.URI;
import javax.annotation.Nullable;
import org.cnx.cnxml.LinkResolver;
import org.cnx.common.collxml.Collection;
import org.cnx.util.RenderTime;

@Singleton public class StaticLinkResolver implements LinkResolver {
    private static final String BASE = "/staticxml/";
    private static final String DOCUMENT_BASE = "/light/module/";
    private static final String COLLECTION_BASE = "/light/collection/";
    private final Provider<Collection> collectionProvider;

    @Inject public StaticLinkResolver(Provider<Collection> collectionProvider) {
        this.collectionProvider = collectionProvider;
    }

    @Override public URI resolveURI(URI uri) throws Exception {
        return new URI(BASE).resolve(uri);
    }

    @Override public URI resolveDocument(String document, @Nullable String version)
            throws Exception {
        final Collection collection = collectionProvider.get();
        if (version == null) {
            version = "latest";
        }
        if (collection != null && collection.hasModule(document)) {
            // TODO(light): collection version
            final String collectionVersion = "latest";
            return new URI(COLLECTION_BASE).resolve(new URI(
                        collection.getId() + "/" + collectionVersion +
                        "/module/" + document + "/" + version + "/"));
        }
        return new URI(DOCUMENT_BASE).resolve(new URI(document + "/" + version + "/"));
    }

    @Override public URI resolveResource(@Nullable String document, @Nullable String version,
            String resource) throws Exception {
        return new URI(BASE).resolve(new URI(resource));
    }
}
