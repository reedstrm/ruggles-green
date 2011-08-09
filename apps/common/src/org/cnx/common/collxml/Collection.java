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

import com.google.common.base.Preconditions;
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

    public Collection(String id, Document collxml, @Nullable Metadata metadata) {
        Preconditions.checkNotNull(id);
        Preconditions.checkNotNull(collxml);

        this.id = id;
        this.collxml= collxml;
        this.metadata = metadata;
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
}
