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

package org.cnx.util;

import static com.google.common.base.Preconditions.*;
import com.google.common.collect.ImmutableSet;

import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import org.apache.commons.lang3.StringEscapeUtils;

import org.jdom.Attribute;
import org.jdom.Comment;
import org.jdom.Content;
import org.jdom.Element;
import org.jdom.Text;

/**
 *  JdomHtmlSerializer serializes a JDOM tree into HTML5.
 */
public class JdomHtmlSerializer {
    /**
     *  SerializerFrame holds one stack frame of the HTML serialization process.
     */
    @SuppressWarnings("unchecked")
    private static class SerializerFrame {
        public final Element element;
        public final Iterator<Content> iterator;

        public SerializerFrame(Element element) {
            this.element = checkNotNull(element);
            this.iterator = (Iterator<Content>)element.getContent().iterator();
        }
    }

    /** This method serializes JDOM content directly to a string. */
    public String serialize(Content content) {
        final StringBuilder sb = new StringBuilder();
        serialize(sb, content);
        return sb.toString();
    }

    /**
     *  This method builds a string from an HTML JDOM tree.
     *
     *  This serialization follows HTML5 rules: namespaces are not preserved and void elements do
     *  not have a closing tag.
     */
    public void serialize(final StringBuilder sb, final Content content) {
        final Stack<SerializerFrame> stack = new Stack<SerializerFrame>();
        serializeContent(sb, stack, content);

        while (!stack.empty()) {
            if (stack.peek().iterator.hasNext()) {
                serializeContent(sb, stack, stack.peek().iterator.next());
            } else {
                final Element elem = stack.pop().element;
                sb.append("</");
                sb.append(elem.getName());
                sb.append('>');
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void serializeContent(final StringBuilder sb, final Stack<SerializerFrame> stack,
            final Content content) {
        if (content instanceof Text) {
            sb.append(StringEscapeUtils.escapeHtml4(((Text)content).getText()));
        } else if (content instanceof Element) {
            final Element elem = (Element)content;

            sb.append('<');
            sb.append(elem.getName());

            for (Attribute attr : (List<Attribute>)elem.getAttributes()) {
                sb.append(' ');
                sb.append(attr.getName());
                sb.append("=\"");
                sb.append(StringEscapeUtils.escapeHtml4(attr.getValue()));
                sb.append('"');
            }
            sb.append('>');

            // If this element can have children, then push it.
            final HtmlTag tag = HtmlTag.of(elem.getName());
            if (tag == null || !tag.isVoidTag()) {
                stack.push(new SerializerFrame(elem));
            }
        } else if (content instanceof Comment) {
            sb.append("<!--\n");
            sb.append(StringEscapeUtils.escapeHtml4(content.getValue().replace("-", "")));
            sb.append("\n-->");
        }
    }
}
