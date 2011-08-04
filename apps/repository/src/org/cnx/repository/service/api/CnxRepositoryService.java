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

import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.cnx.util.Nullable;

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
     * TODO(tal): define the XML format for the reosurce map.
     *
     * TODO(tal): define extra requriements from the cnxmlDoc. TODO(tal): break the XML arg into
     * more java manageable parameters (e.g. Map for resource mapping).
     *
     * @param context the query context
     * @param cnxmlDoc an XML doc in CNXML format.
     * @param resourceMapDoc an XML doc with resource map for this module version.
     * @return operation response.
     */
    RepositoryResponse<AddModuleVersionResult> addModuleVersion(RepositoryRequestContext context,
        String moduleId, String cnxmlDoc, String resourceMapDoc);

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
     * TODO(tal): define extra requirements from the colxmlDoc. TODO(tal): break the XML arg into
     * more java manageable parameters (e.g. ACL).
     *
     * @param context the query context
     * @param colxmlDoc an XML doc in COLXML format.
     * @return operation response.
     */
    RepositoryResponse<AddCollectionVersionResult> addCollectionVersion(
        RepositoryRequestContext context, String collectionId, String colxmlDoc);

    /**
     * Get the content of a collection version.
     * 
     * @param context the request context.
     * @param collectionId the target collection id
     * @param collectionVersion the target collection version or null for latest version.
     * @return operation response.
     */
    RepositoryResponse<GetCollectionVersionResult> getCollectionVersion(
        RepositoryRequestContext context, String collectionId, @Nullable Integer collectionVersion);

    /**
     * Get the information of a collection version.
     * 
     * @param context the request context.
     * @param collectionId the target collection id
     * @param collectionVersion the target collection version or null for latest version.
     * @return operation response.
     */
    RepositoryResponse<GetCollectionVersionInfoResult> getCollectionVersionInfo(
        RepositoryRequestContext context, String collectionId, @Nullable Integer collectionVersion);

    /**
     * Returns a map of export type ids to export type specification. The result of this method is
     * guaranteed to be stable throughout the life of this repository service instance and to be
     * consistent across repository process runs (that is, types can be added, removed or modifies
     * though ids and types are always stable).
     */
    Map<String, ExportType> getExportTypes();

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
