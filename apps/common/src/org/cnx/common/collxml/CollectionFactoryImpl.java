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
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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
        return new Collection(id, collxml, parseMetadata(collxml), parseModuleLinks(collxml));
    }

    private Metadata parseMetadata(Document collxml) {
        Element elem = DOMUtils.findFirstChild(collxml.getDocumentElement(),
                collxmlNamespace, METADATA_TAG_NAME);
        if (elem == null) {
            return null;
        }
        return metadataFactory.create(elem);
    }

    private ArrayList<Collection.ModuleLink> parseModuleLinks(Document collxml) {
        ArrayList<Collection.ModuleLink> links = new ArrayList<Collection.ModuleLink>();
        Element elem = DOMUtils.findFirstChild(collxml.getDocumentElement(),
                collxmlNamespace, CONTENT_TAG_NAME);
        if (elem == null) {
            return links;
        }
        parseModuleLinks(links, elem);
        return links;
    }

    private void parseModuleLinks(ArrayList<Collection.ModuleLink> links, Element parent) {
        final NodeList childList = parent.getChildNodes();
        final int length = childList.getLength();

        for (int i = 0; i < length; i++) {
            final Node child = childList.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE &&
                    collxmlNamespace.equals(child.getNamespaceURI())) {
                final Element elem = (Element)child;
                final String localName = child.getLocalName();

                if (MODULE_TAG_NAME.equals(localName)) {
                    if (elem.hasAttribute(DOCUMENT_ATTR_NAME) &&
                            elem.hasAttribute(VERSION_ATTR_NAME)) {
                        links.add(new Collection.ModuleLink(
                                    elem.getAttribute(DOCUMENT_ATTR_NAME),
                                    elem.getAttribute(VERSION_ATTR_NAME),
                                    metadataFactory.create(elem)));
                    }
                } else if (SUBCOLLECTION_TAG_NAME.equals(localName)) {
                    final Element content = DOMUtils.findFirstChild(elem, collxmlNamespace,
                            CONTENT_TAG_NAME);
                    if (content != null) {
                        parseModuleLinks(links, content);
                    }
                }
            }
        }
    }
}
