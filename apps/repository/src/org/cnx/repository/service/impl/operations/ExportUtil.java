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

package org.cnx.repository.service.impl.operations;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

import javax.jdo.PersistenceManager;

import org.cnx.repository.service.api.ExportInfo;
import org.cnx.repository.service.api.ExportType;
import org.cnx.repository.service.impl.configuration.ExportTypesConfiguration;
import org.cnx.repository.service.impl.schema.JdoExportItemEntity;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.repackaged.com.google.common.collect.Lists;

/**
 * Exports related utils.
 * 
 * @author Tal Dayan
 */
public class ExportUtil {

    /**
     * Query the list of child exports.
     * 
     * @param pm the PersistenceManager to use.
     * @param parentKey the key of the parent entity (module, module version, etc).
     * 
     * @return a list of export entities attached to the parent entity.
     */
    public static List<JdoExportItemEntity> queryChildExports(PersistenceManager pm, Key parentKey) {
        javax.jdo.Query query = pm.newQuery(JdoExportItemEntity.class);
        query.setFilter("parentKey == parentKeyParam");
        query.declareParameters(Key.class.getName() + " parentKeyParam");

        @SuppressWarnings("unchecked")
        final List<JdoExportItemEntity> exportEntities =
            (List<JdoExportItemEntity>) query.execute(parentKey);
        return exportEntities;
    }

    /**
     * Convert an export entity list to export info list.
     * 
     * @param exportEntityList a list of export entities attached to a parent entity.
     * @return a list of matching ExportInfo instances.
     */
    public static List<ExportInfo> exportInfoList(List<JdoExportItemEntity> exportEntityList) {
        final List<ExportInfo> exportInfos = Lists.newArrayList();
        for (JdoExportItemEntity exportEntity : exportEntityList) {
            final String exportTypeId = exportEntity.getExportId();
            final ExportType exportType =
                ExportTypesConfiguration.getExportTypes().get(exportTypeId);
            checkNotNull(exportType, "Unknown export type id: %s", exportTypeId);
            // TODO(tal): assert that export type is alloweable for this scope
            // TODO(tal): assert no duplicate export type ids.
            exportInfos.add(new ExportInfo(exportType));
        }
        return exportInfos;
    }

}
