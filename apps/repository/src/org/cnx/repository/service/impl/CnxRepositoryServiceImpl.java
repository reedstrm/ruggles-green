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

package org.cnx.repository.service.impl;

import javax.servlet.http.HttpServletResponse;

import org.cnx.repository.service.api.CnxRepositoryService;
import org.cnx.repository.service.api.CreateResourceResult;
import org.cnx.repository.service.api.GetResourceInfoResult;
import org.cnx.repository.service.api.RepositoryRequestContext;
import org.cnx.repository.service.api.RepositoryResponse;
import org.cnx.repository.service.api.ServeResourceResult;

/**
 * Implementation of the repository service for Google App Engine.
 * 
 * TODO(tal): (many places in many files) make sure we log all the interesting events.
 * 
 * @author Tal Dayan
 * 
 */
public class CnxRepositoryServiceImpl implements CnxRepositoryService {

    private final static CnxRepositoryServiceImpl instance = new CnxRepositoryServiceImpl();

    @Override
    public RepositoryResponse<CreateResourceResult> CreateResource(RepositoryRequestContext context) {
        return ResourceOperations.CreateResource(context);
    }

    @Override
    public RepositoryResponse<GetResourceInfoResult> GetResourceInfo(
        RepositoryRequestContext context, String resourceId) {
        return ResourceOperations.GetResourceInfo(context, resourceId);
    }

    @Override
    public RepositoryResponse<ServeResourceResult> ServeResouce(RepositoryRequestContext context,
        String resourceId, HttpServletResponse httpResponse) {
        return ResourceOperations.ServeResouce(context, resourceId, httpResponse);
    }

    /**
     * Get a repository service instance.
     * 
     * The instance is reentrant and thread safe such that a single instance is sufficient for an
     * entire application.
     * 
     * @return a repository service instance. The returned instance is not necessarily unique every
     *         call.
     */
    public static CnxRepositoryService getService() {
        return instance;
    }
}
