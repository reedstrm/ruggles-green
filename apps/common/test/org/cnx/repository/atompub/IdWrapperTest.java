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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.google.common.collect.Lists;
import java.util.List;
import org.cnx.exceptions.CnxInvalidUrlException;
import org.junit.Test;

/**
 * Test for {@link IdWrapper}
 * 
 * @author Arjun Satyapal
 */
public class IdWrapperTest {

    @Test
    public void testIdsForUnrestrictedIds() {
        List<String> listOfValidIds = Lists.newArrayList("m100000","col100000", "r100000");
        
        for (String currString : listOfValidIds) {
            IdWrapper idWrapper = IdWrapper.getIdWrapper(currString);    
            validateEqualityForUnrestrictedIds(currString, idWrapper);
        }
    }

    private void validateEqualityForUnrestrictedIds(String unRestrictedId, IdWrapper idWrapper) {
        assertEquals(unRestrictedId, idWrapper.getId());
    }

    @Test
    public void testIdsForRestrictedIds() {
        List<String> listOfValidIds = Lists.newArrayList("m0001", "m0405", "col0001", "r0001");
        
        for (String currString : listOfValidIds) {
            IdWrapper idWrapper = IdWrapper.getIdWrapper(currString);    
            validateEqualityForUnrestrictedIds(currString, idWrapper);
        }
    }

    @Test
    public void testCnxInvalidUrlExceptionIsThrown() {
        List<String> listOfInvalidIds =
                Lists.newArrayList("abc", "module1234", "collection1234", "resource1234");
        confirmInvalidityOfIds(listOfInvalidIds);

        // Now testing for invalid moduleIds.
        listOfInvalidIds =
                Lists.newArrayList("m1a1", "m1", "m01", "m001", "m0000", "m0", "m0123a", "m00001");
        confirmInvalidityOfIds(listOfInvalidIds);

        // Now testing for invalid collections.
        listOfInvalidIds =
                Lists.newArrayList("col1a1", "col1", "col01", "col001", "col0000", "col0",
                        "col0123a", "col00001");
        confirmInvalidityOfIds(listOfInvalidIds);

        // Now testing for invalid ResourceIds.
        listOfInvalidIds =
                Lists.newArrayList("r1a1", "r1", "r01", "r001", "r0000", "r0", "r0123a", "r00001");
        confirmInvalidityOfIds(listOfInvalidIds);
    }

    private void confirmInvalidityOfIds(List<String> listOfIds) {
        for (String currId : listOfIds) {
            try {
                IdWrapper.getIdWrapper(currId);
                fail("Should have failed for id : [" + currId + "].");
            } catch (CnxInvalidUrlException e) {
                // expected
            }
        }
    }

    public void test_isIdUnderForcedRange() {
        List<String> listOfForcedIds =
                Lists.newArrayList("m0001", "m99999", "col0001", "col99999", "r0001", "r99999");
        
        for (String currId : listOfForcedIds) {
            IdWrapper idWrapper = IdWrapper.getIdWrapper(currId);
            assertTrue(idWrapper.isIdUnderForcedRange());
        }
        
        String maxLong = Long.toString(Long.MAX_VALUE);
        
        List<String> listOfIds =
                Lists.newArrayList("m100000", "m" + maxLong, "col100000", "col" + maxLong,
                        "r100000", "r" + maxLong);
        for (String currId : listOfIds) {
            IdWrapper idWrapper = IdWrapper.getIdWrapper(currId);
            assertFalse(idWrapper.isIdUnderForcedRange());
        }
        
    }
}
