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

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import com.google.template.soy.SoyFileSet;
import com.google.template.soy.tofu.SoyTofu;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;

/**
 *  DefaultModule is the default configuration for the HTML generator.
 */
public class DefaultModule extends AbstractModule {
    @Override protected void configure() {
        bind(HTMLGenerator.class).to(SoyHTMLGenerator.class);
        bind(DocumentBuilder.class).toProvider(DocumentBuilderProvider.class).asEagerSingleton();
        bind(TransformerFactory.class).toProvider(TransformerFactoryProvider.class).asEagerSingleton();
        bind(String.class)
                .annotatedWith(Names.named("CNXML_NAMESPACE"))
                .toInstance("http://cnx.rice.edu/cnxml");
        install(new SoyExtras());

        Multibinder<Processor> processorBinder =
                Multibinder.newSetBinder(binder(), Processor.class);

        RenderScope scope = new RenderScope();
        bindScope(RenderScoped.class, scope);
        bind(RenderScope.class).toInstance(scope);
    }

    @Provides @Singleton @SoyHTMLGenerator.Template
            SoyTofu provideTofu(SoyFileSet.Builder builder) {
        builder.add(SoyHTMLGenerator.class.getResource("html.soy"));
        return builder.build().compileToJavaObj();
    }

    @Provides @Singleton @Named("ContentMathMLProcessor.transformer")
            Transformer provideTransformer(TransformerFactory factory) {
        try {
            return factory.newTransformer(new StreamSource(
                        ContentMathMLProcessor.class.getResourceAsStream("ctop.xsl")));
        } catch (Exception e) {
            throw new RuntimeException("ContentMathMLProcessor is invalid", e);
        }
    }
}
