/*
 * Copyright 2011 Google Inc.
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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Text;

/**
 * A JDO representing a collection version entity.
 * 
 * @author Tal Dayan
 */
@PersistenceCapable(table = SchemaConsts.COLLECTION_VERSION_KEY_KIND)
public class JdoCollectionVersionEntity {

    /**
     * This key is a child key of the collection entity. Its child id equals the version number of
     * this version (first is 1).
     */
    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private Key key;

    /**
     * The ID of the collection entity to which this version belong.
     * 
     * NOTE(tal): this information is encoded in the parent key. We duplicate it as well for better
     * debugging using the data store viewer.
     */
    @SuppressWarnings("unused")
    @Persistent
    private Long collectionId;

    /**
     * Version number. First version is 1, second is 2, etc.
     */
    @Persistent
    private Integer versionNumber;

    @Persistent
    private Text colxmlDoc;

    /**
     * @param collectionKey key of parent collection
     * @param versionNumber version number of this version. Asserted to be >= 1.
     * @param colxmlDoc the COLXML doc of this version.
     */
    public JdoCollectionVersionEntity(Key collectionKey, int versionNumber, String colxmlDoc) {
        checkNotNull(collectionKey, "null collection key");
        this.key = collectionVersionKey(collectionKey, versionNumber);
        this.collectionId = collectionKey.getId();
        this.versionNumber = versionNumber;
        this.colxmlDoc = new Text(checkNotNull(colxmlDoc));
    }

    public Key getKey() {
        return key;
    }

    public int getVersionNumber() {
        return versionNumber;
    }

    public String getColxmlDoc() {
        return colxmlDoc.getValue();
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
        checkArgument(SchemaConsts.COLLECTION_KEY_KIND.equals(collectionKey.getKind()),
            "Not a collectionKey: %s", collectionKey);
        checkArgument(versionNumber > 0, "Invalid version number: %s", versionNumber);
        return KeyFactory.createKey(collectionKey, SchemaConsts.COLLECTION_VERSION_KEY_KIND,
            versionNumber);
    }
}
