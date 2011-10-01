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
package org.cnx.atompubclient.cnxcollections;

import java.net.URISyntaxException;
import org.cnx.common.repository.atompub.CnxAtomPubCollectionEnum;
import org.cnx.servicedocument.Service;

/**
 * 
 * @author Arjun Satyapal
 */
public class APCForModules extends AbstractAPC {

    public APCForModules(Service serviceDocumentObject) throws URISyntaxException {
        super(serviceDocumentObject, CnxAtomPubCollectionEnum.APC_MODULE);
    }

  /*  // TODO(arjuns) : Add accept in collection.
    public ModuleWrapper createNewModule() throws IOException, IllegalArgumentException,
            JDOMException, FeedException, URISyntaxException {
        return createModule(getAPCUri());
    }

    public ModuleWrapper createNewModuleForMigration(IdWrapper id)
            throws IllegalArgumentException, IOException, JDOMException, FeedException,
            URISyntaxException {
        return createModule(getAPRUriForMigration(id));
    }

    private ModuleWrapper createModule(URI postUri) throws IOException,
            IllegalArgumentException, JDOMException, FeedException, URISyntaxException {
        @SuppressWarnings("deprecation")
        StringRequestEntity requestEntity = new StringRequestEntity("");
        String response = getHttpClient().post(postUri, requestEntity, null);
        Entry entry = CnxAtomPubUtils.parseXmlToEntry(response);
        ModuleWrapper module = ModuleWrapper.fromEntry(entry);

        return module;
    }

    // public ResourceInfoWrapper getModuleInformation(IdWrapper id) throws URISyntaxException,
    // IOException, IllegalArgumentException, JDOMException, FeedException {
    // String response = getHttpClient().get(getAPRUriForInformation(id));
    // Entry entry = CnxAtomPubUtils.parseStringToEntry(response);
    // return ResourceInfoWrapper.fromEntry(entry);
    // }

    public ModuleWrapper createNewModuleVersion(IdWrapper id, VersionWrapper newVersion,
            String cnxml, String resourceMappingXml) throws URISyntaxException, JDOMException,
            IOException, CnxException, JAXBException, IllegalArgumentException, FeedException {
        Entry moduleVersionEntry = new Entry();
        moduleVersionEntry.setContents(CnxAtomPubUtils.getAtomPubListOfContent(cnxml,
                resourceMappingXml));

        System.out.println("Sending entry = "
                + PrettyXmlOutputter.prettyXmlOutputEntry(moduleVersionEntry));
        StringRequestEntity requestEntity =
                new StringRequestEntity(PrettyXmlOutputter.prettyXmlOutputEntry(moduleVersionEntry));

        URI versionUri = new URI(id.getId() + "/" + newVersion.getVersionInt());
        URI editUri = CommonUtils.appendUri(getAPCUri(), versionUri);

        String response = getHttpClient().put(editUri, requestEntity, null  headers );

        System.out.println("Response for moduleversion = " + response);
        Entry entry = CnxAtomPubUtils.parseXmlToEntry(response);
        return ModuleWrapper.fromEntry(entry);
    }*/
}
