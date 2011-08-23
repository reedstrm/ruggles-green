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
import org.cnx.resourcemapping.Resources;
import org.cnx.util.DOMUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *  A Module is a POJO container for CNXML modules.
 *
 *  The XML files given to Module are assumed to be valid.
 */
public class Module {
    private final String id;
    private final Document cnxml;
    private final Resources resources;
    private final Metadata metadata;
    private final String cnxmlNamespace;

    public Module(String id, Document cnxml, Resources resources,
            @Nullable Metadata metadata, @CnxmlNamespace String cnxmlNamespace) {
        Preconditions.checkNotNull(id);
        Preconditions.checkNotNull(cnxml);
        // TODO(arjuns) : uncomment this later.
//        Preconditions.checkNotNull(resources);
        Preconditions.checkNotNull(cnxmlNamespace);

        this.id = id;
        this.cnxml= cnxml;
        this.resources = resources;
        this.metadata = metadata;
        this.cnxmlNamespace = cnxmlNamespace;
    }

    public String getId() {
        return id;
    }

    public Document getCnxml() {
        return cnxml;
    }

    public Resources getResources() {
        return resources;
    }

    public Metadata getMetadata() {
        return metadata;
    }

    public String getCnxmlNamespace() {
        return cnxmlNamespace;
    }

    public String getTitle() {
        final Element elem = DOMUtils.findFirstChild(cnxml.getDocumentElement(),
                cnxmlNamespace, "title");
        if (elem == null) {
            throw new IllegalArgumentException("CNXML document has no title");
        }
        return elem.getTextContent();
    }
}
