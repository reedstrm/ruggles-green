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

package org.cnx.cnxml;

import java.net.URI;
import javax.annotation.Nullable;

/**
 *  A LinkResolver looks up abstract links and returns URIs.
 */
public interface LinkResolver {
    /**
     *  Resolve a URI into another URI.
     *  <p>
     *  URIs may be any valid URI.  Returning the argument is always acceptable.
     */
    public URI resolveUri(URI uri) throws Exception;

    /**
     *  Resolve a document into a URI.
     *  <p>
     *  One of the parameters will not be null.
     *
     *  @param document A document identifier
     *  @param version A version string
     */
    public URI resolveDocument(@Nullable String document, @Nullable String version) throws Exception;

    /**
     *  Resolve a resource into a URI.
     *
     *  @param document A document identifier
     *  @param version A version string
     *  @param resource A relative URI for the resource
     */
    public URI resolveResource(@Nullable String document, @Nullable String version, URI resource)
            throws Exception;
}
