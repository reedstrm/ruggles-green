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

import com.google.inject.BindingAnnotation;
import com.google.inject.Inject;
import com.google.template.soy.data.SoyListData;
import com.google.template.soy.data.SoyMapData;
import com.google.template.soy.tofu.SoyTofu;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.net.URI;
import org.cnx.cnxml.ResourceResolver;
import org.cnx.util.DOMUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 *  SoyHTMLGenerator renders HTML from a CollXML using Closure Templates.
 */
public class SoyHTMLGenerator implements CollectionHTMLGenerator {
    public static final String SOY_NAMESPACE = "org.cnx.common.collxml.SoyHTMLGenerator";

    private static final String contentTag = "content";
    private static final String moduleTag = "module";
    private static final String subcollectionTag = "subcollection";
    private static final String uriAttribute = "url";
    private static final String documentAttribute = "document";
    private static final String versionAttribute = "version";

    @BindingAnnotation
    @Target({ FIELD, PARAMETER, METHOD })
    @Retention(RUNTIME)
    public @interface Template {}

    private final SoyTofu tofu;
    private final ResourceResolver resourceResolver;
    private final String collxmlNamespace;
    private final String metadataNamespace;

    @Inject public SoyHTMLGenerator(@Template SoyTofu tofu, ResourceResolver resourceResolver,
            @CollxmlNamespace String collxmlNamespace,
            @MetadataNamespace String metadataNamespace) {
        this.tofu = tofu;
        this.resourceResolver = resourceResolver;
        this.collxmlNamespace = collxmlNamespace;
        this.metadataNamespace = metadataNamespace;
    }

    @Override public String generate(final Document doc) throws Exception {
        final SoyMapData params = new SoyMapData(
                "items", extractItemsFromCollection(doc)
        );
        return tofu.render(SOY_NAMESPACE + ".main", params, null);
    }

    /**
     *  This method returns all of the Table of Contents items from a collection as Soy data.
     *
     *  @param doc The CollXML DOM document
     */
    public SoyListData extractItemsFromCollection(final Document doc) throws Exception {
        final Element contentElement = DOMUtils.findFirstChild(doc.getDocumentElement(),
                collxmlNamespace, contentTag);
        return extractItems(contentElement);
    }

    private SoyListData extractItems(final Element parent) throws Exception {
        final SoyListData items = new SoyListData();
        final NodeList childList = parent.getChildNodes();
        final int length = childList.getLength();

        for (int i = 0; i < length; i++) {
            final Node child = childList.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE &&
                    collxmlNamespace.equals(child.getNamespaceURI())) {
                final Element elem = (Element)child;
                final String localName = child.getLocalName();

                if (moduleTag.equals(localName)) {
                    items.add(extractModuleItem(elem));
                } else if (subcollectionTag.equals(localName)) {
                    items.add(extractSubcollectionItem(elem));
                }
            }
        }
        return items;
    }

    private SoyMapData extractModuleItem(final Element elem) throws Exception {
        URI uri;
        if (elem.hasAttribute(uriAttribute)) {
            uri = resourceResolver.resolveURI(new URI(elem.getAttribute(uriAttribute)));
        } else if (elem.hasAttribute(documentAttribute) &&
                elem.hasAttribute(versionAttribute)) {
            uri = resourceResolver.resolveDocument(
                    elem.getAttribute(documentAttribute),
                    elem.getAttribute(versionAttribute));
        } else {
            throw new IllegalArgumentException("No valid reference found");
        }

        return new SoyMapData(
                "type", "module",
                "title", DOMUtils.findFirstChild(elem, metadataNamespace, "title").getTextContent(),
                "uri", uri.toString()
        );
    }

    private SoyMapData extractSubcollectionItem(final Element elem) throws Exception {
        return new SoyMapData(
                "type", "subcollection",
                "title", DOMUtils.findFirstChild(elem, metadataNamespace, "title").getTextContent(),
                "items", extractItems(DOMUtils.findFirstChild(elem, collxmlNamespace, "content"))
        );
    }
}
