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

import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import com.google.template.soy.SoyFileSet;
import com.google.template.soy.tofu.SoyTofu;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;
import org.cnx.util.RenderTime;

/**
 *  CnxmlModule is the default configuration for the HTML generator.
 */
public class CnxmlModule extends AbstractModule {
    private static final String CTOP_CNX_NAME = "ctop-cnx.xsl";
    private static final String CTOP_W3C_NAME = "ctop-w3c.xsl";

    @Override protected void configure() {
        bind(ModuleFactory.class).to(ModuleFactoryImpl.class);
        bind(ModuleHTMLGenerator.class).to(SoyHTMLGenerator.class).in(RenderTime.class);
        bind(String.class)
                .annotatedWith(CnxmlNamespace.class)
                .toInstance("http://cnx.rice.edu/cnxml");
        install(new SoyExtras());

        Multibinder<Processor> processorBinder =
                Multibinder.newSetBinder(binder(), Processor.class);
    }

    @Provides @Singleton @SoyHTMLGenerator.Template
            SoyTofu provideTofu(SoyFileSet.Builder builder, @CnxmlNamespace String cnxmlNamespace) {
        return builder
                .setCompileTimeGlobals(new ImmutableMap.Builder<String, Object>()
                        .put("cnxmlNamespace", cnxmlNamespace)
                        .build())
                .add(SoyHTMLGenerator.class.getResource("html.soy"))
                .build().compileToJavaObj();
    }

    @Provides @Singleton @Named("ContentMathMLProcessor.transformer")
            Transformer provideContentMathMLTransformer(TransformerFactory factory) {
        try {
            final Source cnxSource = getXsltSource(CTOP_CNX_NAME);
            final Source w3cSource = getXsltSource(CTOP_W3C_NAME);

            // TODO(light): This is not thread-safe.
            factory.setURIResolver(new URIResolver() {
                @Override public Source resolve(String href, String base)
                        throws TransformerException {
                    if (cnxSource.getSystemId().equals(href)) {
                        return cnxSource;
                    } else if (w3cSource.getSystemId().equals(href)) {
                        return w3cSource;
                    }
                    return null;
                }
            });

            return factory.newTransformer(cnxSource);
        } catch (Exception e) {
            throw new RuntimeException("ContentMathMLProcessor XSLT is invalid", e);
        }
    }

    private Source getXsltSource(String name) {
        final StreamSource source = new StreamSource(getClass().getResourceAsStream(name));
        source.setSystemId(name);
        return source;
    }
}
