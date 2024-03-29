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

import javax.annotation.Nullable;

import com.google.appengine.repackaged.com.google.common.collect.ImmutableList;

/**
 * Result of a successful
 * {@link CnxRepositoryService#getResourceList(RepositoryRequestContext, String, int)} operation.
 * 
 * @author Tal Dayan
 */
public class GetResourceListResult {

    private final ImmutableList<String> resourceIds;

    @Nullable
    private final String endCursor;

    /**
     * @param resourceIds the result list.
     * @param endCursor if null, end of module list has been reached. Otherwise, can be used in a
     *            successive call to fetch the next chunk of modules.
     */
    public GetResourceListResult(ImmutableList<String> resourceIds, String endCursor) {
        this.resourceIds = checkNotNull(resourceIds);
        this.endCursor = endCursor;
    }

    /**
     * Return end cursor to use in successive query as start cursor.
     * 
     * The method asserts that {@link #isLast()} is false.
     */
    public String getEndCursor() {
        checkNotNull(endCursor, "No end cursor, at end of data");
        return endCursor;
    }

    /**
     * Test if end of resource list has been reached.
     */
    public boolean isLast() {
        return (endCursor == null);
    }

    public ImmutableList<String> getResourceIds() {
        return resourceIds;
    }
}
