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
package org.cnx.repository.scripts.migrators;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.cnx.atompubclient2.CnxClient;
import org.cnx.atompubclient2.HttpClientWrapper;
import org.cnx.common.exceptions.CnxException;
import org.cnx.common.repository.atompub.IdWrapper;

/**
 * 
 * @author Arjun Satyapal
 */
public class MigratorUtils {
    public static void cleanUp(CnxClient cnxClient, IdWrapper id) throws URISyntaxException,
            ClientProtocolException,
            IOException,
            CnxException {
        HttpClientWrapper httpClient = new HttpClientWrapper();
        URI cleanUpUri =
                new URI(cnxClient.getConstants().getAtomPubRestUrl() + "/delete/" + id.getType()
                        + "/"
                        + id.getId());
        HttpGet httpGet = new HttpGet(cleanUpUri);
        httpClient.execute(httpGet);
    }
}
