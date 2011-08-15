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

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletResponse;

/**
 * Java interface for a CNX repository. Implementations of this interface should be
 * 
 * TODO(tal): add support for lenses
 * 
 * TODO(tal): add support for search
 * 
 * TODO(tal): add methods to enumerate resources, modules and lenses.
 * 
 */
public interface CnxRepositoryService {

    /**
     * Return the externally visible configuration of the repository instance.
     */
    CnxRepositoryConfiguration getConfiguration();

    /**
     * Create an empty resource and return information on how to upload its content.
     * 
     * If the response has OK status, the new resource is created and its ID is returned in the
     * result. In addition the repository expects the resource content to be posted at some time in
     * in the future to the upload URL returned in the response.
     * 
     * @param context the query context
     * @return operation response.
     */
    RepositoryResponse<CreateResourceResult> createResource(RepositoryRequestContext context);

    /**
     * Return general information about a resource.
     * 
     * @param context the query context
     * @return operation resourceId a valid resource id
     */
    RepositoryResponse<GetResourceInfoResult> getResourceInfo(RepositoryRequestContext context,
            String resourceId);

    /**
     * Serve resource content.
     * 
     * Send the resource content with its mime type to the given servlet response. This is typically
     * called from a doGet() of a servlet. Service will reutrn a list of Http-headers that user of
     * this service is expected to set before response is returned to clients.
     * 
     * TODO(tal): comment about the state of httpServlet when returning with an error (is the
     * response changed? Does caller need to reset it?)>
     * 
     * @param context the query context
     * @param resourceId a valid id of the resource to serve.
     * @param resp a HTTP servlet response in which the resource is served.
     * @return operation response.
     */
    RepositoryResponse<ServeResourceResult> serveResouce(RepositoryRequestContext context,
            String resourceId, HttpServletResponse httpResponse);

    /**
     * Create a new module.
     * 
     * If the returned response has an OK status than a new module, with no version, has been
     * created and its id is returned in the result. Otherwise, no change is done in the repository.
     * 
     * @param context the query context
     * @return operation response.
     */
    RepositoryResponse<CreateModuleResult> createModule(RepositoryRequestContext context);

    /**
     * Get general module information.
     * 
     * @param context the request context.
     * @param moduleId the target module id
     * @return operation response.
     */
    RepositoryResponse<GetModuleInfoResult> getModuleInfo(RepositoryRequestContext context,
            String moduleId);

    /**
     * Add module version
     * 
     * If the returned response has an OK status than a new version has been added to the module.
     * Otherwise, no change is done in the repository.
     * 
     * The parameter expectedVersionNumber allows to detect and reject submissions that will result
     * in version conflict. Here is a scenario that demostrates it:
     * <ol>
     * <li>A module has 3 versions.</li>
     * <li>User A fetches the latest version of the module (3) for editing.</li>
     * <li>User B fetches the latest version of the module (3) for editing.</li>
     * <li>User B changes module version 3 and submits with expected version 4 (3+1). The submission
     * goes through. The module has now 4 versions.</li>
     * <li>User A changes module version 3 and try to submit it with expected version 4 (3+1). The
     * submission fail due to version conflict because the module already has version 4.</li>
     * <li>User A query the module and finds that it has 4 version. That is, one additional version
     * beyond his base version 3.</li>
     * <li>User A fetches version 4 and merges its modified version 3 with version 4. His base
     * version is now 4.</li>
     * <li>User A submits his changed version with expected version 5. The submission goes through
     * Successfully.</li>
     * </ol>
     * 
     * TODO(tal): define the XML format for the reosurce map.<br>
     * TODO(tal): define extra validation constrainst from the cnxmlDoc.<br>
     * 
     * @param context the query context
     * @param moduleId the module id
     * @param expectedVersionNumber if not null, the operation is rejected if this value does not
     *            equals the the number of previous versions of this module plus one. If null, the
     *            version is added after the existing latest version with no version conflict
     *            verification. If not null, this value should be >= 1.
     * @param cnxmlDoc an XML doc in CNXML format.
     * @param resourceMapDoc an XML doc with resource map for this module version.
     * @return operation response.
     * 
     */
    RepositoryResponse<AddModuleVersionResult> addModuleVersion(RepositoryRequestContext context,
            String moduleId, @Nullable Integer expectedVersionNumber, String cnxmlDoc,
            String resourceMapDoc);

    /**
     * Get the content of a module version.
     * 
     * @param context the request context.
     * @param moduleId the target module id
     * @param moduleVersion the target module version or null for latest version.
     * @return operation response.
     */
    RepositoryResponse<GetModuleVersionResult> getModuleVersion(RepositoryRequestContext context,
            String moduleId, @Nullable Integer moduleVersion);

    /**
     * Get the general information of a module version.
     * 
     * @param context the request context.
     * @param moduleId the target module id
     * @param moduleVersion the target module version or null for latest version.
     * @return operation response.
     */
    RepositoryResponse<GetModuleVersionInfoResult> getModuleVersionInfo(
            RepositoryRequestContext context, String moduleId, @Nullable Integer moduleVersion);

    /**
     * Creates a new collection.
     * 
     * If the returned response has an OK status than a new collection, with no version, has been
     * created and its id is returned in the result. Otherwise, no change is done in the repository.
     * 
     * @param context the query context
     * @return operation response.
     */
    RepositoryResponse<CreateCollectionResult> createCollection(RepositoryRequestContext context);

    /**
     * Get general collection information.
     * 
     * @param context the request context.
     * @param collectionId the target collection id
     * @return operation response.
     */
    RepositoryResponse<GetCollectionInfoResult> getCollectionInfo(RepositoryRequestContext context,
            String collectionId);

    /**
     * Add collection version
     * 
     * If the returned response has an OK status than a new version has been added to the
     * collection. Otherwise, no change is done in the repository.
     * 
     * See {@link #addModuleVersion(RepositoryRequestContext, String, Integer, String, String)} for
     * a discussion of the versioning mechanism and conflict detection.
     * 
     * TODO(tal): define extra requirements from the colxmlDoc.
     * 
     * TODO(tal): break the XML arg into more java manageable parameters (e.g. ACL).
     * 
     * @param context the query context
     * @param collectionId the collection id.
     * @param expectedVersionNumber if not null, the operation is rejected if this value does not
     *            equals the the number of previous versions of this collection plus one. If null,
     *            the version is added after the existing latest version with no version conflict
     *            verification. If not null, this value should be >= 1.
     * @param colxmlDoc an XML doc in COLXML format.
     * @return operation response.
     */
    RepositoryResponse<AddCollectionVersionResult> addCollectionVersion(
            RepositoryRequestContext context, String collectionId,
            @Nullable Integer expectedVersionNumber, String colxmlDoc);

    /**
     * Get the content of a collection version.
     * 
     * 
     * @param context the request context.
     * @param collectionId the target collection id
     * @param collectionVersion the target collection version or null for latest version.
     * 
     * @return operation response.
     */
    RepositoryResponse<GetCollectionVersionResult> getCollectionVersion(
            RepositoryRequestContext context, String collectionId,
            @Nullable Integer collectionVersion);

    /**
     * Get the information of a collection version.
     * 
     * @param context the request context.
     * @param collectionId the target collection id
     * @param collectionVersion the target collection version or null for latest version.
     * @return operation response.
     */
    RepositoryResponse<GetCollectionVersionInfoResult> getCollectionVersionInfo(
            RepositoryRequestContext context, String collectionId,
            @Nullable Integer collectionVersion);

    /**
     * Get an upload URL to attach an export to an entity.
     * 
     * The actual export is not published until it content is actually posted successfully. If this
     * export already exists, the new content overwrites the existing one.
     * 
     * @param context the request context.
     * @param exportReference reference to the export to be uploaded.
     * 
     * @return operation response.
     */
    RepositoryResponse<GetExportUploadUrlResult> getExportUploadUrl(
            RepositoryRequestContext context, ExportReference exportReference);

    /**
     * Serve export content.
     * 
     * Send the export content with its content type to the given servlet response. This is
     * typically called from a doGet() of a servlet. If the returned status is OK, no further action
     * is required from the servlet.
     * 
     * TODO(tal): comment about the state of httpServlet when returning with an error (is the
     * response changed? Does caller need to reset it?)>
     * 
     * @param context the query context
     * @param exportRererence reference to the export to be served
     * @param httpResponse the HttpResponse to which the export should be served.
     */
    RepositoryResponse<ServeExportResult> serveExport(RepositoryRequestContext context,
            ExportReference exportReference, HttpServletResponse httpResponse);

    /**
     * Delete an export.
     * 
     * @param context the request context.
     * @param exportReference reference to the export to be deleted.
     * @return operation response.
     */
    RepositoryResponse<DeleteExportResult> deleteExport(RepositoryRequestContext context,
            ExportReference exportReference);
}
