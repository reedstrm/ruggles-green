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
package org.cnx.repository.atompub;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Test for {@link IdWrapper}
 * 
 * @author Arjun Satyapal
 */
public class IdWrapperTest {

    @Test
    public void testIdUrlForUnrestrictedIds() {
        String unRestrictedId = "m100000";
        IdWrapper idWrapper = IdWrapper.getIdWrapperFromUrlId(unRestrictedId);
        
        
        unRestrictedId = "c100000";
        idWrapper = IdWrapper.getIdWrapperFromUrlId(unRestrictedId);
        validateEqualityForUnrestrictedIds(unRestrictedId, idWrapper);
        
        unRestrictedId = "r100000";
        idWrapper = IdWrapper.getIdWrapperFromUrlId(unRestrictedId);
        validateEqualityForUnrestrictedIds(unRestrictedId, idWrapper);
    }
    
    private void validateEqualityForUnrestrictedIds(String unRestrictedId, IdWrapper idWrapper) {
        assertEquals(unRestrictedId, idWrapper.getIdForRepository());
        assertEquals(unRestrictedId, idWrapper.getIdForUrls());
    }
    
    
    @Test
    public void testIdUrlForRestrictedIds() {
        String restrictedIdIntPart = "m0001";
        IdWrapper idWrapper = IdWrapper.getIdWrapperFromUrlId(restrictedIdIntPart);
        assertEquals(restrictedIdIntPart, idWrapper.getIdForUrls());
        assertEquals("m1", idWrapper.getIdForRepository());
        
        restrictedIdIntPart = "c0001";
        idWrapper = IdWrapper.getIdWrapperFromUrlId(restrictedIdIntPart);
        assertEquals(restrictedIdIntPart, idWrapper.getIdForUrls());
        assertEquals("c1", idWrapper.getIdForRepository());
        
        // TODO(arjuns) : Validate for smaller resource ids.
        restrictedIdIntPart = "r0001";
        idWrapper = IdWrapper.getIdWrapperFromUrlId(restrictedIdIntPart);
        assertEquals(restrictedIdIntPart, idWrapper.getIdForUrls());
        assertEquals("r1", idWrapper.getIdForRepository());
    }
    
    @Test
    public void testIdRepositoryForUnrestrictedIds() {
        String unRestrictedId = "m100000";
        IdWrapper idWrapper = IdWrapper.getIdWrapperFromRepositoryId(unRestrictedId);
        validateEqualityForUnrestrictedIds(unRestrictedId, idWrapper);
        
        unRestrictedId = "c1000000";
        idWrapper = IdWrapper.getIdWrapperFromRepositoryId(unRestrictedId);
        validateEqualityForUnrestrictedIds(unRestrictedId, idWrapper);
        
        unRestrictedId = "r100000";
        idWrapper = IdWrapper.getIdWrapperFromRepositoryId(unRestrictedId);
        validateEqualityForUnrestrictedIds(unRestrictedId, idWrapper);
    }
    
    @Test
    public void testIdRepositoryForRestrictedIds() {
        String restrictedRepoId = "m1";
        IdWrapper idWrapper = IdWrapper.getIdWrapperFromRepositoryId(restrictedRepoId);
        assertEquals("m0001", idWrapper.getIdForUrls());
        assertEquals(restrictedRepoId, idWrapper.getIdForRepository());
        
        restrictedRepoId = "c1";
        idWrapper = IdWrapper.getIdWrapperFromRepositoryId(restrictedRepoId);
        assertEquals("c0001", idWrapper.getIdForUrls());
        assertEquals(restrictedRepoId, idWrapper.getIdForRepository());
        
        restrictedRepoId = "r1";
        idWrapper = IdWrapper.getIdWrapperFromRepositoryId(restrictedRepoId);
        assertEquals("r0001", idWrapper.getIdForUrls());
        assertEquals(restrictedRepoId, idWrapper.getIdForRepository());
    }
    
    @Test
    public void testIdCnxOrgForModules() {
        String restrictedRepoId = "m1";
        IdWrapper idWrapper = IdWrapper.getIdWrapperFromRepositoryId(restrictedRepoId);
        assertEquals("m0001", idWrapper.getIdForCnxOrg());
        
        restrictedRepoId = "m01";
        idWrapper = IdWrapper.getIdWrapperFromRepositoryId(restrictedRepoId);
        assertEquals("m0001", idWrapper.getIdForCnxOrg());
        
        restrictedRepoId = "m10085";
        idWrapper = IdWrapper.getIdWrapperFromRepositoryId(restrictedRepoId);
        assertEquals("m10085", idWrapper.getIdForCnxOrg());
    }
    
    @Test
    public void testIdCnxOrgForCollections() {
        String restrictedRepoId = "c1";
        IdWrapper idWrapper = IdWrapper.getIdWrapperFromRepositoryId(restrictedRepoId);
        assertEquals("col0001", idWrapper.getIdForCnxOrg());
        
        restrictedRepoId = "c01";
        idWrapper = IdWrapper.getIdWrapperFromRepositoryId(restrictedRepoId);
        assertEquals("col0001", idWrapper.getIdForCnxOrg());
        
        restrictedRepoId = "c10064";
        idWrapper = IdWrapper.getIdWrapperFromRepositoryId(restrictedRepoId);
        assertEquals("col10064", idWrapper.getIdForCnxOrg());
    }
}
