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

import java.util.Iterator;

import org.jdom.Element;
import org.jdom.Parent;
import org.jdom.filter.AbstractFilter;

/**
 *  Only returns elements that have the given ID.
 */
public class IdFilter extends AbstractFilter {
    public static final String ATTRIBUTE = "id";

    private final String id;

    public IdFilter(final String id) {
        this.id = checkNotNull(id);
    }

    @Override
    public boolean matches(Object obj) {
        if (obj instanceof Element) {
            final Element elem = (Element)obj;
            return id.equals(elem.getAttributeValue(ATTRIBUTE));
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
}
