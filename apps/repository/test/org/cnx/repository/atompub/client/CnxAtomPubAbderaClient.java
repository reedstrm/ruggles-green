//package org.cnx.repository.atompub.client;
/////*
//// * Copyright 2011 Google Inc.
//// *
//// * Licensed under the Apache License, Version 2.0 (the "License"); you may not
//// * use this file except in compliance with the License. You may obtain a copy of
//// * the License at
//// *
//// * http://www.apache.org/licenses/LICENSE-2.0
//// *
//// * Unless required by applicable law or agreed to in writing, software
//// * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
//// * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
// * License for the specific language governing permissions and limitations under
// * the License.
// */
//package org.cnx.repository.atompub.client;
//
//import org.apache.abdera.Abdera;
//import org.apache.abdera.factory.Factory;
//import org.apache.abdera.model.Collection;
//import org.apache.abdera.model.Document;
//import org.apache.abdera.model.Service;
//import org.apache.abdera.model.Workspace;
//import org.apache.abdera.protocol.client.AbderaClient;
//import org.cnx.repository.atompub.utils.CnxAtomPubConstants;
//
//import java.net.URL;
//
///**
// *
// * @author Arjun Satyapal
// */
//public class CnxAtomPubClient {
//    private URL atomPubServerUrl;
//    private Abdera abdera;
//    private AbderaClient abderaClient;
//    private Factory factory;
//
//    private Document<Service> serviceDocument;
//    private Service service;
//    private Workspace workspace;
//
//    public CnxAtomPubClient(URL atomPubServerUrl) {
//        abdera = new Abdera();
//        abderaClient = new AbderaClient(abdera);
//        factory = abdera.getFactory();
//        this.atomPubServerUrl = atomPubServerUrl;
//
//        serviceDocument =
//            abderaClient.get(atomPubServerUrl + CnxAtomPubConstants.SERVICE_DOCUMENT_PATH)
//                .getDocument();
//        service = serviceDocument.getRoot();
//        workspace = service.getWorkspace(CnxAtomPubConstants.CNX_WORKSPACE_TITLE);
//    }
//
//    /**
//     * CNX has only one workspace with 3 collections.
//     *
//     * @return CNX Workspace.
//     */
//    public Workspace getWorkSpace() {
//        return workspace;
//    }
//
//    /**
//     * @return Collection for CNX Resources.
//     */
//    public Collection getCollectionResource() {
//        return workspace.getCollection(CnxAtomPubConstants.COLLECTION_RESOURCE_NAME);
//    }
//
//}
