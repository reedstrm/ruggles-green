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

import com.google.common.base.Preconditions;

import org.cnx.repository.common.KeyValue;

import java.util.List;

/**
 * Result of a successful serveResource() operation.
 *
 * @author Tal Dayan
 */
public class ServeResourceResult {
    // TODO(tal): any attribute to return (size, type, etc)?

    // List of headers to be set by Repository Service client.
    private final List<KeyValue> listOfHeaders;

    public List<KeyValue> getListOfHeaders() {
        return listOfHeaders;
    }

    public ServeResourceResult(List<KeyValue> listOfHeaders) {
        Preconditions.checkNotNull(listOfHeaders);
        // At present, CNXService implementation is expected to reutrn one Httpheader for BlobKey.
        Preconditions.checkArgument(listOfHeaders.size() == 1);
        this.listOfHeaders = listOfHeaders;
    }
}
