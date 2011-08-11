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

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.AbstractIterator;
import java.util.Iterator;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class DOMUtils {
    private static class ElementIterator extends AbstractIterator<Element> {
        private final NodeList list;
        private int index = -1;

        public ElementIterator(Element parent) {
            Preconditions.checkNotNull(parent);
            this.list = parent.getChildNodes();
        }

        @Override protected Element computeNext() {
            for (; index < list.getLength(); index++) {
                final Node child = list.item(index);
                if (child instanceof Element) {
                    // Skip this index on the next call to findNext.
                    index++;
                    return (Element)child;
                }
            }
            return endOfData();
        }
    }

    /** Create an iterable that yields all direct descendents of a given element. */
    public static Iterable<Element> iterElements(final Element parent) {
        return new Iterable<Element>() {
            @Override public Iterator<Element> iterator() {
                return new ElementIterator(parent);
            }
        };
    }

    /**
     *  Find the first direct descendant element with the given name.
     *
     *  @param parent The element to search in
     *  @param namespace The namespace URI to search for
     *  @param name The element local name to search for
     *  @return The first element found, or null if no such element exists.
     */
    public static Element findFirstChild(final Element parent, final String namespace,
            final String name) {
        for (Element child : iterElements(parent)) {
            final String childNamespace = child.getNamespaceURI();
            if (Objects.equal(namespace, childNamespace) && name.equals(child.getLocalName())) {
                return child;
            }
        }
        return null;
    }

    /**
     *  Retrieve the text inside the direct descendent element with the given name.
     *
     *  @param parent The element to search in
     *  @param namespace The namespace URI to search for
     *  @param name The element local name to search for
     *  @return The text inside the first element found, or null if no such element exists.
     */
    public static String getElementText(final Element parent, final String namespace,
            final String name) {
        final Element child = findFirstChild(parent, namespace, name);
        return (child != null ? child.getTextContent() : null);
    }
}
