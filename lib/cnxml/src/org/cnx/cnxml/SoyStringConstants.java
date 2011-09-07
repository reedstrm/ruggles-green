/*
 * Copyright (C) 2011 The CNX Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.cnx.cnxml;

/**
 * Enum to wrap String Constants that are going to be used for Converting SoyDom to
 * HTML.
 *
 * @author Arjun Satyapal
 */
public enum SoyStringConstants {
    // Constant Strings for Soy.
    ATTRIBUTE_NODES("attributeNodes"),
    CDATA("cdata"),
    CHILD_NODES("childNodes"),
    ELEMENT("element"),
    LOCAL_NAME("localName"),
    NAMESPACE_URI("namespaceURI"),
    NODE ("node"),
    NODE_TYPE("nodeType"),
    NODE_VALUE("nodeValue"),
    TEXT("text"),

    // CNXML Related constants.
    AUDIO("audio"),
    DOWNLOAD("download"),
    FLASH("flash"),
    IMAGE("image"),
    JAVA_APPLET("java-applet"),
    LABVIEW("labview"),
    OBJECT("object"),
    PDF("pdf"),
    VIDEO("video"),
    WEBVIEW_2_0("webview2.0"),

    // Class identifiers for Soy Extras.
    FIND_ELEM("findElem"),
    INNER_TEXT("innerText"),
    MATH_CHILDREN_JAVA("mathChildrenJava"),
    MEDIA("media"),
    STR_TO_DECIMAL("strtod");

    private String soyName;

    private SoyStringConstants(String soyName) {
        this.soyName = soyName;
    }

    public String getSoyName() {
        return soyName;
    }
}
