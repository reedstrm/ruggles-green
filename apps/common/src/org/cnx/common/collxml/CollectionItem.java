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
import java.util.Iterator;
import javax.annotation.Nullable;
import org.cnx.mdml.Metadata;

/**
 *  A CollectionItem is a node inside a collection's table of contents tree.
 */
public abstract class CollectionItem implements Iterable<CollectionItem> {
    private final Metadata metadata;
    private final int depth;
    private final int index;

    public CollectionItem(int depth, int index) {
        this(depth, index, null);
    }

    public CollectionItem(int depth, int index, @Nullable Metadata metadata) {
        this.depth = depth;
        this.index = index;
        this.metadata = metadata;
    }

    public int getDepth() {
        return depth;
    }

    public int getIndex() {
        return index;
    }

    public final Metadata getMetadata() {
        return metadata;
    }

    public abstract ImmutableList<CollectionItem> getChildren();

    @Override
    public final Iterator<CollectionItem> iterator() {
        return getChildren().iterator();
    }
}
