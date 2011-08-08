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
package org.cnx.repository.atompub.utils;

import javax.servlet.http.HttpServletRequest;

import org.cnx.repository.service.api.RepositoryRequestContext;

/**
 * 
 * @author Arjun Satyapal
 */
public class RepositoryUtils {
    // TODO(arjuns) : remove this.
    /** Temporary global userId. */
    public static final String GLOBAL_USER_ID = "temp_user";

    public static RepositoryRequestContext getRepositoryContext(HttpServletRequest httpRequest) {
        return new RepositoryRequestContext(httpRequest, GLOBAL_USER_ID);
    }
}
