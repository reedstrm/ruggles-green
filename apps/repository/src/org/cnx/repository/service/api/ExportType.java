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

import java.util.Set;

import com.google.appengine.repackaged.com.google.common.collect.ImmutableSet;

/**
 * An immutable data object representing a repository export type.
 * 
 * @author Tal Dayan
 */
public class ExportType {

    private final String id;
    private final String contentType;
    private final Set<ExportScopeType> allowedScopeTypes;

    /**
     * @param id export type id. This is a stable and non empty web safe string that is unique (case
     *            sensitive) among all export types ever supported.
     * @param contentType content type of this export type. e.g. "application/pdf" for PDF. content
     *            type is not unique in the sense that multiple export types may have the same
     *            content type. Only content of this type is accepted by the system for this export
     *            type.
     * @param allowedScopeTypes the set of scopes to which this export type can be attached. OK to
     *            have an empty set.
     */
    public ExportType(String id, String contentType, Set<ExportScopeType> allowedScopeTypes) {
        // TODO(tal): assert that id is a web safe string (a-zA-Z._-0-9).
        this.id = checkNotNull(id);
        this.contentType = checkNotNull(contentType);
        this.allowedScopeTypes = ImmutableSet.copyOf(allowedScopeTypes);
    }

    public String getId() {
        return id;
    }

    public String getContentType() {
        return contentType;
    }

    public Set<ExportScopeType> getAllowedScopeTypes() {
        return allowedScopeTypes;
    }
}