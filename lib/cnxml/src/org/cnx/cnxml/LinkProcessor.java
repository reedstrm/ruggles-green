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

import static org.cnx.cnxml.CnxmlAttributes.FRAGMENT;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.logging.Logger;

import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.filter.ElementFilter;

/**
 *  LinkProcessor traverses the DOM tree given to it and bakes the final URI into the
 *  <code>src</code> or <code>url</code> attributes of any link or media elements.
 *  <p>
 *  The final URI is discovered using a {@link LinkResolver}.
 *
 *  @see LinkResolver
 */
public class LinkProcessor implements Processor {
    private static final Logger logger = Logger.getLogger(LinkProcessor.class.getName());

    private static final ImmutableSet<CnxmlTag> LINK_TAGS = ImmutableSet.of(
            CnxmlTag.FOREIGN,
            CnxmlTag.LINK,
            CnxmlTag.TERM
    );

    private final LinkResolver resolver;

    @Inject public LinkProcessor(LinkResolver resolver) {
        this.resolver = resolver;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Module process(final Module module) throws Exception {
        final Iterator<Element> iter = (Iterator<Element>)module.getCnxml().getRootElement()
                .getChild(CnxmlTag.CONTENT.getTag(), CnxmlTag.NAMESPACE)
                .getDescendants(new ElementFilter(CnxmlTag.NAMESPACE));
        while (iter.hasNext()) {
            final Element elem = iter.next();
            final CnxmlTag tag = CnxmlTag.of(elem.getName());

            if (LINK_TAGS.contains(tag)) {
                resolveLink(elem);
            } else if (CnxmlTag.MEDIA_CHILDREN.contains(tag)) {
                resolveMedia(elem);
            }
        }
        return module;
    }

    @VisibleForTesting void resolveLink(final Element elem) throws Exception {
        final String url = elem.getAttributeValue(CnxmlAttributes.LINK_URL);
        final String document = elem.getAttributeValue(CnxmlAttributes.LINK_DOCUMENT);
        final String version = elem.getAttributeValue(CnxmlAttributes.LINK_VERSION);
        final String resource = elem.getAttributeValue(CnxmlAttributes.LINK_RESOURCE);
        final String targetId = elem.getAttributeValue(CnxmlAttributes.LINK_TARGET_ID);

        URI target = null;
        if (url != null) {
            // URL
            target = resolver.resolveUri(Links.convertLinkAttributeToUri(url));
        } else if (resource != null) {
            // Resource reference
            final URI resourceUri =
                    (resource != null ? Links.convertFileNameToUri(resource) : null);
            target = resolver.resolveResource(document, version, resourceUri);
        } else if (document != null || version != null) {
            // Document reference
            target = resolver.resolveDocument(document, version);
            if (target != null && targetId != null) {
                target = new URI(target.getScheme(), target.getSchemeSpecificPart(), targetId);
            }
        } else if (targetId != null) {
            // ID reference
            target = resolver.resolveUri(Links.convertLinkAttributeToUri(FRAGMENT + targetId));
        }

        if (target != null) {
            elem.setAttribute(CnxmlAttributes.LINK_URL, target.toString());

            elem.removeAttribute(CnxmlAttributes.LINK_TARGET_ID);
            elem.removeAttribute(CnxmlAttributes.LINK_RESOURCE);
            elem.removeAttribute(CnxmlAttributes.LINK_DOCUMENT);
            elem.removeAttribute(CnxmlAttributes.LINK_VERSION);
        }
    }

    @VisibleForTesting void resolveMedia(final Element elem) throws Exception {
        final String attr = elem.getAttributeValue(CnxmlAttributes.MEDIA_CHILD_SOURCE);
        final URI target = resolver.resolveUri(Links.convertLinkAttributeToUri(attr));
        if (target != null) {
            elem.setAttribute(CnxmlAttributes.MEDIA_CHILD_SOURCE, target.toString());
        }
    }
}
