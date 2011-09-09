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

package org.cnx.repository.service.impl;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletResponse;

import org.cnx.repository.service.api.AddCollectionVersionResult;
import org.cnx.repository.service.api.AddModuleVersionResult;
import org.cnx.repository.service.api.CnxRepositoryConfiguration;
import org.cnx.repository.service.api.CnxRepositoryService;
import org.cnx.repository.service.api.CreateCollectionResult;
import org.cnx.repository.service.api.CreateModuleResult;
import org.cnx.repository.service.api.CreateResourceResult;
import org.cnx.repository.service.api.DeleteExportResult;
import org.cnx.repository.service.api.ExportReference;
import org.cnx.repository.service.api.GetCollectionInfoResult;
import org.cnx.repository.service.api.GetCollectionListResult;
import org.cnx.repository.service.api.GetCollectionVersionInfoResult;
import org.cnx.repository.service.api.GetCollectionVersionResult;
import org.cnx.repository.service.api.GetExportUploadUrlResult;
import org.cnx.repository.service.api.GetModuleInfoResult;
import org.cnx.repository.service.api.GetModuleListResult;
import org.cnx.repository.service.api.GetModuleVersionInfoResult;
import org.cnx.repository.service.api.GetModuleVersionResult;
import org.cnx.repository.service.api.GetResourceInfoResult;
import org.cnx.repository.service.api.GetResourceListResult;
import org.cnx.repository.service.api.RepositoryRequestContext;
import org.cnx.repository.service.api.RepositoryResponse;
import org.cnx.repository.service.api.ServeExportResult;
import org.cnx.repository.service.api.ServeResourceResult;
import org.cnx.repository.service.impl.operations.CollectionOperations;
import org.cnx.repository.service.impl.operations.ExportOperations;
import org.cnx.repository.service.impl.operations.ModuleOperations;
import org.cnx.repository.service.impl.operations.ResourceOperations;
import org.cnx.repository.service.impl.operations.Services;

/**
 * Implementation of the repository service for Google App Engine.
 * 
 * @author Tal Dayan
 * 
 */
public class CnxRepositoryServiceImpl implements CnxRepositoryService {

    private final static CnxRepositoryServiceImpl instance = new CnxRepositoryServiceImpl();

    @Override
    public CnxRepositoryConfiguration getConfiguration() {
        return Services.config;
    }

    @Override
    public RepositoryResponse<CreateResourceResult>
    createResource(RepositoryRequestContext context) {
        return ResourceOperations.createResource(context);
    }

    @Override
    public RepositoryResponse<CreateResourceResult> migrationCreateResourceWithId(
            RepositoryRequestContext context, String forcedId) {
        return ResourceOperations.migrationCreateResourceWithId(context, forcedId);
    }

    @Override
    public RepositoryResponse<GetResourceInfoResult> getResourceInfo(
            RepositoryRequestContext context, String resourceId) {
        return ResourceOperations.getResourceInfo(context, resourceId);
    }

    @Override
    public RepositoryResponse<ServeResourceResult> serveResouce(RepositoryRequestContext context,
            String resourceId, HttpServletResponse httpResponse) {
        return ResourceOperations.serveResource(context, resourceId, httpResponse);
    }

    @Override
    public RepositoryResponse<GetResourceListResult> getResourceList(
            RepositoryRequestContext context, String startCursor, int maxResults) {
        return ResourceOperations.getResourceList(context, startCursor, maxResults);
    }

    @Override
    public RepositoryResponse<CreateModuleResult> createModule(RepositoryRequestContext context) {
        return ModuleOperations.createModule(context);
    }

    @Override
    public RepositoryResponse<CreateModuleResult> migrationCreateModuleWithId(
            RepositoryRequestContext context, String forcedId) {
        return ModuleOperations.migrationCreateModuleWithId(context, forcedId);
    }

    @Override
    public RepositoryResponse<GetModuleInfoResult> getModuleInfo(RepositoryRequestContext context,
            String moduleId) {
        return ModuleOperations.getModuleInfo(context, moduleId);
    }

    @Override
    public RepositoryResponse<GetModuleListResult> getModuleList(RepositoryRequestContext context,
            @Nullable String moduleListCursor, int maxResults) {
        return ModuleOperations.getModuleList(context, moduleListCursor, maxResults);
    }

    @Override
    public RepositoryResponse<AddModuleVersionResult> addModuleVersion(
            RepositoryRequestContext context, String moduleId,
            @Nullable Integer expectedVersionNumber, String cnxmlDoc, String resourceMapDoc) {
        return ModuleOperations.addModuleVersion(context, moduleId, expectedVersionNumber,
                cnxmlDoc, resourceMapDoc);
    }

    @Override
    public RepositoryResponse<GetModuleVersionResult> getModuleVersion(
            RepositoryRequestContext context, String moduleId, @Nullable Integer moduleVersion) {
        return ModuleOperations.getModuleVersion(context, moduleId, moduleVersion);
    }

    @Override
    public RepositoryResponse<GetModuleVersionInfoResult> getModuleVersionInfo(
            RepositoryRequestContext context, String moduleId, @Nullable Integer moduleVersion) {
        return ModuleOperations.getModuleVersionInfo(context, moduleId, moduleVersion);
    }

    @Override
    public RepositoryResponse<CreateCollectionResult> createCollection(
            RepositoryRequestContext context) {
        return CollectionOperations.createCollection(context);
    }

    @Override
    public RepositoryResponse<CreateCollectionResult> migrationCreateCollectionWithId(
            RepositoryRequestContext context, String forcedId) {
        return CollectionOperations.migrationCreateCollectionWithId(context, forcedId);
    }

    @Override
    public RepositoryResponse<GetCollectionInfoResult> getCollectionInfo(
            RepositoryRequestContext context, String collectionId) {
        return CollectionOperations.getCollectionInfo(context, collectionId);
    }

    @Override
    public RepositoryResponse<GetCollectionListResult>
    getCollectionList(RepositoryRequestContext context,
            @Nullable String collectionListCursor, int maxResults) {
        return CollectionOperations.getCollectionList(context, collectionListCursor, maxResults);
    }

    @Override
    public RepositoryResponse<AddCollectionVersionResult> addCollectionVersion(
            RepositoryRequestContext context, String collectionId,
            @Nullable Integer expectedVersionNumber, String colxmlDoc) {
        return CollectionOperations.addCollectionVersion(context, collectionId,
                expectedVersionNumber, colxmlDoc);
    }

    @Override
    public RepositoryResponse<GetCollectionVersionResult> getCollectionVersion(
            RepositoryRequestContext context, String collectionId,
            @Nullable Integer collectionVersion) {
        return CollectionOperations.getCollectionVersion(context, collectionId, collectionVersion);
    }

    @Override
    public RepositoryResponse<GetCollectionVersionInfoResult> getCollectionVersionInfo(
            RepositoryRequestContext context, String collectionId, Integer collectionVersion) {
        return CollectionOperations.getCollectionVersionInfo(context, collectionId,
                collectionVersion);
    }

    @Override
    public RepositoryResponse<GetExportUploadUrlResult> getExportUploadUrl(
            RepositoryRequestContext context, ExportReference exportReference) {
        return ExportOperations.getExportUploadUrl(context, exportReference);
    }

    @Override
    public RepositoryResponse<ServeExportResult> serveExport(RepositoryRequestContext context,
            ExportReference exportReference, HttpServletResponse httpResponse) {
        return ExportOperations.serveExport(context, exportReference, httpResponse);
    }

    @Override
    public RepositoryResponse<DeleteExportResult> deleteExport(RepositoryRequestContext context,
            ExportReference exportReference) {
        return ExportOperations.deleteExport(context, exportReference);
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
