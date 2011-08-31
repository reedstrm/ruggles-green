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

import static com.google.common.base.Preconditions.checkNotNull;
import javax.annotation.Nullable;
import org.cnx.mdml.Metadata;
import org.cnx.resourcemapping.Resources;
import org.jdom.Document;
import org.jdom.Element;

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

    public Module(String id, Document cnxml, Resources resources, @Nullable Metadata metadata) {
        this.id = checkNotNull(id);
        this.cnxml= checkNotNull(cnxml);
        this.resources = checkNotNull(resources);
        this.metadata = metadata;
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

    public String getTitle() {
        return cnxml.getRootElement().getChildText(CnxmlTag.TITLE.getTag(), CnxmlTag.NAMESPACE);
    }
}
