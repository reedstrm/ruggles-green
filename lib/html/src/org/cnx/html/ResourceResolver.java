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

package org.cnx.html;

import java.net.URI;

/**
 *  A ResourceResolver looks up resources and returns URIs.
 */
public interface ResourceResolver {
    public URI resolveURI(URI uri) throws Exception;
    public URI resolveDocument(String document, String version) throws Exception;
    public URI resolveResource(String document, String version, String resource) throws Exception;
}
