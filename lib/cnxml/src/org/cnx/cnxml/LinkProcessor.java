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

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.logging.Logger;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;

/**
 *  LinkProcessor traverses the DOM tree given to it and bakes the final URI into the
 *  <code>src</code> or <code>url</code> attributes of any link or media elements.
 *  <p>
 *  The final URI is discovered using a {@link LinkResolver}.
 *
 *  @see LinkResolver
 */
public class LinkProcessor implements Processor {
    Logger logger = Logger.getLogger(LinkProcessor.class.getName());

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

    private final LinkResolver resolver;
    private final String cnxmlNamespace;

    @Inject public LinkProcessor(LinkResolver resolver, @CnxmlNamespace String cnxmlNamespace) {
        this.resolver = resolver;
        this.cnxmlNamespace = cnxmlNamespace;
    }

    @Override
    public Module process(final Module module) throws Exception {
        processElement(module.getCnxml().getDocumentElement());
        return module;
    }

    protected void processElement(final Element elem) throws Exception {
        NodeList children;
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
        children = elem.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i).getNodeType() == Node.ELEMENT_NODE) {
                processElement((Element)children.item(i));
            }
        }
    }

    private void resolveLink(final Element elem) throws Exception {
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

    private void resolveMedia(final Element elem) throws Exception {
        URI src, target;

        String attr = elem.getAttribute("src");
        try {
            src = new URI(attr);
        } catch (URISyntaxException e) {
            logger.severe("Forcing string escape for : " + attr);
            String escapedString = URLEncoder.encode(attr, "UTF-8");
            src = new URI(escapedString);
        }
        target = resolver.resolveURI(src);
        if (target != null) {
            elem.setAttribute("src", target.toString());
        }
    }

    private static String getAttributeOrNull(final Element elem, final String name) {
        if (!elem.hasAttribute(name)) {
            return null;
        }
        return elem.getAttribute(name);
    }
}
