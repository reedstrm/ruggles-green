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

import static org.cnx.common.repository.atompub.CnxAtomPubUtils.LATEST_VERSION_STRING;
import static org.junit.Assert.*;

import org.cnx.common.repository.atompub.CnxAtomPubUtils;
import org.cnx.common.repository.atompub.VersionWrapper;

import org.cnx.common.exceptions.CnxInvalidUrlException;

import java.util.List;

import com.google.common.collect.Lists;

import org.junit.Test;

/**
 * Test for {@link VersionWrapper}
 * 
 * @author Arjun Satyapal
 */
public class VersionWrapperTests {

    @Test
    public void testVersionWrapperConstructor_long() {
        VersionWrapper versionWrapper = new VersionWrapper(100);
        assertEquals("100", versionWrapper.toString());

        // Testing for invalid values.
        try {
            new VersionWrapper(-1);
            fail("should have failed");
        } catch (CnxInvalidUrlException e) {
            // expected.
        }
    }

    @Test
    public void testVersionWrapperConstructor_string() {
        VersionWrapper versionWrapper = new VersionWrapper("100");
        assertEquals("100", versionWrapper.toString());

        versionWrapper = new VersionWrapper(LATEST_VERSION_STRING);
        assertNull(versionWrapper.getVersionInt());

        // Now testing for invalid values.
        List<String> listOfInvalidIds = Lists.newArrayList("-100", "-1.1", "0.1", "100.1");
        for (String currId : listOfInvalidIds) {
            try {
                new VersionWrapper(currId);
                fail("should have failed");
            } catch (CnxInvalidUrlException e) {
                // expected.
            }
        }
    }

    @Test
    public void testVersionWrapper_isValidVersion() {
        List<String> listOfInvalidVersions =
                Lists.newArrayList("asdf", "0.1", "1.1", "-1", "-1.1");

        confirmInvalidityOfVersion(listOfInvalidVersions);
    }

    private void confirmInvalidityOfVersion(List<String> listOfInvalidVersions) {
        for (String currVersion : listOfInvalidVersions) {
            assertFalse(currVersion + " should be invalide",
                    VersionWrapper.isValidVersion(currVersion));
        }
    }

    @Test
    public void testNextVersion() {
        VersionWrapper versionWrapper = new VersionWrapper(0);
        assertEquals("1", versionWrapper.getNextVersion().toString());

        versionWrapper = new VersionWrapper(1);
        assertEquals("2", versionWrapper.getNextVersion().toString());

        versionWrapper = new VersionWrapper(Integer.MAX_VALUE);
        try {
            versionWrapper.getNextVersion();
        } catch (CnxInvalidUrlException e) {
            // expected.
        }

        versionWrapper = CnxAtomPubUtils.LATEST_VERSION_WRAPPER;
        try {
            versionWrapper.getNextVersion();
        } catch (IllegalStateException e) {
            // expected.
        }
    }
    
    @Test
    public void testEquals() {
        assertEquals(new VersionWrapper(1), new VersionWrapper("1"));
        
        assertEquals(CnxAtomPubUtils.LATEST_VERSION_WRAPPER, new VersionWrapper("latest"));
    }
}
