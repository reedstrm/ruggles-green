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

import java.util.Collection;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableSet;

import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.filter.AbstractFilter;

/**
 *  Match elements whose name is in a set.
 */
public class ElementSetFilter extends AbstractFilter {
    private final ImmutableSet<String> names;
    private final Namespace namespace;

    public ElementSetFilter(final Collection<String> names) {
        this.names = ImmutableSet.copyOf(checkNotNull(names));
        this.namespace = null;
    }

    public ElementSetFilter(final Collection<String> names, @Nullable final Namespace namespace) {
        this.names = ImmutableSet.copyOf(checkNotNull(names));
        this.namespace = namespace;
    }

    @Override
    public boolean matches(final Object obj) {
        if (obj instanceof Element) {
            final Element elem = (Element)obj;
            return names.contains(elem.getName())
                    && (namespace == null || namespace.equals(elem.getNamespace()));
        }
        return false;
    }
}
