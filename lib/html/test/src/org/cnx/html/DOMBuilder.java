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

import com.google.common.base.Preconditions;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

class DOMBuilder {
    private static final String CONTENT_TAG = "content";
    private Node node;
    private String defaultNamespace;

    public DOMBuilder(Node node) {
        Preconditions.checkNotNull(node);
        this.node = node;
    }

    public DOMBuilder(Node node, String namespace) {
        Preconditions.checkNotNull(node);
        this.node = node;
        this.defaultNamespace = namespace;
    }

    public Document getDocument() {
        if (node instanceof Document) {
            return (Document)node;
        }
        return node.getOwnerDocument();
    }

    public DOMBuilder element(String tag) {
        return element(defaultNamespace, tag);
    }

    public DOMBuilder element(String ns, String tag) {
        return new DOMBuilder(getDocument().createElementNS(ns, tag));
    }

    public DOMBuilder attr(String key, String value) {
        Preconditions.checkState(node instanceof Element);
        ((Element)node).setAttribute(key, value);
        return this;
    }

    public DOMBuilder child(Node... args) {
        for (Node arg : args) {
            node.appendChild(arg);
        }
        return this;
    }

    public DOMBuilder child(DOMBuilder... args) {
        for (DOMBuilder arg : args) {
            node.appendChild(arg.build());
        }
        return this;
    }

    public DOMBuilder text(String s) {
        node.appendChild(getDocument().createTextNode(s));
        return this;
    }

    public DOMBuilder wrapContent(Node... args) {
        return child(element(CONTENT_TAG).child(args));
    }

    public DOMBuilder wrapContent(DOMBuilder... args) {
        return child(element(CONTENT_TAG).child(args));
    }

    public Node build() {
        return node;
    }
}
