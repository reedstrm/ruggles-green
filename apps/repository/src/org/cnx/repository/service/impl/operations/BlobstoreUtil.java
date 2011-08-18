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

package org.cnx.repository.service.impl.operations;

/**
 * Blobstore related utils.
 * 
 * @author Tal Dayan
 */
public class BlobstoreUtil {

    /*
     * When serving a blob, blobstore service sets a header with "BlobKey = <value>" and then App
     * Engine replaces the body of the response with the content of the blob. For technical reasons,
     * when using the AtomPub API, this setting need to be done outside of the repository service,
     * using the additional headers field of its result.
     * 
     * NOTE(tal): this header name MUST match the header name used by blobstore.
     */
    public static final String BLOB_KEY_HEADER_NAME = "BlobKey";

}