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

import com.google.inject.Inject;

import org.cnx.mdml.Metadata;
import org.cnx.mdml.MdmlMetadata;
import org.cnx.resourcemapping.Resources;

import org.jdom.Document;
import org.jdom.Element;

public class ModuleFactoryImpl implements ModuleFactory {
    private final MdmlMetadata.Factory metadataFactory;

    @Inject public ModuleFactoryImpl(MdmlMetadata.Factory metadataFactory) {
        this.metadataFactory = metadataFactory;
    }

    @Override
    public Module create(String id, Document cnxml, Resources resources) {
        Metadata metadata = null;
        final Element elem = cnxml.getRootElement()
                .getChild(CnxmlTag.METADATA.getTag(), CnxmlTag.NAMESPACE);
        if (elem != null) {
            metadata = metadataFactory.create(elem);
        }
        return new Module(id, cnxml, resources, metadata);
    }
}
