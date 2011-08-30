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

package org.cnx.repository.service.impl.configuration;

import org.cnx.repository.service.api.CnxRepositoryConfiguration;
import org.cnx.repository.service.api.ExportScopeType;
import org.cnx.repository.service.api.ExportType;

import com.google.appengine.repackaged.com.google.common.collect.Sets;
import com.google.common.collect.ImmutableMap;

/**
 * Implementation of CNX repository external configuration provider.
 * 
 * @author Tal Dayan
 */
public class CnxRepositoryConfigurationImpl implements CnxRepositoryConfiguration {
    private static final long KB = 1024;
    private static final long MB = 1024 * KB;

    /**
     * The singleton instance.
     */
    private static final CnxRepositoryConfiguration instance = new CnxRepositoryConfigurationImpl();

    /**
     * The export type map (id -> exportType).
     */
    private final ImmutableMap<String, ExportType> exportTypes;

    private CnxRepositoryConfigurationImpl() {
        exportTypes = constructExportMap();
    }

    /**
     * Internal method to construct the export types.
     * 
     * TODO(tal): load from configuration file.
     */
    private static ImmutableMap<String, ExportType> constructExportMap() {
        final ImmutableMap.Builder<String, ExportType> builder =
                new ImmutableMap.Builder<String, ExportType>();

        // Canonical PDF
        addType(builder, "pdf_std", "application/pdf", 50 * MB, ExportScopeType.MODULE,
                ExportScopeType.MODULE_VERSION, ExportScopeType.COLLECTION,
                ExportScopeType.COLLECTION_VERSION);

        // Canonical EPUB
        addType(builder, "epub_std", "application/xhtml+xml", 50 * MB, ExportScopeType.MODULE,
                ExportScopeType.MODULE_VERSION, ExportScopeType.COLLECTION,
                ExportScopeType.COLLECTION_VERSION);

        return builder.build();
    }

    /**
     * Internal utility method to add an export type to a map builder.
     */
    private static void addType(ImmutableMap.Builder<String, ExportType> builder, String id,
            String contentType, long maxSizeInBytes, ExportScopeType... allowedScopeTypes) {
        builder
        .put(id,
                new ExportType(id, contentType, maxSizeInBytes, Sets
                        .newHashSet(allowedScopeTypes)));
    }

    @Override
    public ImmutableMap<String, ExportType> getExportTypes() {
        return exportTypes;
    }

    @Override
    public long getMaxResourceSize() {
        return 100 * MB;
    }

    public static CnxRepositoryConfiguration getInstance() {
        return instance;
    }

    @Override
    public long getMaxCnxmlDocSize() {
        // NOTE(tal): if larger size is needed, can add compression in OrmModuleEntity.
        return 800 * KB;
    }

    @Override
    public long getMaxResourceMapDocSize() {
        // This is stored in the OrmModuleEntity in addition to the CNXML doc.
        return 100 * KB;
    }

    @Override
    public long getMaxColxmlDocSize() {
        return 500 * KB;
    }
}
