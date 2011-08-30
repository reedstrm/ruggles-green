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
import com.google.common.collect.Lists;

import java.net.URI;
import java.util.Stack;

import org.cnx.cnxml.LinkResolver;
import org.cnx.util.HtmlTag;
import org.cnx.util.HtmlAttributes;
import org.cnx.util.JdomHtmlSerializer;

import org.jdom.Element;

/**
 *  HtmlGeneratorImpl renders CollXML as an ordered list using JDOM.
 */
public class HtmlGeneratorImpl implements CollectionHTMLGenerator {
    private static final String HTML_MODULE_CLASS = "module";
    private static final String HTML_COLLECTION_CLASS = "collection";
    private static final String HTML_SUBCOLLECTION_CLASS = "subcollection";
    private static final String HTML_TITLE_CLASS = "title";

    private final LinkResolver linkResolver;
    private final JdomHtmlSerializer jdomHtmlSerializer;

    @Inject public HtmlGeneratorImpl(LinkResolver linkResolver,
            JdomHtmlSerializer jdomHtmlSerializer) {
        this.linkResolver = linkResolver;
        this.jdomHtmlSerializer = jdomHtmlSerializer;
    }

    private static class GeneratorFrame {
        public final CollectionItem item;
        public final Element parent;

        public GeneratorFrame(final CollectionItem item, final Element parent) {
            this.item = item;
            this.parent = parent;
        }
    }

    /** {@inheritDoc} */
    @Override public String generate(final Collection coll) throws Exception {
        final Stack<GeneratorFrame> stack = new Stack<GeneratorFrame>();
        final Element root = new Element(HtmlTag.UNORDERED_LIST.getTag())
                .setAttribute(HtmlAttributes.CLASS, HTML_COLLECTION_CLASS);
        for (CollectionItem item : Lists.reverse(coll.getTopItems())) {
            stack.push(new GeneratorFrame(item, root));
        }

        while (!stack.empty()) {
            final GeneratorFrame frame = stack.pop();

            if (frame.item instanceof ModuleLink) {
                final ModuleLink link = (ModuleLink)frame.item;
                final URI uri = linkResolver.resolveDocument(
                        link.getModuleId(), link.getModuleVersion());
                frame.parent.addContent(new Element(HtmlTag.LIST_ITEM.getTag())
                        .setAttribute(HtmlAttributes.CLASS, HTML_MODULE_CLASS)
                        .addContent(new Element(HtmlTag.LINK.getTag())
                                .setAttribute(HtmlAttributes.LINK_URL, uri.toString())
                                .setText(link.getMetadata().getTitle())));
            } else if (frame.item instanceof Subcollection) {
                final Subcollection sub = (Subcollection)frame.item;
                final Element newParent = new Element(HtmlTag.UNORDERED_LIST.getTag());
                frame.parent.addContent(new Element(HtmlTag.LIST_ITEM.getTag())
                        .setAttribute(HtmlAttributes.CLASS, HTML_SUBCOLLECTION_CLASS)
                        .addContent(new Element(HtmlTag.SPAN.getTag())
                                .setAttribute(HtmlAttributes.CLASS, HTML_TITLE_CLASS)
                                .setText(sub.getMetadata().getTitle()))
                        .addContent(newParent));
                for (CollectionItem item : Lists.reverse(sub.getChildren())) {
                    stack.push(new GeneratorFrame(item, newParent));
                }
            }
        }

        return jdomHtmlSerializer.serialize(root);
    }
}
