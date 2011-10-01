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
package org.cnx.atompubclient;

import org.apache.http.client.HttpClient;

import com.google.appengine.api.utils.SystemProperty;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.esxx.js.protocol.GAEConnectionManager;

/**
 * Helper class for CNX Client.
 * 
 * @author Arjun Satyapal
 */
public class CnxClientUtils {
    // Utility class.
    private CnxClientUtils() {
    }

    public static HttpClient getHttpClient() {
        if (SystemProperty.environment.value() == SystemProperty.Environment.Value.Production) {
            return new DefaultHttpClient(new GAEConnectionManager(), new BasicHttpParams());
        }
        return new DefaultHttpClient();
    }
}
