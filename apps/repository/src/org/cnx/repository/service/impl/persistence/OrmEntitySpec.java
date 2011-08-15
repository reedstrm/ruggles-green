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

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import javax.annotation.Nullable;

public class OrmEntitySpec {

    /**
     * Resource keys always have this kind.
     */
    private final String keyKind;

    /**
     * Resources ids are prefixed with this string.
     */
    @Nullable
    private final String idPrefix;

    public OrmEntitySpec(String keyKind, String idPrefix) {
        this.keyKind = checkNotNull(keyKind);
        this.idPrefix = idPrefix;
    }

    public String getKeyKind() {
        return keyKind;
    }

    public boolean supportsIds() {
        return (idPrefix != null);
    }

    public String getIdPrefix() {
        checkState(supportsIds(), "Entity kind %s does not support ids", keyKind);
        return idPrefix;
    }
}
