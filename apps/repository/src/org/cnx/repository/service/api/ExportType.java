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

import java.util.Set;
import java.util.regex.Pattern;

import com.google.appengine.repackaged.com.google.common.collect.ImmutableSet;

/**
 * An immutable data object representing a repository export type.
 * 
 * @author Tal Dayan
 */
public class ExportType {

    private static final Pattern EXPORT_TYPE_ID_PATTERN = Pattern.compile("[a-zA-Z0-9._]+");

    private final String id;
    private final String contentType;
    private final long maxSizeInBytes;
    private final Set<ExportScopeType> allowedScopeTypes;

    /**
     * @param id export type id. This is a stable and non empty web safe string that is unique (case
     *            sensitive) among all export types ever supported.
     * @param contentType content type of this export type. e.g. "application/pdf" for PDF. content
     *            type is not unique in the sense that multiple export types may have the same
     *            content type. Only content of this type is accepted by the system for this export
     *            type.
     * @param maxSizeInBytes the max export blob size that is accepted by the repository. Larger
     *            exports are rejected upon upload, though larger exports may already exist in the
     *            repository if this value used to be larger in the past.
     * @param allowedScopeTypes the set of scopes to which this export type can be attached. OK to
     *            have an empty set.
     */
    public ExportType(String id, String contentType, long maxSizeInBytes,
        Set<ExportScopeType> allowedScopeTypes) {
        checkArgument(EXPORT_TYPE_ID_PATTERN.matcher(id).matches(), "Invalid export type id: [%s]",
                id);
        checkArgument(maxSizeInBytes > 0, "Invalid max size in bytes: %s", maxSizeInBytes);
        this.id = checkNotNull(id);
        this.contentType = checkNotNull(contentType);
        this.maxSizeInBytes = maxSizeInBytes;
        this.allowedScopeTypes = ImmutableSet.copyOf(allowedScopeTypes);
    }

    public String getId() {
        return id;
    }

    public String getContentType() {
        return contentType;
    }

    public long getMaxSizeInBytes() {
        return maxSizeInBytes;
    }

    public Set<ExportScopeType> getAllowedScopeTypes() {
        return allowedScopeTypes;
    }
}
