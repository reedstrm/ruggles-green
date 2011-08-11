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

package org.cnx.mdml;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;

/**
 *  MdmlModule configures the org.cnx.mdml package for Guice.
 */
public class MdmlModule extends AbstractModule {
    @Override protected void configure() {
        bind(String.class)
                .annotatedWith(MdmlNamespace.class)
                .toInstance("http://cnx.rice.edu/mdml");
        install(new FactoryModuleBuilder()
                .implement(MdmlMetadata.class, MdmlMetadata.class)
                .build(MdmlMetadata.Factory.class));
    }
}