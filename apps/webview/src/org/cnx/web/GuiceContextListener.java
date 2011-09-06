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

import com.google.appengine.api.utils.SystemProperty;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;
import com.google.inject.servlet.GuiceServletContextListener;

/**
 *  GuiceContextListener adds a global injector to the servlet context.
 */
public class GuiceContextListener extends GuiceServletContextListener {
    @Override protected Injector getInjector() {
        Stage stage = Stage.DEVELOPMENT;
        if (SystemProperty.environment.value() == SystemProperty.Environment.Value.Production) {
            stage = Stage.PRODUCTION;
        }
        return Guice.createInjector(stage, new WebViewModule());
    }
}
