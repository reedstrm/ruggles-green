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

package org.cnx.common.collxml;

import static com.google.common.base.Objects.equal;
import static com.google.common.base.Preconditions.*;
import com.google.common.collect.ImmutableList;
import java.util.List;
import javax.annotation.Nullable;
import org.cnx.mdml.Metadata;
import com.google.common.base.Objects;

/**
 *  A subcollection stores a list of collection items.
 */
public class Subcollection extends CollectionItem {
    private final ImmutableList<CollectionItem> children;

    public Subcollection(int depth, int index, List<CollectionItem> children,
            @Nullable Metadata metadata) {
        super(depth, index, metadata);
        this.children = ImmutableList.copyOf(checkNotNull(children));
    }

    @Override public int hashCode() {
        return Objects.hashCode(children, getMetadata());
    }

    @Override public boolean equals(Object o) {
        if (o instanceof Subcollection) {
            final Subcollection other = (Subcollection)o;
            return equal(children, other.children) && equal(getMetadata(), other.getMetadata());
        }
        return false;
    }

    @Override public ImmutableList<CollectionItem> getChildren() {
        return children;
    }
}
