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

import static com.google.common.base.Preconditions.checkState;

import javax.annotation.Nullable;
import javax.jdo.JDOHelper;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

/**
 * A JDO representing a module entity.
 * 
 * @author Tal Dayan
 */
@PersistenceCapable(table = SchemaConsts.MODULE_KEY_KIND)
public class JdoModuleEntity extends CnxJdoEntity {

    /**
     * The key of this module entity in the data store. Assigned automatically by the data store
     * first time the entity is persisted. The kind of this key is always
     * {@link SchemaConsts#MODULE_KEY_KIND} and they have a Long id assigned by the persistence
     * layer.
     */
    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private Key key;

    /**
     * Number of versions of this module. Value >= 0. If > 0, this is also the version of the last
     * version of this module (version numbering is 1, 2, ...). Zero when the module was created but
     * no version was added.
     */
    @Persistent
    private Integer versionCount = 0;

    /**
     * @return the key of this module entity. Guaranteed to be non null after the entity is
     *         persisted by the first time.
     */
    @Nullable
    public Key getKey() {
        return key;
    }

    /**
     * Get the id of this module.
     * 
     * Module ID is a web safe string that can be mapped back to the module key. Module ids are
     * permanent and can be safely used externally to refer to modules.
     * 
     * @return the module id or null if the resource does not have a key yet.
     */
    @Nullable
    public String getModuleId() {
        if (key == null) {
            return null;
        }
        checkState(SchemaConsts.MODULE_KEY_KIND.equals(key.getKind()),
                "Unexpected kind: %s at key %s", key.getKind(), key);
        return KeyUtil.idToString(SchemaConsts.MODULE_ID_PREFIX, key.getId());
    }

    public int getVersionCount() {
        return versionCount;
    }

    public int incrementVersionCount() {
        versionCount++;
        JDOHelper.makeDirty(this, "versionCount");
        return versionCount;
    }

    /**
     * Convert a module id returned by getModuleId() back to the resource key. Returns null if
     * module id has invalid format.
     */
    @Nullable
    public static Key moduleIdToKey(String moduleId) {
        final Long moduleIdLong = KeyUtil.stringToId(SchemaConsts.MODULE_ID_PREFIX, moduleId);
        return (moduleIdLong == null) ? null : KeyFactory.createKey(SchemaConsts.MODULE_KEY_KIND,
                moduleIdLong);
    }
}
