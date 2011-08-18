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

import javax.annotation.Nullable;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

/**
 * A POJO representing a module entity.
 * 
 * @author Tal Dayan
 */
public class OrmModuleEntity extends OrmEntity {

    private static final OrmEntitySpec ENTITY_SPEC = new OrmEntitySpec("Module", "m");

    private static final String VERSION_COUNT_PROPERTY = "versions";

    /**
     * Number of versions of this module. Value >= 0. If > 0, this is also the version of the last
     * version of this module (version numbering is 1, 2, ...). Zero when the module was created but
     * no version was added.
     */
    private int versionCount = 0;

    /**
     * Construct an entity with zero versions.
     * 
     * Useful when creating new modules.
     */
    public OrmModuleEntity(Date creationTime) {
        super(ENTITY_SPEC, null, creationTime);
    }

    /**
     * Deserialize a module entity from a datastore entity.
     */
    public OrmModuleEntity(Entity entity) {
        super(ENTITY_SPEC, entity);
        // NOTE(tal): numeric values are stored by datastore as Longs.
        this.versionCount = ((Long) entity.getProperty(VERSION_COUNT_PROPERTY)).intValue();
    }

    public int getVersionCount() {
        return versionCount;
    }

    public int incrementVersionCount() {
        versionCount++;
        return versionCount;
    }

    public static String moduleKeyToId(Key moduleKey) {
        checkNotNull(moduleKey);
        checkArgument(ENTITY_SPEC.getKeyKind().equals(moduleKey.getKind()), "Not a module key");
        return IdUtil.idToString(ENTITY_SPEC.getIdPrefix(), moduleKey.getId());
    }

    /**
     * Convert a module id returned by {@link #getId()} back to the module key. Returns null if
     * module id has invalid format.
     */
    @Nullable
    public static Key moduleIdToKey(String moduleId) {
        final Long moduleIdLong = IdUtil.stringToId(ENTITY_SPEC.getIdPrefix(), moduleId);
        return (moduleIdLong == null) ? null : KeyFactory.createKey(ENTITY_SPEC.getKeyKind(),
                moduleIdLong);
    }

    @Override
    protected void serializeToEntity(Entity entity) {
        entity.setProperty(VERSION_COUNT_PROPERTY, versionCount); // serialized as Long
    }

    public static OrmEntitySpec getSpec() {
        return ENTITY_SPEC;
    }
}
