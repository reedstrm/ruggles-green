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
import static org.cnx.common.repository.RepositoryConstants.MIN_NON_RESERVED_KEY_ID;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyRange;
import java.util.logging.Logger;

/**
 * Utilities related to the data migration from old CNX repository.
 * 
 * TODO(tal): delete after the successful migration.
 * 
 * @author Tal Dayan
 */
public class PersistenceMigrationUtil {

    private static final Logger log = Logger.getLogger(PersistenceMigrationUtil.class.getName());

    /**
     * Protect all key ranges reserved for migration.
     * 
     * The protection prevents automatic key allocation over the reserved keys.
     * 
     * Note that this operation is persisted in the database so it is sufficient to perform only
     * once though no harm if calling this multiple time (but not per query, for performance
     * reasons).
     */
    public static void protectAllReservedKeys(DatastoreService datastore) {

        protectEntityReservedKeys(datastore, OrmCollectionEntity.getSpec(), MIN_NON_RESERVED_KEY_ID);
        protectEntityReservedKeys(datastore, OrmModuleEntity.getSpec(), MIN_NON_RESERVED_KEY_ID);
        protectEntityReservedKeys(datastore, OrmResourceEntity.getSpec(), MIN_NON_RESERVED_KEY_ID);
    }

    private static void protectEntityReservedKeys(DatastoreService datastore,
            OrmEntitySpec entitySpec, long firstFreeId) {
        checkArgument(firstFreeId > 1, "Invalid firstFreeId: %s", firstFreeId);
        KeyRange keyRange = new KeyRange(null, entitySpec.getKeyKind(), 1, firstFreeId - 1);
        checkArgument(keyRange.getSize() == firstFreeId - 1, "Unexpected range size: %s",
                keyRange.getSize());
        // NOTE(tal): we don't care about the return state
        log.info("Protecting key range: " + entitySpec.getKeyKind() + " [1, " + firstFreeId + ")");
        datastore.allocateIdRange(keyRange);
    }

    /**
     * Test if given resource key is in the protected range.
     */
    public static boolean isResourceKeyProtected(Key resourceKey) {
        checkArgument(OrmResourceEntity.getSpec().getKeyKind().equals(resourceKey.getKind()),
                "Not a resource key: %s", resourceKey);
        return (resourceKey.getId() < MIN_NON_RESERVED_KEY_ID);
    }

    /**
     * Test if given module key is in the protected range.
     */
    public static boolean isModuleKeyProtected(Key moduleKey) {
        checkArgument(OrmModuleEntity.getSpec().getKeyKind().equals(moduleKey.getKind()),
                "Not a module key: %s", moduleKey);
        return (moduleKey.getId() < MIN_NON_RESERVED_KEY_ID);
    }

    /**
     * Test if given collection key is in the protected range.
     */
    public static boolean isCollectionKeyProtected(Key collectionKey) {
        checkArgument(OrmCollectionEntity.getSpec().getKeyKind().equals(collectionKey.getKind()),
                "Not a collection key: %s", collectionKey);
        return (collectionKey.getId() < MIN_NON_RESERVED_KEY_ID);
    }

}
