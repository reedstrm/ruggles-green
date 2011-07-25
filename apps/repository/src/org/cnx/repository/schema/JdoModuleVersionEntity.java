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

package org.cnx.repository.schema;

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
 * A JDO representing a module version entity.
 * 
 * @author Tal Dayan
 */
@PersistenceCapable(table = SchemaConsts.MODULE_VERSION_KEY_KIND)
public class JdoModuleVersionEntity {

    /**
     * This key is a child key of the module entity. Its child id equals the version number of this
     * version (first is 1).
     */
    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private Key key;

    /**
     * The ID of the module entity to which this version belong.
     * 
     * NOTE(tal): this information is encoded in the parent key. We duplicate
     * it as well for better debugging using the data store viewer.
     */
    @SuppressWarnings("unused")
    @Persistent
    private Long moduleId;

    /**
     * Version number. First version is 1, second is 2, etc.
     */
    @Persistent
    private Integer versionNumber;

    @Persistent
    private Text CNXMLDoc;

    @Persistent
    private Text resourceMapDoc;

    /**
     * @param moduleKey key of parent module
     * @param versionNumber version number of this version. Asserted to be >= 1.
     * @param CNXMLDoc the CNXML doc of this version.
     * @param manifestDoc the resource mapping XML doc of this version.
     */
    public JdoModuleVersionEntity(Key moduleKey, int versionNumber, String CNXMLDoc,
        String resourceMapDoc) {
        checkNotNull(moduleKey, "null module key");
        this.key = moduleVersionKey(moduleKey, versionNumber);
        this.moduleId = moduleKey.getId();
        this.versionNumber = versionNumber;
        this.CNXMLDoc = new Text(checkNotNull(CNXMLDoc));
        this.resourceMapDoc = new Text(checkNotNull(resourceMapDoc));
    }

    public Key getKey() {
        return key;
    }

    public int getVersionNumber() {
        return versionNumber;
    }

    public String getCNXMLDoc() {
        return CNXMLDoc.getValue();
    }

    public String getResourceMapDoc() {
        return resourceMapDoc.getValue();
    }
    
    /**
     * Construct a module version key.
     * 
     * @param moduleKey the key of the parent module entity.
     * @param versionNumber version number (asserted to be >= 1)
     * @return the module version key.
     */   
    public static Key moduleVersionKey(Key moduleKey, long versionNumber) {
      checkNotNull(moduleKey, "null module key");
      checkArgument(versionNumber > 0, "Invalid version number: %s", versionNumber);
      checkArgument(SchemaConsts.MODULE_KEY_KIND.equals(moduleKey.getKind()), "Not a moduleKey: %s", moduleKey);
      return KeyFactory.createKey(moduleKey, SchemaConsts.MODULE_VERSION_KEY_KIND, versionNumber);
    }
}
