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

package org.cnx.repository.service.api;

/**
 * Represent the type of object to which an export can be attach.
 * 
 * @author Tal Dayan
 */
public enum ExportScopeType {
    MODULE_VERSION,
    MODULE,
    COLLECTION_VERSION,
    COLLECTION;

    /**
     * Test if this scope type is a version of a parent entity.
     */
    public boolean isVersion() {
        return this == MODULE_VERSION || this == COLLECTION_VERSION;
    }
}
