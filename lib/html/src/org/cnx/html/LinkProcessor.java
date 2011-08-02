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

package org.cnx.html;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import java.net.URI;
import java.net.URISyntaxException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *  LinkProcessor traverses the DOM tree given to it and bakes the final URI into the
 *  <code>src</code> or <code>url</code> attributes of any link or media elements.
 *  <p>
 *  The final URI is discovered using a {@link ResourceResolver}.
 *
 *  @see ResourceResolver
 */
public class LinkProcessor implements Processor {
    private static final ImmutableSet<String> LINK_ELEMENT_NAMES = ImmutableSet.<String>of(
            "foreign",
            "link",
            "term"
    );
    private static final ImmutableSet<String> MEDIA_ELEMENT_NAMES = ImmutableSet.<String>of(
            "audio",
            "download",
            "flash",
            "image",
            "java-applet",
            "labview",
            "object",
            "video"
    );
    private static String urlAttribute = "url";
    private static String resourceAttribute = "resource";
    private static String documentAttribute = "document";
    private static String versionAttribute = "version";
    private static String targetIdAttribute = "target-id";

    private final ResourceResolver resolver;
    private final String cnxmlNamespace;

    @Inject public LinkProcessor(ResourceResolver resolver,
            @CnxmlNamespace String cnxmlNamespace) {
        this.resolver = resolver;
        this.cnxmlNamespace = cnxmlNamespace;
    }

    public Node process(final Node node) throws Exception {
        final short nodeType = node.getNodeType();
        NodeList children;

        switch (node.getNodeType()) {
        case Node.DOCUMENT_NODE:
            children = node.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                process(children.item(i));
            }
            break;
        case Node.ELEMENT_NODE:
            // Process, if necessary.
            final Element elem = (Element)node;
            if (cnxmlNamespace.equals(elem.getNamespaceURI())) {
                final String localName = elem.getLocalName();
                URI uri;

                if (LINK_ELEMENT_NAMES.contains(localName)) {
                    resolveLink(elem);
                } else if (MEDIA_ELEMENT_NAMES.contains(localName)) {
                    resolveMedia(elem);
                }
            }

            // Recurse to children
            children = node.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                process(children.item(i));
            }
            break;
        }
        return node;
    }

    private void resolveLink(Element elem) throws Exception {
        URI target = null;

        if (elem.hasAttribute(urlAttribute)) {
            // URL
            target = resolver.resolveURI(new URI(elem.getAttribute(urlAttribute)));
        } else if (elem.hasAttribute(resourceAttribute)) {
            // Resource reference
            final String resource = elem.getAttribute(resourceAttribute);
            final String document = getAttributeOrNull(elem, documentAttribute);
            final String version = getAttributeOrNull(elem, versionAttribute);
            target = resolver.resolveResource(document, version, resource);
        } else if (elem.hasAttribute(documentAttribute) || elem.hasAttribute(versionAttribute)) {
            // Document reference
            final String document = getAttributeOrNull(elem, documentAttribute);
            final String version = getAttributeOrNull(elem, versionAttribute);
            target = resolver.resolveDocument(document, version);

            if (target != null && elem.hasAttribute(targetIdAttribute)) {
                target = new URI(target.getScheme(), target.getSchemeSpecificPart(),
                                 elem.getAttribute(targetIdAttribute));
            }
        } else if (elem.hasAttribute(targetIdAttribute)) {
            // ID reference
            target = resolver.resolveURI(new URI(null, null, elem.getAttribute(targetIdAttribute)));
        }

        if (target != null) {
            elem.setAttribute("url", target.toString());

            elem.removeAttribute("target-id");
            elem.removeAttribute("resource");
            elem.removeAttribute("document");
            elem.removeAttribute("version");
        }
    }

    private void resolveMedia(Element elem) throws Exception {
        URI src, target;

        src = new URI(elem.getAttribute("src"));
        target = resolver.resolveURI(src);
        if (target != null) {
            elem.setAttribute("src", target.toString());
        }
    }

    private static String getAttributeOrNull(Element elem, String name) {
        if (!elem.hasAttribute(name)) {
            return null;
        }
        return elem.getAttribute(name);
    }
}
