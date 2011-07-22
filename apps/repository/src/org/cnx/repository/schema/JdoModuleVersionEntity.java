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

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Text;
import com.google.common.base.Preconditions;

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
     */
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
    private Text manifestDoc;

    public JdoModuleVersionEntity(Key key, long moduleId, int versionNumber, String CNXMLDoc,
        String manifestDoc) {
        this.key = Preconditions.checkNotNull(key);
        this.moduleId = moduleId;
        this.versionNumber = versionNumber;
        this.CNXMLDoc = new Text(Preconditions.checkNotNull(CNXMLDoc));
        this.manifestDoc = new Text(Preconditions.checkNotNull(manifestDoc));
    }

    public int getVersionNumber() {
        return versionNumber;
    }

    public String getCNXMLDoc() {
        return CNXMLDoc.getValue();
    }

    public String getManifestDoc() {
        return manifestDoc.getValue();
    }
    
    public long getModuleId() {
        return moduleId;
    }
}
