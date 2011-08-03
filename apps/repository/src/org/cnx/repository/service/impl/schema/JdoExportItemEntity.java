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

package org.cnx.repository.service.impl.schema;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import org.cnx.repository.service.api.ExportType;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

/**
 * JDO representing an export item attached to a repository object.
 * 
 * @author Tal Dayan
 */
@PersistenceCapable(table = SchemaConsts.EXPORT_ITEM_KEY_KIND)
public class JdoExportItemEntity extends CnxJdoEntity {
    /**
     * This key is a child key of the entity to which this export is attached. The name portion of
     * this key is the exportId.
     */
    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private Key key;

    // TODO(tal): currently we use this key to do ancestor queries in JDO.
    // It contains a copy of key.getParent(). Is there a direct way to do
    // ancestor queries in JDO? If not, should be drop JDO?
    @Persistent
    private Key parentKey;

    // @Persistent
    // private final String exportId;

    @Persistent
    private BlobKey blobKey;

    // TODO(tal): temp, remove.
    // @Persistent
    // private String dummy = "xyz";
    // public String getDummy() { return dummy; }

    /**
     * .
     * 
     * @param key the key of this export item. Should be a value computed by
     *            {@link #moduleVersionKey}.
     * @param blobKey the key of blob with with export.
     */
    public JdoExportItemEntity(Key key, BlobKey blobKey) {
        this.key = checkNotNull(key);
        this.parentKey = checkNotNull(key.getParent());
        this.blobKey = checkNotNull(blobKey);
    }

    public Key getKey() {
        return key;
    }

    public String getExportId() {
        return key.getName();
    }

    public BlobKey getBlobKey() {
        return blobKey;
    }

    public Key getParentKey() {
        return parentKey;
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
        return KeyFactory.createKey(parentKey, SchemaConsts.EXPORT_ITEM_KEY_KIND, exportType
            .getId());
    }
}
