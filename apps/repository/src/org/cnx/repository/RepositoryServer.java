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

package org.cnx.repository;

import org.cnx.repository.service.api.CnxRepositoryService;
import org.cnx.repository.service.impl.CnxRepositoryServiceImpl;

/**
 * Global definitions for the repository server.
 * 
 * @author Tal Dayan
 */
public class RepositoryServer {

    public static final String LOCAL_SERVER = "localhost";
    public static final int LOCAL_PORT = 8888;
    public static final String LOCAL_BASE_URL = "http://" + LOCAL_SERVER + ":" + LOCAL_PORT;
    
    private static final CnxRepositoryService SERVICE = new CnxRepositoryServiceImpl(LOCAL_BASE_URL);

    public static CnxRepositoryService getService() {
        return SERVICE;
    }
}
