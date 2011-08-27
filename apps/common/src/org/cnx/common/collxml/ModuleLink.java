/*
 *  Copyright 2011 Google Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.cnx.common.collxml;

import com.google.common.collect.ImmutableList;
import javax.annotation.Nullable;
import org.cnx.mdml.Metadata;
import com.google.common.base.Objects;
import static com.google.common.base.Objects.equal;
import static com.google.common.base.Preconditions.*;

/**
 *  A module link is a POJO that stores a link from a collection to a module it contains, along
 *  with associated metadata.
 */
public class ModuleLink extends CollectionItem {
    private final String moduleId;
    private final String moduleVersion;

    public ModuleLink(int depth, int index, String moduleId, String moduleVersion,
            @Nullable Metadata metadata) {
        super(depth, index, metadata);
        this.moduleId = checkNotNull(moduleId);
        this.moduleVersion = checkNotNull(moduleVersion);
    }

    public String getModuleId() {
        return moduleId;
    }

    public String getModuleVersion() {
        return moduleVersion;
    }

    @Override public int hashCode() {
        return Objects.hashCode(moduleId, moduleVersion, getMetadata());
    }

    @Override public boolean equals(Object o) {
        if (o instanceof ModuleLink) {
            final ModuleLink other = (ModuleLink)o;
            return equal(moduleId, other.moduleId) && equal(moduleVersion, other.moduleVersion) &&
                    equal(getMetadata(), other.getMetadata());
        }
        return false;
    }

    @Override public ImmutableList<CollectionItem> getChildren() {
        return ImmutableList.<CollectionItem>of();
    }
}
