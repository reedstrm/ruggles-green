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

package org.cnx.cnxml;

import com.google.common.base.Preconditions;
import javax.annotation.Nullable;
import org.cnx.mdml.Metadata;
import org.w3c.dom.Document;

/**
 *  A Module is a POJO container for CNXML modules.
 */
public class Module {
    private final String id;
    private final Document cnxml;
    private final Document resourceMapping;
    private final Metadata metadata;

    public Module(String id, Document cnxml, Document resourceMapping,
            @Nullable Metadata metadata) {
        Preconditions.checkNotNull(id);
        Preconditions.checkNotNull(cnxml);
        Preconditions.checkNotNull(resourceMapping);

        this.id = id;
        this.cnxml= cnxml;
        this.resourceMapping = resourceMapping;
        this.metadata = metadata;
    }

    public String getId() {
        return id;
    }

    public Document getCnxml() {
        return cnxml;
    }

    public Document getResourceMapping() {
        return resourceMapping;
    }

    public Metadata getMetadata() {
        return metadata;
    }
}
