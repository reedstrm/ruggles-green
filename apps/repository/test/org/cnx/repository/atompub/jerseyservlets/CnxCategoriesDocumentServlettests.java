package org.cnx.repository.atompub.jerseyservlets;
///*
// * Copyright (C) 2011 The CNX Authors
// *
// * Licensed under the Apache License, Version 2.0 (the "License"); you may not
// * use this file except in compliance with the License. You may obtain a copy of
// * the License at
// *
// * http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
// * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
// * License for the specific language governing permissions and limitations under
// * the License.
// */
//package org.cnx.repository.atompub.jerseyservlets;
//
//import java.io.IOException;
//import org.apache.commons.httpclient.HttpClient;
//import org.apache.commons.httpclient.HttpException;
//import org.apache.commons.httpclient.methods.GetMethod;
//import org.junit.Test;
//
///**
// * Test for {@link CnxCategoriesDocumentServlet}
// *
// * @author Arjun Satyapal
// */
//@Deprecated
//public class CnxCategoriesDocumentServletTest extends CnxAtomPubBasetest {
//
//    public CnxCategoriesDocumentServletTest() throws IOException {
//        super();
//    }
//
//    @Test
//    public void testCnxCategoryDocument() throws HttpException, IOException {
//        HttpClient httpClient = new HttpClient();
//        GetMethod getMethod = new GetMethod(getConstants().getCategoryDocumentAbsPath());
//        httpClient.executeMethod(getMethod);
//        // TODO(arjuns) : fix this.
//        @SuppressWarnings("unused")
//        String response = getMethod.getResponseBodyAsString();
//
//        // TODO(arjuns) : Add more tests for CategoryDocument.
//    }
//}
