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

import javax.servlet.http.HttpServletResponse;

/**
 * Java interface for a CNX repository. Implementations of this interface should be
 * 
 */
public interface CnxRepositoryService {
    /**
     * Create an empty resource and return information on how to upload its content.
     * 
     * If the response has OK status, the new resource is created and its ID is returned in the
     * result. In addition the repository expects the resource content to be posted at some to the
     * upload URL returned in the response.
     * 
     * @param context the query context
     * @return operation response.
     */
    RepositoryResponse<CreateResourceResult> CreateResource(RepositoryRequestContext context);

    /**
     * Return general information about a resource.
     * 
     * @param context the query context
     * @return operation resourceId a valid resource id
     */
    RepositoryResponse<GetResourceInfoResult> GetResourceInfo(RepositoryRequestContext context,
        String resourceId);

    /**
     * Serve resource content.
     * 
     * Send the resource content with its mime type to the given servlet response. This is typically
     * called from a doGet() of a servlet. If the returned status is OK, no further action is
     * required from the servlet.
     * 
     * TODO(tal): comment about the state of httpServlet when returning with an error (is the
     * response changed? Does caller need to reset it?)>
     * 
     * @param context the query context
     * @param resourceId a valid id of the resource to serve.
     * @param resp a HTTP servlet response in which the resource is served.
     * @return operation response.
     */
    RepositoryResponse<ServeResourceResult> ServeResouce(RepositoryRequestContext context,
        String resourceId, HttpServletResponse httpResponse);
}
