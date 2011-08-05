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

package org.cnx.web;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import com.google.template.soy.SoyFileSet;
import com.google.template.soy.tofu.SoyTofu;
import java.io.File;
import org.cnx.html.LinkProcessor;
import org.cnx.html.Processor;
import org.cnx.html.RenderTime;
import org.cnx.html.ResourceResolver;

/**
 *  WebViewModule is the Guice configuration for the web view application.
 */
public class WebViewModule extends AbstractModule {
    @Override protected void configure() {
        bind(String.class)
                .annotatedWith(Names.named("javax.xml.transform.TransformerFactory"))
                .toInstance("org.apache.xalan.processor.TransformerFactoryImpl");
        bind(XmlFetcher.class).to(StaticXmlFetcher.class);
        bind(ResourceResolver.class).to(StaticResourceResolver.class);

        Multibinder<Processor> processorBinder =
                Multibinder.newSetBinder(binder(), Processor.class);
        processorBinder.addBinding().to(LinkProcessor.class);
    }

    @Provides @Singleton @WebViewTemplate
            SoyTofu provideTofu(SoyFileSet.Builder builder) {
        builder.add(new File("base.soy"));
        builder.add(new File("module.soy"));
        return builder.build().compileToJavaObj();
    }

    @Provides @RenderTime @Named("moduleId") String provideModuleId() {
        // Placeholder to make Guice happy. The real module ID is seeded in-scope.
        return null;
    }
}
