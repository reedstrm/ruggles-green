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

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.template.soy.SoyFileSet;
import com.google.template.soy.tofu.SoyTofu;

/**
 *  CollxmlModule configures the org.cnx.collxml package for Guice.
 */
public class CollxmlModule extends AbstractModule {
    @Override protected void configure() {
        bind(CollectionHTMLGenerator.class).to(SoyHTMLGenerator.class);
        bind(String.class).annotatedWith(CollxmlNamespace.class).toInstance("http://cnx.rice.edu/collxml");
        bind(String.class).annotatedWith(MetadataNamespace.class).toInstance("http://cnx.rice.edu/mdml");
    }

    @Provides @Singleton @SoyHTMLGenerator.Template
            SoyTofu provideTofu(SoyFileSet.Builder builder) {
        builder.add(SoyHTMLGenerator.class.getResource("collxml.soy"));
        return builder.build().compileToJavaObj();
    }
}
