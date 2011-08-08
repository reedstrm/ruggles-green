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

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.template.soy.SoyModule;
import org.cnx.common.collxml.CollxmlModule;
import org.cnx.cnxml.CnxmlModule;
import org.cnx.cnxml.DefaultProcessorModule;
import org.cnx.mdml.MdmlModule;
import org.cnx.util.UtilModule;

public class GuiceConfig extends GuiceServletContextListener {
    @Override protected Injector getInjector() {
        return Guice.createInjector(
                new CnxmlModule(),
                new CollxmlModule(),
                new DefaultProcessorModule(),
		new MdmlModule(),
                new ServletModule(),
                new SoyModule(),
                new UtilModule(),
                new WebViewModule()
        );
    }
}
