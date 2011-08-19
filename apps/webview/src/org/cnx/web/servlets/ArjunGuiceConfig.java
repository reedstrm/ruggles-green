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

package org.cnx.web.servlets;

import org.cnx.cnxml.CnxmlModule;
import org.cnx.cnxml.DefaultProcessorModule;
import org.cnx.common.collxml.CollxmlModule;
import org.cnx.mdml.MdmlModule;
import org.cnx.util.UtilModule;

import com.google.appengine.api.utils.SystemProperty;
import com.google.inject.AbstractModule;
import com.google.inject.Stage;
import com.google.template.soy.SoyModule;

/**
 * Guice Configuration for WebViewer.
 *
 *  TODO(arjuns) : Rename this file.
 *
 * @author Arjun Satyapal
 *
 */
public class ArjunGuiceConfig extends AbstractModule {
    @Override
    protected void configure() {
        Stage stage = Stage.DEVELOPMENT;
        if (SystemProperty.environment.value() == SystemProperty.Environment.Value.Production) {
            stage = Stage.PRODUCTION;
        }
        install(new CnxmlModule());
        install(new CollxmlModule());
        install(new DefaultProcessorModule());
        install(new MdmlModule());
        install(new SoyModule());
        install(new UtilModule());
        install(new ArjunWebViewModule());
    }
}
