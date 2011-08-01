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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Result of a successful getCollectionInfo() operation.
 * 
 * @author Tal Dayan
 */
public class GetCollectionInfoResult {
    private final String collectionId;
    private final int versionCount;

    public GetCollectionInfoResult(String collectionId, int versionCount) {
        checkArgument(versionCount >= 0, "Negative version count: %s", versionCount);
        this.collectionId = checkNotNull(collectionId);
        this.versionCount = versionCount;
    }

    public String getCollectionId() {
        return collectionId;
    }

    public int getVersionCount() {
        return versionCount;
    }
}
