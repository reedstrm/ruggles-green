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
import java.net.URI;
import org.cnx.cnxml.ResourceResolver;

@Singleton public class StaticResourceResolver implements ResourceResolver {
    private static final String BASE = "/staticxml/";
    private static final String DOCUMENT_BASE = "/light/module/";

    public URI resolveURI(URI uri) throws Exception {
        return new URI(BASE).resolve(uri);
    }

    public URI resolveDocument(String document, String version) throws Exception {
        return new URI(DOCUMENT_BASE).resolve(new URI(document + "/" + version + "/"));
    }

    public URI resolveResource(String document, String version, String resource) throws Exception {
        return new URI(BASE).resolve(new URI(resource));
    }
}
