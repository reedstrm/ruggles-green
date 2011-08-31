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

import static com.google.common.base.Preconditions.*;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import javax.annotation.Nullable;
import org.cnx.mdml.Metadata;
import org.jdom.Document;

/**
 *  A Collection is a POJO container for CollXML collections.
 */
public class Collection {
    private final String id;
    private final Document collxml;
    private final Metadata metadata;
    private final ImmutableList<CollectionItem> topItems;
    private final ImmutableList<CollectionItem> items;
    private final ImmutableList<ModuleLink> moduleLinks;

    /** Create a collection. This is package-private; others should use CollectionFactory. */
    Collection(String id, Document collxml, @Nullable Metadata metadata,
            List<CollectionItem> topItems) {
        this.id = checkNotNull(id);
        this.collxml = checkNotNull(collxml);
        this.metadata = metadata;
        this.topItems = ImmutableList.copyOf(checkNotNull(topItems));

        final ArrayList<CollectionItem> items = new ArrayList<CollectionItem>();
        final ArrayList<ModuleLink> moduleLinks = new ArrayList<ModuleLink>();
        buildLists(items, moduleLinks, this.topItems);
        this.items = ImmutableList.copyOf(items);
        this.moduleLinks = ImmutableList.copyOf(moduleLinks);
    }

    private void buildLists(ArrayList<CollectionItem> items, ArrayList<ModuleLink> moduleLinks,
            ImmutableList<CollectionItem> list) {
        for (CollectionItem item : list) {
            items.add(item);
            if (item instanceof ModuleLink) {
                moduleLinks.add((ModuleLink)item);
            } else if (!item.getChildren().isEmpty()) {
                buildLists(items, moduleLinks, item.getChildren());
            }
        }
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

    /** This method returns all items in the collection with depth 0. */
    public ImmutableList<CollectionItem> getTopItems() {
        return topItems;
    }

    /** This method returns all items in the collection in pre-order. */
    public ImmutableList<CollectionItem> getItems() {
        return items;
    }

    public ImmutableList<ModuleLink> getModuleLinks() {
        return moduleLinks;
    }

    /**
     *  This method checks whether the collection has a module.
     */
    public boolean hasModule(String moduleId) {
        checkNotNull(moduleId);
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
     *  @return The index of module associated with the ID in the moduleLinks list, or -1 if not
     *          found.
     */
    public int getModuleIndex(String moduleId) {
        checkNotNull(moduleId);
        int index = 0;
        for (ModuleLink link : moduleLinks) {
            if (moduleId.equals(link.getModuleId())) {
                return index;
            }
            index++;
        }
        return -1;
    }

    /**
     *  This method finds the module link inside the collection.
     *
     *  @return The module link associated with the ID, or null if not found.
     */
    public ModuleLink getModuleLink(String moduleId) {
        final int index = getModuleIndex(checkNotNull(moduleId));
        return index >= 0 ? moduleLinks.get(index) : null;
    }

    /**
     *  This method finds the previous and next modules.
     *  <p>
     *  The module IDs returned are not necessarily siblings in the item hierarchy.
     *
     *  @return An array containing [previous, next].  One or both may be null.
     */
    public ModuleLink[] getPreviousNext(String moduleId) {
        checkNotNull(moduleId);
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
