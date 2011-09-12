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

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;

import org.cnx.util.RenderTime;

import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;

/**
 *  CnxmlModule is the default configuration for the HTML generator.
 */
public class CnxmlModule extends AbstractModule {
    private static final String CTOP_CNX_NAME = "ctop-cnx.xsl";
    private static final String CTOP_W3C_NAME = "ctop-w3c.xsl";

    @Override
    protected void configure() {
        bind(ModuleFactory.class).to(ModuleFactoryImpl.class);
        bind(ModuleHtmlGenerator.class).to(JdomHtmlGenerator.class).in(RenderTime.class);

        Multibinder<Processor> processorBinder =
                Multibinder.newSetBinder(binder(), Processor.class);
    }

    @Provides
    @Singleton
    @ContentMathmlProcessor.ContentToPresentation
    Transformer provideContentToPresentationTransformer(TransformerFactory factory) {
        try {
            final Source cnxSource = getXsltSource(CTOP_CNX_NAME);
            final Source w3cSource = getXsltSource(CTOP_W3C_NAME);

            factory.setURIResolver(new URIResolver() {
                @Override public Source resolve(String href, String base) {
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
