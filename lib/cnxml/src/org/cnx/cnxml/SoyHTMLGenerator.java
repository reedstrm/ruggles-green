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
import com.google.inject.BindingAnnotation;
import com.google.inject.Inject;
import com.google.template.soy.data.SoyMapData;
import com.google.template.soy.tofu.SoyTofu;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.Set;

import org.cnx.util.RenderTime;
import org.w3c.dom.Node;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 *  SoyHTMLGenerator converts a CNXML file to HTML using Closure Templates.
 */
@RenderTime public class SoyHTMLGenerator implements ModuleHTMLGenerator {
    public static final String SOY_NAMESPACE = "org.cnx.cnxml.SoyHTMLGenerator";

    @BindingAnnotation
    @Target({ FIELD, PARAMETER, METHOD })
    @Retention(RUNTIME)
    public @interface Template {}

    private final SoyTofu tofu;
    private final ImmutableSet<Processor> processors;
    private final DomToSoyDataConverter soyDataConverter;

    @Inject public SoyHTMLGenerator(@Template SoyTofu tofu, Set<Processor> processors,
            DomToSoyDataConverter soyDataConverter) {
        this.tofu = tofu;
        this.processors = ImmutableSet.copyOf(processors);
        this.soyDataConverter = soyDataConverter;
    }

    @Override public String generate(Node node) throws Exception {
        // Apply processors
        for (Processor processor : processors) {
            node = processor.process(node);
        }

        // Render to HTML
        final SoyMapData params = new SoyMapData(
                "node", soyDataConverter.convertDomToSoyData(node)
        );
        return tofu.render(SOY_NAMESPACE + ".main", params, null);
    }
}
