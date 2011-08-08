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

import com.google.inject.Inject;
import org.cnx.mdml.Metadata;
import org.cnx.mdml.MdmlMetadata;
import org.cnx.util.DOMUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class CollectionFactoryImpl implements CollectionFactory {
    private static final String METADATA_TAG_NAME = "metadata";
    private final MdmlMetadata.Factory metadataFactory;
    private final String collxmlNamespace;

    @Inject public CollectionFactoryImpl(MdmlMetadata.Factory metadataFactory,
            @CollxmlNamespace String collxmlNamespace) {
        this.metadataFactory = metadataFactory;
        this.collxmlNamespace = collxmlNamespace;
    }

    @Override public Collection create(String id, Document collxml) {
        Metadata metadata = null;
        Element elem = DOMUtils.findFirstChild(collxml.getDocumentElement(),
                collxmlNamespace, METADATA_TAG_NAME);
        if (elem != null) {
            metadata = metadataFactory.create(elem);
        }
        return new Collection(id, collxml, metadata);
    }
}
