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

package org.cnx.repository.service.impl.persistence;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.util.Date;

import org.cnx.repository.service.api.ExportType;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.repackaged.com.google.common.base.StringUtil;

/**
 * A POJO representing an export item attached to a repository object.
 * 
 * @author Tal Dayan
 */
public class OrmExportItemEntity extends OrmEntity {

    /**
     * Export entities do not have an external id of their own. They are referenced by their parent
     * and export type.
     */
    private static final OrmEntitySpec ENTITY_SPEC = new OrmEntitySpec("Export", null);

    private static final String BLOB_KEY_PROPERTY = "blob_key";

    // @Persistent
    private BlobKey blobKey;

    public OrmExportItemEntity(Key key, Date creationTime, BlobKey blobKey) {
        super(ENTITY_SPEC, checkNotNull(key), creationTime);
        this.blobKey = checkNotNull(blobKey);
    }

    /**
     * Deserialize an export entity from a datastore entity.
     */
    public OrmExportItemEntity(Entity entity) {
        super(ENTITY_SPEC, entity);
        this.blobKey = (BlobKey) entity.getProperty(BLOB_KEY_PROPERTY);
    }

    public String getExportTypeId() {
        final String exportTypeId = getKey().getName();
        checkState(!StringUtil.isEmptyOrWhitespace(exportTypeId),
                "Export type id not found in key: %s", getKey());
        return exportTypeId;
    }

    public BlobKey getBlobKey() {
        return blobKey;
    }

    public void setBlobKey(BlobKey blobKey) {
        this.blobKey = checkNotNull(blobKey);
    }

    /**
     * Construct an export item key.
     * 
     * @param parentKey the key of the object to which this export is attached.
     * @param exportType the type of this export.
     * @return the export item key.
     */
    public static Key exportEntityKey(Key parentKey, ExportType exportType) {
        checkNotNull(parentKey, "null parent key");
        checkNotNull(exportType, "null export type");
        return KeyFactory.createKey(parentKey, ENTITY_SPEC.getKeyKind(), exportType.getId());
    }

    @Override
    protected void serializeToEntity(Entity entity) {
        entity.setProperty(BLOB_KEY_PROPERTY, blobKey);
    }

    public static OrmEntitySpec getSpec() {
        return ENTITY_SPEC;
    }
}
