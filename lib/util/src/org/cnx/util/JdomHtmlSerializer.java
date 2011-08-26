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
import org.jdom.Content;
import org.jdom.Element;
import org.jdom.Text;

/**
 *  JdomHtmlSerializer serializes a JDOM tree into HTML5.
 */
public class JdomHtmlSerializer {
    /**
     *  These tags do not need a closing tag in HTML.
     *  <p>
     *  This list comes from the HTML5 spec: http://dev.w3.org/html5/spec/syntax.html#void-elements
     */
    private final static ImmutableSet<String> HTML_VOID_ELEMENTS = ImmutableSet.of(
            "area",
            "base",
            "br",
            "col",
            "command",
            "embed",
            "hr",
            "img",
            "input",
            "keygen",
            "link",
            "meta",
            "param",
            "source",
            "track",
            "wbr");

    /**
     *  SerializerFrame holds one stack frame of the HTML serialization process.
     */
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
        if (content instanceof Text) {
            serializeText(sb, (Text)content);
            return;
        }
        if (!(content instanceof Element)) {
            // Skip it.
            return;
        }

        final Stack<SerializerFrame> stack = new Stack<SerializerFrame>();
        serializeStartTag(sb, (Element)content);
        if (!HTML_VOID_ELEMENTS.contains(((Element)content).getName())) {
            stack.push(new SerializerFrame((Element)content));
        }
        while (!stack.empty()) {
            if (stack.peek().iterator.hasNext()) {
                final Content child = stack.peek().iterator.next();
                if (child instanceof Text) {
                    serializeText(sb, (Text)child);
                } else if (child instanceof Element) {
                    serializeStartTag(sb, (Element)child);

                    // If this element can have children, then push it.
                    if (!HTML_VOID_ELEMENTS.contains(((Element)child).getName())) {
                        stack.push(new SerializerFrame((Element)child));
                    }
                }
            } else {
                final Element elem = stack.pop().element;
                sb.append("</");
                sb.append(elem.getName());
                sb.append('>');
            }
        }
    }

    private void serializeStartTag(final StringBuilder sb, final Element elem) {
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
    }

    private void serializeText(final StringBuilder sb, final Text text) {
        sb.append(StringEscapeUtils.escapeHtml4(text.getText()));
    }
}
