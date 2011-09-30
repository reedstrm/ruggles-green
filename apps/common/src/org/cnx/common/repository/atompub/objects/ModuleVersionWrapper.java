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
package org.cnx.common.repository.atompub.objects;

import com.sun.syndication.feed.atom.Entry;
import java.io.IOException;
import java.net.URISyntaxException;
import org.cnx.common.exceptions.CnxException;
import org.cnx.common.repository.atompub.CnxAtomPubLinkRelations;
import org.cnx.common.repository.atompub.CnxAtomPubUtils;
import org.cnx.common.repository.atompub.IdWrapper;
import org.cnx.common.repository.atompub.VersionWrapper;
import org.jdom.JDOMException;

/**
 * Wrapper object for CNX Resource.
 * @author Arjun Satyapal
 */
public class ModuleVersionWrapper extends AtomPubResource {
    private final String cnxml;
    private final String resourceMappingXml;
    
    public ModuleVersionWrapper(IdWrapper id, VersionWrapper version, String cnxml,
            String resourceMappingXml) {
        super(id, version);
        this.cnxml = cnxml;
        this.resourceMappingXml = resourceMappingXml;
    }
    
    @Override
    protected StringBuilder getStringBuilder() {
        return super.getStringBuilder();
    }
    
    @Override
    public String toString() {
        return this.getStringBuilder().toString();
    }

    public static ModuleVersionWrapper fromEntry(Entry entry) throws URISyntaxException, JDOMException, IOException, CnxException {
        IdWrapper id = CnxAtomPubUtils.getIdFromAtomPubId(entry.getId());
        VersionWrapper version = CnxAtomPubUtils.getVersionFromAtomPubId(entry.getId());
        
        // TODO(arjuns) : Add validations here.
        String cnxml = CnxAtomPubUtils.getCnxmlFromModuleVersionEntry(entry);
        String resourceMappingXml = CnxAtomPubUtils.getResourceMappingDocFromModuleEntry(entry);
        
        ModuleVersionWrapper module = new ModuleVersionWrapper(id, version, cnxml, resourceMappingXml);
        
        module.setSelfUri(CnxAtomPubLinkRelations.getSelfUri(entry));
        module.setPublished(entry.getPublished());
        
        return module;
    }

    public String getCnxml() {
        return cnxml;
    }

    public String getResourceMappingXml() {
        return resourceMappingXml;
    }
}
