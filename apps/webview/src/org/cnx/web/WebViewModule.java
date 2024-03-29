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

package org.cnx.web;

import java.io.File;

import org.cnx.cnxml.CnxmlModule;
import org.cnx.cnxml.DefaultProcessorModule;
import org.cnx.cnxml.LinkProcessor;
import org.cnx.cnxml.LinkResolver;
import org.cnx.cnxml.Module;
import org.cnx.cnxml.Processor;
import org.cnx.common.collxml.Collection;
import org.cnx.common.collxml.CollxmlModule;
import org.cnx.mdml.MdmlModule;
import org.cnx.util.RenderTime;
import org.cnx.util.UtilModule;

import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;
import com.google.template.soy.SoyFileSet;
import com.google.template.soy.SoyModule;
import com.google.template.soy.tofu.SoyTofu;

/**
 *  WebViewModule is the Guice configuration for the web view application.
 */
public class WebViewModule extends AbstractModule {
    @Override protected void configure() {
        bind(String.class)
                .annotatedWith(Names.named("javax.xml.transform.TransformerFactory"))
                .toInstance("org.apache.xalan.processor.TransformerFactoryImpl");
        bind(LinkResolver.class).to(WebViewLinkResolver.class);

        Multibinder<Processor> processorBinder =
                Multibinder.newSetBinder(binder(), Processor.class);
        processorBinder.addBinding().to(LinkProcessor.class);

        install(new CnxmlModule());
        install(new CollxmlModule());
        install(new DefaultProcessorModule());
        install(new MdmlModule());
        install(new SoyModule());
        install(new UtilModule());
    }

    @Provides @Singleton @WebViewTemplate
            SoyTofu provideTofu(WebViewConfiguration config, SoyFileSet.Builder builder) {
        return builder
                .setCompileTimeGlobals(new ImmutableMap.Builder<String, Object>()
                        .put("analyticsJs", config.getAnalyticsCode())
                        .put("siteMessage", config.getSiteMessage())
                        .put("feedbackUrl", config.getFeedbackLink())
                        .build())
                .add(new File("base.soy"))
                .add(new File("collection.soy"))
                .add(new File("index.soy"))
                .add(new File("module.soy"))
                .add(new File("errors.soy"))
                .build().compileToJavaObj();
    }

    @Provides @RenderTime Module provideModule() {
        // Placeholder to make Guice happy. The real module is seeded in-scope.
        return null;
    }

    @Provides @RenderTime Collection provideCollection() {
        // Placeholder to make Guice happy. The real collection is seeded in-scope.
        return null;
    }
}
