/*
 * Copyright The CNX Authors.
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
package org.cnx.web.servlets;

import java.io.IOException;
import java.util.Properties;

/**
 * Configuration Provider for Webview App.
 *
 * @author Arjun Satyapal
 */
public class WebviewConfiguration {
    private String CONFIG_PROPERTIES_FILE="config.properties";
    private Properties configProperties = new Properties();

    public WebviewConfiguration() {
        try {
            configProperties.load(this.getClass().getClassLoader().getResourceAsStream(
                CONFIG_PROPERTIES_FILE));
        } catch (IOException e) {
            // TODO(arjuns): Auto-generated catch block
            throw new RuntimeException(e);
        }
    }

    public String getRepositoryServerUrl() {
        return configProperties.get("cnx.repo.server.url").toString();
    }

    public String getRepositoryAtomPubUrl() {
        return getRepositoryServerUrl() + "/atompub";
    }

    public String getRichsCollectionUrl() {
        String collectionId = configProperties.getProperty("rich.collection.id");

        return "/content/collection/" + collectionId + "/latest/";
    }
}
