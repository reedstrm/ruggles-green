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

import javax.servlet.http.HttpServletRequest;

import org.cnx.repository.service.api.ExportInfo;
import org.cnx.repository.service.api.ExportReference;
import org.cnx.repository.service.api.ExportScopeType;
import org.cnx.repository.service.api.ExportType;
import org.cnx.repository.service.impl.persistence.OrmBlobInfo;
import org.cnx.repository.service.impl.persistence.OrmExportItemEntity;
import org.cnx.repository.service.impl.persistence.PersistenceService;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.repackaged.com.google.common.collect.Lists;

/**
 * Exports related utils.
 * 
 * @author Tal Dayan
 */
@SuppressWarnings("synthetic-access")
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
     * Construct export reference from the parameters of an request.
     * 
     * @param req an incoming request that contains parameters encoded by
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
            Services.config.getExportTypes()
                .get(req.getParameter(ExportParams.EXPORT_TYPE_ID.name));

        final ExportReference exportReference =
            new ExportReference(scopeType, objectId, versionNumber, exportType.getId());
        return exportReference;
    }

    public static List<OrmExportItemEntity> fetchParentEportList(PersistenceService orm,
            Key parentKey) {
        return orm.readChildren(OrmExportItemEntity.class, parentKey);
    }

    public static List<ExportInfo> fetchParentEportInfoList(PersistenceService orm, Key parentKey) {
        List<OrmExportItemEntity> entities = fetchParentEportList(orm, parentKey);

        final List<ExportInfo> exportInfos = Lists.newArrayList();
        for (OrmExportItemEntity exportEntity : entities) {
            final String exportTypeId = exportEntity.getExportTypeId();
            final ExportType exportType = Services.config.getExportTypes().get(exportTypeId);
            checkNotNull(exportType, "Unknown export type id: %s", exportTypeId);
            // TODO(tal): what should we do if an export type is not allowable in this scope? This
            // can happen for example if we change the export config to be more restrictive.

            final OrmBlobInfo blobInfo = exportEntity.getBlobInfo();
            exportInfos.add(new ExportInfo(exportType, exportEntity.getCreationTime(), blobInfo
                .getSize(), blobInfo.getMd5Hash()));
        }
        return exportInfos;
    }

    /**
     * Construct request parameters representing a given export reference.
     * 
     * @return a string with the encoded parameters in the form name=value&name=value&... This
     *         encoding is compatible with {@link #exportReferenceFromRequestParameters}
     */
    public static String exportReferenceToRequestParameters(ExportReference exportReference) {
        final StringBuilder builder = new StringBuilder();

        // TODO(tal): *** We are using ExportType enum name from the abstract API as URL param
        // values.
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
