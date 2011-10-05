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
package org.cnx.common.repository.atompub;

/**
 * Enum to wrap all CnxAtomPub Collections.
 * 
 * TODO(arjuns) : Add term and category here.
 * @author Arjun Satyapal
 */
public enum CnxAtomPubCollectionEnum {
    APC_COLLECTION("AtomPub Collection for CNX Collections"),
    APC_MODULE("AtomPub Collection for CNX Modules."),
    APC_RESOURCES("AtomPub Collection for CNX Resources.");
    
    private String title;
    
    private CnxAtomPubCollectionEnum(String title) {
        this.title = title;
    }
    
    public String getTitle() {
        return title;
    }
}
