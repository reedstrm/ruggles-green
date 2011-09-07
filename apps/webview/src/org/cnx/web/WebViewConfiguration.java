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

import java.io.IOException;
import java.util.Map.Entry;
import java.util.Properties;

/**
 * Configuration Provider for Webview App.
 *
 * @author Arjun Satyapal
 */
public class WebViewConfiguration {
    private String CONFIG_PROPERTIES_FILE="config.properties";
    private Properties configProperties = new Properties();

    public WebViewConfiguration() {
        try {
            configProperties.load(this.getClass().getClassLoader().getResourceAsStream(
                CONFIG_PROPERTIES_FILE));
        } catch (IOException e) {
            // TODO(arjuns): Auto-generated catch block
            throw new RuntimeException(e);
        }
    }

    public String getConfiguration() {
        StringBuilder builder = new StringBuilder();

        for (Entry<Object, Object> currKeyValue : configProperties.entrySet()) {
            builder.append(getPropertyHtmlString(currKeyValue.getKey(), currKeyValue.getValue()));
        }

        return builder.toString();
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

    public static String getPropertyHtmlString(Object key, Object value) {
        return key + "=" + value + "<br/>";
    }

    public String getFeedbackLink() {
        return configProperties.getProperty("cnx.feedbackUrl");
    }
    
    public boolean isStackEnabled() {
        return Boolean.parseBoolean(configProperties.getProperty("stack.enable"));
    }
}
