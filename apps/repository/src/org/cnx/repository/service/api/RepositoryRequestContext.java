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

import java.net.MalformedURLException;
import java.net.URL;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;

/**
 * Common context that is passed to each repository operation.
 * 
 * @author Tal Dayan
 */
public class RepositoryRequestContext {

    @Nullable
    public final String authenticatedUserId;

    /**
     * @param authenticatedUserId an optional string with the user id. Null if no user id is
     *            associated with this request. It is the responsibility of the caller to
     *            authenticate the user. The repository service uses this value to authorize the
     *            operation.
     */
    public RepositoryRequestContext(@Nullable String authenticatedUserId) {
        this.authenticatedUserId = authenticatedUserId;
    }

    @Nullable
    public String getAuthenticatedUserId() {
        return authenticatedUserId;
    }
}
