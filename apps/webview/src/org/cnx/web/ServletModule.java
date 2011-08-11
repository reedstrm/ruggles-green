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

/**
 *  ServletModule configures the URLs for the web view servlets.
 */
public class ServletModule extends com.google.inject.servlet.ServletModule {
    @Override protected void configureServlets() {
        serveRegex("^/light/clear-cache$").with(CacheClearServlet.class);
        serveRegex("^/light/module/[^/]+/[^/]+/$").with(RenderModuleServlet.class);
        serveRegex("^/light/collection/[^/]+/[^/]+/module/[^/]+/[^/]+/$")
            .with(RenderCollectionModuleServlet.class);
        serveRegex("^/light/collection/[^/]+/[^/]+/$").with(RenderCollectionServlet.class);
    }
}
