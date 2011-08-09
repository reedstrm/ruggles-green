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

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.template.soy.data.SoyData;
import com.google.template.soy.data.SoyListData;
import com.google.template.soy.data.SoyMapData;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class DomToSoyDataConverterImpl implements DomToSoyDataConverter {
    private static final ImmutableSet<String> NUMBERED_ELEMENTS = ImmutableSet.of(
            "definition",
            "equation",
            "example",
            "exercise",
            "figure",
            "note",
            "rule"
    );
    private final String cnxmlNamespace;

    @Inject public DomToSoyDataConverterImpl(@CnxmlNamespace String cnxmlNamespace) {
        this.cnxmlNamespace = cnxmlNamespace;
    }

    public SoyData convertDomToSoyData(Node node) throws Exception {
        return convert(node, new Counter());
    }

    private SoyMapData convert(final Node node, final Counter counter) {
        final SoyMapData m = new SoyMapData();
        final short nodeType = node.getNodeType();
        final String localName = node.getLocalName();
        final String namespaceURI = node.getNamespaceURI();

        m.put("nodeType", getNodeTypeName(nodeType));
        m.put("nodeValue", nullToEmptyString(node.getNodeValue()));
        m.put("localName", nullToEmptyString(localName));
        m.put("namespaceURI", nullToEmptyString(namespaceURI));
        m.put("prefix", nullToEmptyString(node.getPrefix()));

        // Attach counter number
        if (nodeType == Node.ELEMENT_NODE && cnxmlNamespace.equals(namespaceURI)
                && NUMBERED_ELEMENTS.contains(localName)) {
            String type = ((Element)node).getAttribute("type");
            if (type == null) {
                type = "";
            }
            m.put("number", counter.getNextNumber(localName, type));
        }

        // Attributes
        final NamedNodeMap attr = node.getAttributes();
        if (attr != null) {
            m.put("attributes", xmlAttributesToSoyMap(attr));

            // Attribute nodes
            final SoyListData soyAttrList = new SoyListData();
            for (int i = 0; i < attr.getLength(); i++) {
                soyAttrList.add(convert(attr.item(i), counter));
            }
            m.put("attributeNodes", soyAttrList);
        }

        // Children
        final NodeList childNodes = node.getChildNodes();
        final SoyListData childList = new SoyListData();
        for (int i = 0; i < childNodes.getLength(); i++) {
            childList.add(convert(childNodes.item(i), counter));
        }
        m.put("childNodes", childList);
        return m;
    }

    /**
     *  getNodeTypeName converts a DOM nodeType attribute into a string suitable for
     *  use in Soy.
     *
     *  @param type A DOM node type
     *  @return A string representation of the node type
     *  @see org.w3c.dom.Node
     */
    private static String getNodeTypeName(final short type) {
        switch (type) {
        case Node.ATTRIBUTE_NODE:
            return "attribute";
        case Node.CDATA_SECTION_NODE:
            return "cdata";
        case Node.COMMENT_NODE:
            return "comment";
        case Node.DOCUMENT_FRAGMENT_NODE:
            return "document_fragment";
        case Node.DOCUMENT_NODE:
            return "document";
        case Node.DOCUMENT_TYPE_NODE:
            return "document_type";
        case Node.ELEMENT_NODE:
            return "element";
        case Node.ENTITY_NODE:
            return "entity";
        case Node.ENTITY_REFERENCE_NODE:
            return "entity_ref";
        case Node.NOTATION_NODE:
            return "notation";
        case Node.PROCESSING_INSTRUCTION_NODE:
            return "processing_instruction";
        case Node.TEXT_NODE:
            return "text";
        }
        return "";
    }

    /**
     *  xmlAttributesToSoyMap converts attribute nodes into a Soy map.
     *
     *  @param map An DOM named node map of attributes.
     *  @return The corresponding Soy map
     */
    private static SoyMapData xmlAttributesToSoyMap(final NamedNodeMap map) {
        final SoyMapData soyMap = new SoyMapData();
        for (int i = 0; i < map.getLength(); i++) {
            Node attrNode = map.item(i);
            soyMap.put(xmlAttributeNameToSoyIdentifier(attrNode.getNodeName()),
                    attrNode.getNodeValue());
        }
        return soyMap;
    }

    /**
     *  Clean an attribute name to a Soy identifier.  Soy does not allow any non-identifer
     *  characters in a map key, so an attribute name must be sanitized.  The following rules are
     *  applied:
     *
     *  <ol>
     *      <li>If the character can be used in an identifier, it is copied as-is.
     *      <li>If the character is a hyphen, it is converted to an underscore.
     *      <li>Any other character is discarded (this includes digits for the first character).
     *  </ol>
     *
     *  @param s The string to clean
     *  @return A valid Soy identifier
     */
    static String xmlAttributeNameToSoyIdentifier(final String s) {
        int size = 0;
        char[] name = new char[s.length()];
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (Character.isLetter(ch) || ch == '_' || (i > 0 && Character.isDigit(ch))) {
                name[size] = ch;
                size++;
            } else if (ch == '-') {
                name[size] = '_';
                size++;
            }
        }
        return new String(name, 0, size);
    }

    private static String nullToEmptyString(final String s) {
        // TODO(light): this method doesn't really belong here
        return s != null ? s : "";
    }
}
