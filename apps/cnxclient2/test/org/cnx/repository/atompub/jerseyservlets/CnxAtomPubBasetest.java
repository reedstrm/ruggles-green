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
package org.cnx.repository.atompub.jerseyservlets;

import java.io.IOException;
import java.net.URL;
import org.cnx.common.repository.atompub.CnxAtomPubConstants;

/**
 * BaseTest for all tests for CNX AtomPub API.
 *
 * @author Arjun Satyapal
 */
public abstract class CnxAtomPubBasetest {
    private CnxAtomPubConstants constants;
    private URL cnxServerAtomPubUrl;
    private int cnxServerPort;

    public CnxAtomPubConstants getConstants() {
        return constants;
    }

    public URL getCnxServerAtomPubUrl() {
        return cnxServerAtomPubUrl;
    }

    public int getCnxServerPort() {
        return cnxServerPort;
    }

    /*
     * Remember that Port is binded once the construction phase is over. So derived classes want to
     * access URI created for CNX Server, they should not rely on constructor.
     */
    public CnxAtomPubBasetest() {
        try {
            cnxServerAtomPubUrl = new URL("http://100.qa-cnx-repo.appspot.com/atompub");
            cnxServerAtomPubUrl =
                new URL("http://127.0.0.1:" + CnxAtomPubConstants.LOCAL_SERVER_PORT + "/atompub");
    
            // Initializing CnxAtomPub Service.
            constants = new CnxAtomPubConstants(cnxServerAtomPubUrl);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
