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

import com.sun.syndication.propono.atom.common.AtomService;
import com.sun.syndication.propono.atom.common.Workspace;
import java.net.MalformedURLException;
import java.net.URL;
import org.cnx.common.repository.atompub.CnxAtomPubConstants;
import org.cnx.common.repository.atompub.CnxAtomPubUtils;
import org.cnx.common.repository.atompub.CnxMediaTypes;
import org.jdom.Document;
import org.jdom.Element;

/**
 * CNX AtomPub Service.
 * 
 * @author Arjun Satyapal
 */
public class CnxAtomService extends AtomService {
    private CnxAtomPubConstants constants;
    private Workspace workspace;

    public CnxAtomPubConstants getConstants() {
        return constants;
    }

    @SuppressWarnings("unchecked")
    public CnxAtomService(String hostUrl) {
        // TODO(arjuns) : Fix this.

        URL atomPubUrl = null;
        try {
            atomPubUrl = new URL(hostUrl + "/" + CnxAtomPubUtils.ATOMPUB_URL_PREFIX);
        } catch (MalformedURLException e) {
            // TODO(arjuns) : Handle exception properly.
            throw new RuntimeException(e);
        }

        constants = new CnxAtomPubConstants(atomPubUrl);

        /*
         * For CNX repository, there is only one workspace. Each workspace will have three
         * AtomPubcollections : 1. Resources 2. Modules 3. Collections.
         */
        workspace = new Workspace(CnxAtomPubUtils.CNX_WORKSPACE_TITLE, CnxMediaTypes.TEXT_XML_UTF8);
        getWorkspaces().add(workspace);

        workspace.addCollection(getCollectionForCnxResource(constants.getAPCResourceScheme()));
        workspace.addCollection(getCollectionForCnxModule(constants.getAPCModuleScheme()));
        workspace.addCollection(getCollectionForCnxCollection(constants.getAPCCollectionScheme()));
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
