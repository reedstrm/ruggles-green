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

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Map;

import org.cnx.repository.service.api.ExportScopeType;
import org.cnx.repository.service.api.ExportType;

import com.google.appengine.repackaged.com.google.common.collect.Sets;
import com.google.common.collect.ImmutableMap;

public class ExportTypesConfiguration {
    /**
     * The export type map (id -> exportType).
     */
    private static final ImmutableMap<String, ExportType> EXPORT_TYPES = constructExportMap();

    /**
     * Get the export type map.
     * 
     * @return a map of export ids to export types.
     */
    public static Map<String, ExportType> getExportTypes() {
        return checkNotNull(EXPORT_TYPES);
    }

    /**
     * Internal utility method to add an export type to a map builder.
     */
    private static void addType(ImmutableMap.Builder<String, ExportType> builder, String id,
            String contentType, ExportScopeType... allowedScopeTypes) {
        builder.put(id, new ExportType(id, contentType, Sets.newHashSet(allowedScopeTypes)));
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
        addType(builder, "pdf_std", "application/pdf", ExportScopeType.MODULE,
                ExportScopeType.MODULE_VERSION, ExportScopeType.COLLECTION,
                ExportScopeType.COLLECTION_VERSION);

        // Canonical EPUB
        addType(builder, "epub_std", "application/xhtml+xml", ExportScopeType.MODULE,
                ExportScopeType.MODULE_VERSION, ExportScopeType.COLLECTION,
                ExportScopeType.COLLECTION_VERSION);

        return builder.build();
    }
}
