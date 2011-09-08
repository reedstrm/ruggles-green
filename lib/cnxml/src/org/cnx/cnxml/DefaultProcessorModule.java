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

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

/**
 *  DefaultProcessorModule is the default set of DOM processors.
 */
public class DefaultProcessorModule extends AbstractModule {
    @Override protected void configure() {
        Multibinder<Processor> processorBinder =
                Multibinder.newSetBinder(binder(), Processor.class);
        processorBinder.addBinding().to(ContentMathmlProcessor.class);
    }
}
