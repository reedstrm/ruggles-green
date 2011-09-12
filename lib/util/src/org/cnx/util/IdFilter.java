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

package org.cnx.util;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.jdom.Element;
import org.jdom.Parent;
import org.jdom.filter.AbstractFilter;

/**
 *  Match elements based on their ID attribute.
 */
public class IdFilter extends AbstractFilter {
    public static final String ATTRIBUTE = "id";

    private final String id;

    /** Construct a filter that matches any element with an ID. */
    public IdFilter() {
        this.id = null;
    }

    /** Construct a filter that matches any element with the given ID. */
    public IdFilter(final String id) {
        this.id = checkNotNull(id);
    }

    @Override
    public boolean matches(Object obj) {
        if (obj instanceof Element) {
            final Element elem = (Element)obj;
            if (id == null) {
                return elem.getAttribute(ATTRIBUTE) != null;
            } else {
                return id.equals(elem.getAttributeValue(ATTRIBUTE));
            }
        }
        return false;
    }

    /**
     *  Returns the first element with the given ID, or null if not found.
     */
    @SuppressWarnings("unchecked")
    public static Element getElementById(final Parent parent, final String id) {
        final Iterator<Element> iter = (Iterator<Element>)checkNotNull(parent).getDescendants(
                new IdFilter(checkNotNull(id)));
        if (!iter.hasNext()) {
            return null;
        }
        return iter.next();
    }

    /**
     *  Returns a mapping from IDs to elements.
     *  <p>
     *  If there are multiple elements with the same ID, the first element is used.
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Element> getIdMap(final Parent parent) {
        final Iterator<Element> iter = (Iterator<Element>)checkNotNull(parent).getDescendants(
                new IdFilter());
        final Map<String, Element> m = new HashMap<String, Element>();
        while (iter.hasNext()) {
            final Element elem = iter.next();
            final String id = elem.getAttributeValue(ATTRIBUTE);
            if (!m.containsKey(id)) {
                m.put(id, elem);
            }
        }
        return m;
    }
}
