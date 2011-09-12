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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import com.google.inject.Inject;

import org.cnx.mdml.Metadata;
import org.cnx.mdml.MdmlMetadata;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.filter.ElementFilter;

public class CollectionFactoryImpl implements CollectionFactory {
    private static final String METADATA_TAG_NAME = "metadata";
    private static final String CONTENT_TAG_NAME = "content";
    private static final String MODULE_TAG_NAME = "module";
    private static final String SUBCOLLECTION_TAG_NAME = "subcollection";

    private final MdmlMetadata.Factory metadataFactory;

    @Inject public CollectionFactoryImpl(MdmlMetadata.Factory metadataFactory) {
        this.metadataFactory = metadataFactory;
    }

    @Override public Collection create(final String id, @Nullable final String version,
            final Document collxml) {
        return new Collection(id, version, collxml, parseMetadata(collxml), parseTopNodes(collxml));
    }

    private Metadata parseMetadata(final Document collxml) {
        final Element elem = collxml.getRootElement().getChild(
                CollxmlTag.METADATA.getTag(), CollxmlTag.NAMESPACE);
        return elem != null ? metadataFactory.create(elem) : null;
    }

    private List<CollectionItem> parseTopNodes(final Document collxml) {
        final Element elem = collxml.getRootElement().getChild(
                CollxmlTag.CONTENT.getTag(), CollxmlTag.NAMESPACE);
        if (elem == null) {
            return Collections.<CollectionItem>emptyList();
        }
        return parseNodes(0, elem);
    }

    @SuppressWarnings("unchecked")
    private ArrayList<CollectionItem> parseNodes(final int depth, final Element parent) {
        final ArrayList<CollectionItem> nodes = new ArrayList<CollectionItem>();
        final List<Element> children = (List<Element>)parent.getContent(
                new ElementFilter(CollxmlTag.NAMESPACE));
        int index = 0;
        for (Element elem : children) {
            switch (CollxmlTag.of(elem.getName())) {
            case MODULE:
                nodes.add(new ModuleLink(
                        depth, index,
                        elem.getAttributeValue(CollxmlAttributes.MODULE_DOCUMENT),
                        elem.getAttributeValue(CollxmlAttributes.MODULE_VERSION),
                        metadataFactory.create(elem)));
                break;
            case SUBCOLLECTION:
                final Element content = elem.getChild(
                        CollxmlTag.CONTENT.getTag(), CollxmlTag.NAMESPACE);
                if (content != null) {
                    final ArrayList<CollectionItem> subnodes = parseNodes(depth + 1, content);
                    nodes.add(new Subcollection(depth, index, subnodes,
                            metadataFactory.create(elem)));
                }
            }
            index++;
        }
        return nodes;
    }
}
