/*
 * Copyright 2011 Google Inc.
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
package org.cnx.repository.atompub.servlets.migrators;

import java.io.File;
import java.io.IOException;

import org.apache.commons.httpclient.HttpException;
import org.cnx.atompubclient.CnxAtomPubClient;

import com.sun.syndication.propono.atom.client.ClientEntry;
import com.sun.syndication.propono.utils.ProponoException;

/**
 * Migrator for a resource.
 *
 * @author Arjun Satyapal
 */
public class ResourceMigrator {
    private final CnxAtomPubClient cnxClient;

    public ResourceMigrator(CnxAtomPubClient cnxClient) {
        this.cnxClient = cnxClient;
    }

    // TODO(arjuns) : Replace probably with InputStream.
    public ClientEntry migrateResource(String resourceLocation) throws HttpException,
            ProponoException, IOException {
        File file = new File(resourceLocation);
        return cnxClient.uploadFileToBlobStore(file.getName(), file);
    }
}
