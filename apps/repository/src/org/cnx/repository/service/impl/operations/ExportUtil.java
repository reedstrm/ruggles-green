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
import javax.servlet.http.HttpServletRequest;

import org.cnx.repository.service.api.ExportInfo;
import org.cnx.repository.service.api.ExportReference;
import org.cnx.repository.service.api.ExportScopeType;
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
     * Request params used to encode export reference in the export upload completion URL.
     */
    private enum ExportParams {
        SCOPE("scope"),
        PARENT_ID("id"),
        PARENT_VERSION("ver"),
        EXPORT_TYPE_ID("type");

        /**
         * Param name as appears in the request URL. Should be web safe. Changing these names will
         * break only pending export uploadings.
         */
        private final String name;

        private ExportParams(String name) {
            this.name = name;
        }
    }

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

    /**
     * Construct export reference from the parameters of an request.
     * 
     * @param an incoming request that contains parameters encoded by
     *            {@link #exportReferenceToRequestParameters}
     */
    public static ExportReference exportReferenceFromRequestParameters(HttpServletRequest req) {
        final ExportScopeType scopeType =
            ParamUtil.paramToEnum(ExportScopeType.class, req.getParameter(ExportParams.SCOPE.name));

        final String objectId = req.getParameter(ExportParams.PARENT_ID.name);

        final String versionNumberParam = req.getParameter(ExportParams.PARENT_VERSION.name);
        final Integer versionNumber =
            versionNumberParam.equals("null") ? null : Integer.valueOf(versionNumberParam);

        final ExportType exportType =
            ExportTypesConfiguration.getExportTypes().get(
                    req.getParameter(ExportParams.EXPORT_TYPE_ID.name));

        final ExportReference exportReference =
            new ExportReference(scopeType, objectId, versionNumber, exportType.getId());
        return exportReference;
    }

    /**
     * Construct request parameters representing a given export reference.
     * 
     * @return a string with the encoded parameters in the form name=value&name=value&... This
     *         encoding is compatible with {@link #exportReferenceFromRequestParameters}
     */
    public static String exportReferenceToRequestParameters(ExportReference exportReference) {
        final StringBuilder builder = new StringBuilder();

        // TODO(tal): currently we use ExportType enum name from the API as URL param values.
        // Changing these name will break pending export uploads. Consider to add here a mapping
        // to stable names.
        builder.append(ExportParams.SCOPE.name);
        builder.append('=');
        builder.append(exportReference.getScopeType());

        builder.append("&");
        builder.append(ExportParams.PARENT_ID.name);
        builder.append('=');
        builder.append(exportReference.getObjectId());

        builder.append("&");
        builder.append(ExportParams.PARENT_VERSION.name);
        builder.append('=');
        builder.append(exportReference.getVersionNumber());

        builder.append("&");
        builder.append(ExportParams.EXPORT_TYPE_ID.name);
        builder.append('=');
        builder.append(exportReference.getExportTypeId());

        return builder.toString();
    }

}
