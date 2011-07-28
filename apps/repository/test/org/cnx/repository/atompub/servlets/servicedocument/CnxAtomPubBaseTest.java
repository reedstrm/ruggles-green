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
package org.cnx.repository.atompub.servlets.servicedocument;

import com.google.appengine.api.utils.SystemProperty;
import com.google.appengine.api.utils.SystemProperty.Environment;

import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.WebAppDescriptor;

import org.cnx.repository.atompub.utils.CnxAtomPubConstants;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * BaseTest for all tests for CNX AtomPub API.
 *
 * @author Arjun Satyapal
 */
public abstract class CnxAtomPubBaseTest extends JerseyTest {
    private WebResource webResource;
    private CnxAtomPubConstants constants;
    private URL cnxServerAtomPubUrl;
    private int cnxServerPort;

    public WebResource getWebResource() {
        return webResource;
    }

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
    public CnxAtomPubBaseTest(String resourcePackage) throws MalformedURLException {
        super(new WebAppDescriptor.Builder(resourcePackage).contextPath(
            CnxAtomPubConstants.ATOMPUB_URL_PREFIX).build());

        webResource = resource();// .path(CnxAtomPubConstants.SERVICE_DOCUMENT_PATH);
        cnxServerAtomPubUrl = webResource.getURI().toURL();

        // Initializing AppEngine environment.
        SystemProperty.environment.set(Environment.Value.Development);

        // Initializing CnxAtomPub Service.
        constants =
            new CnxAtomPubConstants(webResource.getURI().toString(), webResource.getURI().getPort());
    }
}