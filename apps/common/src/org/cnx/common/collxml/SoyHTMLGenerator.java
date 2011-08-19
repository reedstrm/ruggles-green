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

package org.cnx.common.collxml;

import com.google.inject.BindingAnnotation;
import com.google.inject.Inject;
import com.google.template.soy.data.SoyListData;
import com.google.template.soy.data.SoyMapData;
import com.google.template.soy.tofu.SoyTofu;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.net.URI;
import org.cnx.cnxml.LinkResolver;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 *  SoyHTMLGenerator renders HTML from a CollXML using Closure Templates.
 */
public class SoyHTMLGenerator implements CollectionHTMLGenerator {
    public static final String SOY_NAMESPACE = "org.cnx.common.collxml.SoyHTMLGenerator";

    @BindingAnnotation
    @Target({ FIELD, PARAMETER, METHOD })
    @Retention(RUNTIME)
    public @interface Template {}

    private final SoyTofu tofu;
    private final LinkResolver linkResolver;

    @Inject public SoyHTMLGenerator(@Template SoyTofu tofu, LinkResolver linkResolver) {
        this.tofu = tofu;
        this.linkResolver = linkResolver;
    }

    @Override public String generate(final Collection coll) throws Exception {
        final SoyListData items = new SoyListData();
        for (CollectionItem item : coll.getTopItems()) {
            items.add(convertItemToSoyData(item));
        }
        final SoyMapData params = new SoyMapData(
                "items", items
        );
        return tofu.render(SOY_NAMESPACE + ".main", params, null);
    }

    private SoyMapData convertItemToSoyData(CollectionItem item) throws Exception {
        if (item instanceof ModuleLink) {
            return convertModuleLinkToSoyData((ModuleLink)item);
        } else if (item instanceof Subcollection) {
            return convertSubcollectionToSoyData((Subcollection)item);
        }
        throw new IllegalArgumentException("item must be a module link or subcollection");
    }

    private SoyMapData convertModuleLinkToSoyData(final ModuleLink link) throws Exception {
        URI uri = linkResolver.resolveDocument(link.getModuleId(), link.getModuleVersion());
        return new SoyMapData(
                "type", "module",
                "title", link.getMetadata().getTitle(),
                "uri", uri.toString()
        );
    }

    private SoyMapData convertSubcollectionToSoyData(final Subcollection sub) throws Exception {
        SoyListData items = new SoyListData();
        for (CollectionItem item : sub) {
            items.add(convertItemToSoyData(item));
        }
        return new SoyMapData(
                "type", "subcollection",
                "title", sub.getMetadata().getTitle(),
                "items", items
        );
    }
}
