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

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class DOMUtils {
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
        final NodeList childList = parent.getChildNodes();
        final int length = childList.getLength();
        for (int i = 0; i < length; i++) {
            final Node child = childList.item(i);
            final String childNamespace = child.getNamespaceURI();
            if (child.getNodeType() == Node.ELEMENT_NODE &&
                    ((namespace == null && childNamespace == null) ||
                            (namespace != null && namespace.equals(childNamespace)))
                    && name.equals(child.getNodeName())) {
                return (Element)child;
            }
        }
        return null;
    }
}
