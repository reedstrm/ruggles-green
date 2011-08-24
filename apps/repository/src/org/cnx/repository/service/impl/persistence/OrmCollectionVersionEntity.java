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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Date;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Text;

/**
 * A POJO representing a collection version entity.
 * 
 * @author Tal Dayan
 */
public class OrmCollectionVersionEntity extends OrmEntity {
    /**
     * Module version do not have an id. The use the module id and version number.
     */
    private static final OrmEntitySpec ENTITY_SPEC = new OrmEntitySpec("CollectionVer", null);

    private static final String VERSION_NUMBER = "version";
    private static final String COLXML_DOC = "colxml";

    /**
     * Version number. First version is 1, second is 2, etc.
     */
    private Integer versionNumber;

    private String colxmlDoc;

    /**
     * @param collectionKey key of parent collection
     * @param versionNumber version number of this version. Asserted to be >= 1.
     * @param colxmlDoc the COLXML doc of this version.
     */
    public OrmCollectionVersionEntity(Key collectionKey, Date creationTime, int versionNumber,
        String colxmlDoc) {
        super(ENTITY_SPEC, collectionVersionKey(collectionKey, versionNumber), creationTime);
        this.versionNumber = versionNumber;
        this.colxmlDoc = checkNotNull(colxmlDoc);
    }

    /**
     * Deserialize a collection version entity from a datastore entity.
     */
    public OrmCollectionVersionEntity(Entity entity) {
        super(ENTITY_SPEC, entity);
        this.versionNumber = ((Long) entity.getProperty(VERSION_NUMBER)).intValue();
        this.colxmlDoc = ((Text) entity.getProperty(COLXML_DOC)).getValue();
    }

    public int getVersionNumber() {
        return versionNumber;
    }

    public String getColxmlDoc() {
        return colxmlDoc;
    }

    /**
     * Construct a collection version key.
     * 
     * @param collectionKey the key of the parent collection entity.
     * @param versionNumber version number (asserted to be >= 1)
     * @return the collection version key.
     */
    public static Key collectionVersionKey(Key collectionKey, long versionNumber) {
        checkNotNull(collectionKey, "null collection key");
        checkArgument(OrmCollectionEntity.getSpec().getKeyKind().equals(collectionKey.getKind()),
                "Not a collectionKey: %s", collectionKey);
        checkArgument(versionNumber > 0, "Invalid version number: %s", versionNumber);
        return KeyFactory.createKey(collectionKey, ENTITY_SPEC.getKeyKind(), versionNumber);
    }

    @Override
    protected void serializeToEntity(Entity entity) {
        entity.setProperty(VERSION_NUMBER, versionNumber); // serialized as Long
        entity.setProperty(COLXML_DOC, new Text(colxmlDoc));
    }

    public static OrmEntitySpec getSpec() {
        return ENTITY_SPEC;
    }
}
