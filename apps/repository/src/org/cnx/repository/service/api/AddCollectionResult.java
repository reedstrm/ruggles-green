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
 * Result of a successful collection creation operation.
 * 
 * @author Tal Dayan
 */
public class AddCollectionResult {

    private final String collectionId;

    /**
     * @param collectionId the id of the new collection. This is a web safe string that is unique
     *            among all the collection IDs.
     */
    public AddCollectionResult(String collectionId) {
        this.collectionId = checkNotNull(collectionId);
    }

    public String getCollectionId() {
        return collectionId;
    }
}
