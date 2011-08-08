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
package org.cnx.repository.atompub.service;

import static org.cnx.repository.atompub.utils.CnxAtomCollectionUtils.getCollectionForCnxCollection;
import static org.cnx.repository.atompub.utils.CnxAtomCollectionUtils.getCollectionForCnxModule;
import static org.cnx.repository.atompub.utils.CnxAtomCollectionUtils.getCollectionForCnxResource;

import java.net.MalformedURLException;
import java.net.URL;

import org.cnx.repository.atompub.CnxAtomPubConstants;
import org.cnx.repository.atompub.utils.CustomMediaTypes;
import org.jdom.Document;
import org.jdom.Element;

import com.sun.syndication.propono.atom.common.AtomService;
import com.sun.syndication.propono.atom.common.Workspace;

/**
 * Cnx AtomPub Service.
 * 
 * @author Arjun Satyapal
 */
public class CnxAtomService extends AtomService {
    private CnxAtomPubConstants constants;
    private Workspace workspace;

    public CnxAtomPubConstants getConstants() {
        return constants;
    }


    public CnxAtomService(String hostUrl) {
        // TODO(arjuns) : Fix this.

        URL atomPubUrl = null;
        try {
            atomPubUrl = new URL(hostUrl + "/" + CnxAtomPubConstants.ATOMPUB_URL_PREFIX);
        } catch (MalformedURLException e) {
            // TODO(arjuns) : Handle exception properly.
            throw new RuntimeException(e);
        }

        constants = new CnxAtomPubConstants(atomPubUrl);

        /*
         * For Connexions repository, there is only one workspace. Each workspace will have three
         * AtomPubcollections : 1. Resources 2. Modules 3. Collections.
         */
        workspace = new Workspace(CnxAtomPubConstants.CNX_WORKSPACE_TITLE, CustomMediaTypes.TEXT);
        getWorkspaces().add(workspace);

        workspace
            .addCollection(getCollectionForCnxResource(constants.getCollectionResourceScheme()));
        workspace.addCollection(getCollectionForCnxModule(constants.getCollectionModuleScheme()));
        workspace.addCollection(getCollectionForCnxCollection(constants
            .getCollectionCnxCollectionScheme()));

    }

    /**
     * Serialize an AtomService object into an XML document
     */
    public Document getServiceDocument() {
        Document doc = new Document();
        Element root = new Element("service", ATOM_PROTOCOL);
        doc.setRootElement(root);
        root.addContent(workspace.workspaceToElement());
        return doc;
    }
}
