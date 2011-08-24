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

package org.cnx.repository.service.api;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

/**
 * Result of a successful serveExport() operation.
 * 
 * @author Tal Dayan
 */
public class ServeExportResult {

    private ImmutableMap<String, String> additionalHeaders;

    public ServeExportResult(ImmutableMap<String, String> additionalHeaders) {
        Preconditions.checkNotNull(additionalHeaders);
        this.additionalHeaders = checkNotNull(additionalHeaders);
    }

    /**
     * Name value map of additional HTTP headers.
     * 
     * Zero or more HTTP header name/value pairs that must be applied to the HTTP response by the
     * caller. These headers are in additional to other headers that the repository service may
     * already applied to the HTTP response.
     * 
     * NOTE(tal): in some implementations of the CNX repository service, the service may not be able
     * to set some response headers due internal technical limitations (e.g. serving from blobstore
     * in Google App Engine).
     */
    public ImmutableMap<String, String> getAdditionalHeaders() {
        return additionalHeaders;
    }
}
