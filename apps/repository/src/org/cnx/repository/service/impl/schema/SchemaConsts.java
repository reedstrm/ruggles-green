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

/**
 * Global schema related constants.
 * 
 * TODO(tal): delete this file after migrating off JDO
 * 
 * @author Tal Dayan
 */
public class SchemaConsts {
    /**
     * Module entities are persisted with keys of this kind.
     */
    public static final String MODULE_KEY_KIND = "Module";

    /**
     * Modules ids exposed via the repository API always have this prefix.
     */
    public static final String MODULE_ID_PREFIX = "M";

    /**
     * Module version entities are persisted with keys of this kind.
     */
    public static final String MODULE_VERSION_KEY_KIND = "ModuleVer";

    /**
     * Collection entities are persisted with keys of this kind.
     */
    public static final String COLLECTION_KEY_KIND = "Collection";

    /**
     * Collection ids exposed via the repository API always have this prefix.
     */
    public static final String COLLECTION_ID_PREFIX = "C";

    /**
     * Collection version entities are persisted with keys of this kind.
     */
    public static final String COLLECTION_VERSION_KEY_KIND = "CollectionVer";

    /**
     * Export item entities are persisted with keys of this kind.
     */
    public static final String EXPORT_ITEM_KEY_KIND = "Export";
}
