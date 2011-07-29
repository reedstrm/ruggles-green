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

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.junit.Test;

import java.io.IOException;
import java.net.MalformedURLException;

/**
 * Test for {@link CnxCategoriesDocumentServlet}
 *
 * @author Arjun Satyapal
 */
public class CnxCategoriesDocumentServletTest extends CnxAtomPubBaseTest {
    private final static String PACKAGE = "org.cnx.repository.atompub.servlets.servicedocument";

    public CnxCategoriesDocumentServletTest() throws MalformedURLException {
        super(PACKAGE);
    }

    @Test
    public void testCnxCategoryDocument() throws HttpException, IOException {
        HttpClient httpClient = new HttpClient();
        GetMethod getMethod = new GetMethod(getConstants().getCategoryDocumentAbsPath());
        httpClient.executeMethod(getMethod);
        String response = getMethod.getResponseBodyAsString();

        // TODO(arjuns) : Add more tests for CategoryDocument.
    }
}