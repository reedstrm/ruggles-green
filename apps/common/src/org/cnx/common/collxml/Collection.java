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

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import java.util.List;
import java.util.ListIterator;
import javax.annotation.Nullable;
import org.cnx.mdml.Metadata;
import org.w3c.dom.Document;

/**
 *  A Collection is a POJO container for CollXML collections.
 */
public class Collection {
    private final String id;
    private final Document collxml;
    private final Metadata metadata;
    private final List<ModuleLink> moduleLinks;

    /**
     *  A module link is a POJO that stores a link from a collection to a module it contains, along
     *  with associated metadata.
     */
    public static class ModuleLink {
        private final String moduleId;
        private final String moduleVersion;
        private final Metadata metadata;

        public ModuleLink(String moduleId, String moduleVersion, @Nullable Metadata metadata) {
            this.moduleId = moduleId;
            this.moduleVersion = moduleVersion;
            this.metadata = metadata;
        }

        public String getModuleId() {
            return moduleId;
        }

        public String getModuleVersion() {
            return moduleVersion;
        }

        public Metadata getMetadata() {
            return metadata;
        }

        @Override public int hashCode() {
            return Objects.hashCode(moduleId, moduleVersion, metadata);
        }

        @Override public boolean equals(Object o) {
            if (o instanceof ModuleLink) {
                final ModuleLink other = (ModuleLink)o;
                return Objects.equal(moduleId, other.moduleId) &&
                        Objects.equal(moduleVersion, other.moduleVersion) &&
                        Objects.equal(metadata, other.metadata);
            }
            return false;
        }
    }

    /** Create a collection. This is package-private; others should use CollectionFactory. */
    Collection(String id, Document collxml, @Nullable Metadata metadata,
            List<ModuleLink> moduleLinks) {
        Preconditions.checkNotNull(id);
        Preconditions.checkNotNull(collxml);
        Preconditions.checkNotNull(moduleLinks);

        this.id = id;
        this.collxml = collxml;
        this.metadata = metadata;
        this.moduleLinks = moduleLinks;
    }

    public String getId() {
        return id;
    }

    public Document getCollxml() {
        return collxml;
    }

    public Metadata getMetadata() {
        return metadata;
    }

    public List<ModuleLink> getModuleLinks() {
        return moduleLinks;
    }

    /**
     *  This method checks whether the collection has a module.
     */
    public boolean hasModule(String moduleId) {
        Preconditions.checkNotNull(moduleId);
        for (ModuleLink link : moduleLinks) {
            if (moduleId.equals(link.getModuleId())) {
                return true;
            }
        }
        return false;
    }

    /**
     *  This method finds the module link inside the collection.
     *
     *  @return The module link associated with the ID, or null if not found.
     */
    public ModuleLink getModuleLink(String moduleId) {
        Preconditions.checkNotNull(moduleId);
        for (ModuleLink link : moduleLinks) {
            if (moduleId.equals(link.getModuleId())) {
                return link;
            }
        }
        return null;
    }

    /**
     *  This method finds the previous and next modules.
     *  <p>
     *  The module IDs returned are not necessarily siblings in the item hierarchy.
     *
     *  @return An array containing [previous, next].  One or both may be null.
     */
    public ModuleLink[] getPreviousNext(String moduleId) {
        Preconditions.checkNotNull(moduleId);
        final ModuleLink[] result = new ModuleLink[2];
        ListIterator<ModuleLink> linkIter = moduleLinks.listIterator();
        boolean found = false;

        while (linkIter.hasNext()) {
            final ModuleLink link = linkIter.next();
            if (moduleId.equals(link.getModuleId())) {
                found = true;
                break;
            }
        }
        if (!found) {
            return result;
        }

        if (linkIter.hasNext()) {
            result[1] = linkIter.next();
            linkIter.previous();
        }
        linkIter.previous();
        if (linkIter.hasPrevious()) {
            result[0] = linkIter.previous();
        }
        return result;
    }
}
