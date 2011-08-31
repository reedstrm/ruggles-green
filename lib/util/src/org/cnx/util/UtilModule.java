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

package org.cnx.util;

import com.google.inject.AbstractModule;

import javax.xml.parsers.SAXParser;
import javax.xml.transform.TransformerFactory;

/**
 *  UtilModule configures the org.cnx.util package for Guice.
 */
public class UtilModule extends AbstractModule {
    @Override protected void configure() {
        bind(TransformerFactory.class)
                .toProvider(TransformerFactoryProvider.class)
                .asEagerSingleton();
        bind(SAXParser.class).toProvider(SAXParserProvider.class);

        RenderScope scope = new RenderScope();
        bindScope(RenderTime.class, scope);
        bind(RenderScope.class).toInstance(scope);
    }
}
