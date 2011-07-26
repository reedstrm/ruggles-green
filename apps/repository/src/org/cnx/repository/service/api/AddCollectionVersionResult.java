/*
 * Copyright 2011 Google Inc.
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
 * Result of a successful addCollectionVersion() operation.
 * 
 * @author Tal Dayan
 */
public class AddCollectionVersionResult {
    private final String collectionId;
    private final int newVersionNumber;

    /**
     * @param collectionId the collection id
     * @param newVersionNumber the version number of the new version (1 is first, 2 is second, etc).
     */
    public AddCollectionVersionResult(String collectionId, int newVersionNumber) {
        this.collectionId = checkNotNull(collectionId);
        this.newVersionNumber = newVersionNumber;
    }

    public String getCollectionId() {
        return collectionId;
    }

    public int getNewVersionNumber() {
        return newVersionNumber;
    }
}
