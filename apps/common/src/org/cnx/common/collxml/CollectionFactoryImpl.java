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
import java.util.ArrayList;
import org.cnx.mdml.Metadata;
import org.cnx.mdml.MdmlMetadata;
import org.cnx.util.DOMUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class CollectionFactoryImpl implements CollectionFactory {
    private static final String METADATA_TAG_NAME = "metadata";
    private static final String CONTENT_TAG_NAME = "content";
    private static final String MODULE_TAG_NAME = "module";
    private static final String SUBCOLLECTION_TAG_NAME = "subcollection";
    private static final String DOCUMENT_ATTR_NAME = "document";
    private static final String VERSION_ATTR_NAME = "version";

    private final MdmlMetadata.Factory metadataFactory;
    private final String collxmlNamespace;

    @Inject public CollectionFactoryImpl(MdmlMetadata.Factory metadataFactory,
            @CollxmlNamespace String collxmlNamespace) {
        this.metadataFactory = metadataFactory;
        this.collxmlNamespace = collxmlNamespace;
    }

    @Override public Collection create(String id, Document collxml) {
        return new Collection(id, collxml, parseMetadata(collxml), parseTopNodes(collxml));
    }

    private Metadata parseMetadata(Document collxml) {
        Element elem = DOMUtils.findFirstChild(collxml.getDocumentElement(),
                collxmlNamespace, METADATA_TAG_NAME);
        if (elem == null) {
            return null;
        }
        return metadataFactory.create(elem);
    }

    private ArrayList<CollectionItem> parseTopNodes(Document collxml) {
        ArrayList<CollectionItem> nodes = new ArrayList<CollectionItem>();
        Element elem = DOMUtils.findFirstChild(collxml.getDocumentElement(),
                collxmlNamespace, CONTENT_TAG_NAME);
        if (elem == null) {
            return nodes;
        }
        return parseNodes(0, elem);
    }

    private ArrayList<CollectionItem> parseNodes(int depth, Element parent) {
        final ArrayList<CollectionItem> nodes = new ArrayList<CollectionItem>();
        int index = 0;
        for (Element elem : DOMUtils.iterElements(parent)) {
            final String localName = elem.getLocalName();
            if (MODULE_TAG_NAME.equals(localName)) {
                nodes.add(new ModuleLink(
                        depth,
                        index,
                        elem.getAttribute(DOCUMENT_ATTR_NAME),
                        elem.getAttribute(VERSION_ATTR_NAME),
                        metadataFactory.create(elem)));
                index++;
            } else if (SUBCOLLECTION_TAG_NAME.equals(localName)) {
                final Element content = DOMUtils.findFirstChild(elem, collxmlNamespace,
                        CONTENT_TAG_NAME);
                if (content != null) {
                    final ArrayList<CollectionItem> subnodes = parseNodes(depth + 1, content);
                    nodes.add(new Subcollection(depth, index, subnodes,
                            metadataFactory.create(elem)));
                }
            }
        }
        return nodes;
    }
}
