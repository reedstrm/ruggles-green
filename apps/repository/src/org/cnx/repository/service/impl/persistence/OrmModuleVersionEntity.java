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
 * A POJO class representing a module version entity.
 *
 * @author Tal Dayan
 */
// @PersistenceCapable(table = SchemaConsts.MODULE_VERSION_KEY_KIND)
public class OrmModuleVersionEntity extends OrmEntity {

    /**
     * Module version do not have an id. The use the module id and version number.
     */
    private static final OrmEntitySpec ENTITY_SPEC = new OrmEntitySpec("ModuleVer", null);

    private static final String VERSION_NUMBER = "version";
    private static final String CNXML_DOC = "cnxml";
    private static final String RESOURCE_MAP_DOC = "resource_map";

    /**
     * Version number. First version is 1, second is 2, etc.
     */
    private final int versionNumber;

    private final String cnxmlDoc;

    private final String resourceMapDoc;

    /**
     * @param moduleKey key of parent module
     * @param versionNumber version number of this version. Asserted to be >= 1.
     * @param cnxmlDoc the CNXML doc of this version.
     * @param resourceMapDoc the resource mapping XML doc of this version.
     */
    public OrmModuleVersionEntity(Key moduleKey, Date creationDate, int versionNumber,
        String cnxmlDoc, String resourceMapDoc) {
        super(ENTITY_SPEC, moduleVersionKey(moduleKey, versionNumber), creationDate);
        this.versionNumber = versionNumber;
        this.cnxmlDoc = checkNotNull(cnxmlDoc);
        this.resourceMapDoc = checkNotNull(resourceMapDoc);
    }

    /**
     * Deserialize a module version entity from a datastore entity.
     */
    public OrmModuleVersionEntity(Entity entity) {
        super(ENTITY_SPEC, entity);
        this.versionNumber = ((Long) entity.getProperty(VERSION_NUMBER)).intValue();
        this.cnxmlDoc = ((Text) entity.getProperty(CNXML_DOC)).getValue();
        this.resourceMapDoc = ((Text) entity.getProperty(RESOURCE_MAP_DOC)).getValue();
    }

    public int getVersionNumber() {
        return versionNumber;
    }

    public String getCNXMLDoc() {
        return cnxmlDoc;
    }

    public String getResourceMapDoc() {
        return resourceMapDoc;
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
        checkArgument(OrmModuleEntity.getSpec().getKeyKind().equals(moduleKey.getKind()),
                "Not a moduleKey: %s", moduleKey);
        checkArgument(versionNumber > 0, "Invalid version number: %s", versionNumber);
        return KeyFactory.createKey(moduleKey, ENTITY_SPEC.getKeyKind(), versionNumber);
    }

    @Override
    protected void serializeToEntity(Entity entity) {
        entity.setProperty(VERSION_NUMBER, versionNumber); // serialized as Long
        entity.setProperty(CNXML_DOC, new Text(cnxmlDoc));
        entity.setProperty(RESOURCE_MAP_DOC, new Text(resourceMapDoc));
    }

    public static OrmEntitySpec getSpec() {
        return ENTITY_SPEC;
    }
}
