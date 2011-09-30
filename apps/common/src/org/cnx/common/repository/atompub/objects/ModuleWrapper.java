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
import java.net.URISyntaxException;
import org.cnx.common.repository.atompub.CnxAtomPubLinkRelations;
import org.cnx.common.repository.atompub.CnxAtomPubUtils;
import org.cnx.common.repository.atompub.IdWrapper;
import org.cnx.common.repository.atompub.VersionWrapper;

/**
 * Wrapper object for CNX Resource.
 * @author Arjun Satyapal
 */
public class ModuleWrapper extends AtomPubResource {
    public ModuleWrapper(IdWrapper id, VersionWrapper version) {
        super(id, version);
    }
    
    @Override
    protected StringBuilder getStringBuilder() {
        return super.getStringBuilder();
    }
    
    @Override
    public String toString() {
        return this.getStringBuilder().toString();
    }

    /**
     * Function to create {@link ModuleWrapper} from Atom Entry returned by CNX Repository.
     * 
     * @param entry AtomEntry returned by Server.
     * 
     * @return ModuleWrapper representation for the Entry.
     * @throws URISyntaxException
     */
    public static ModuleWrapper fromEntry(Entry entry) throws URISyntaxException {
        IdWrapper id = CnxAtomPubUtils.getIdFromAtomPubId(entry.getId());
        VersionWrapper version = CnxAtomPubUtils.getVersionFromAtomPubId(entry.getId());
        ModuleWrapper module = new ModuleWrapper(id, version);
        
        if (version.getVersionInt() != 0) {
            module.setSelfUri(CnxAtomPubLinkRelations.getSelfUri(entry));
        }
        
        module.setEditUri(CnxAtomPubLinkRelations.getEditUri(entry));
        
        module.setPublished(entry.getPublished());
        
        return module;
    }
}
