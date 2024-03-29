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

/**
 * Result of a successful getCollectionVersion() operation.
 * 
 * @author Tal Dayan
 */
public class GetCollectionVersionResult {
    private final String collectionId;
    private final int versionNumber;
    private final String colxmlDoc;

    /**
     * @param collectionId the collection id
     * @param versionNumber the version number of the returned version (1 is first, 2 is second,
     *            etc).
     * @param colxmlDoc the COLXML (XML) doc of this version.
     */
    public GetCollectionVersionResult(String collectionId, int versionNumber, String colxmlDoc) {
        this.collectionId = checkNotNull(collectionId);
        this.versionNumber = versionNumber;
        this.colxmlDoc = checkNotNull(colxmlDoc);
    }

    public String getCollectionId() {
        return collectionId;
    }

    public int getVersionNumber() {
        return versionNumber;
    }

    public String getColxmlDoc() {
        return colxmlDoc;
    }
}
