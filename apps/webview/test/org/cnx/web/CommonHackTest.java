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
package org.cnx.web;

import static org.junit.Assert.fail;

import org.cnx.exceptions.CnxInvalidUrlException;
import org.cnx.exceptions.CnxPossibleValidIdException;
import org.cnx.repository.atompub.IdWrapper;
import org.junit.Test;

/**
 * Test for {@link CommonHack}
 * 
 * @author Arjun Satyapal
 */
public class CommonHackTest {
    @Test
    public void test_handleCnxInvalidUrlException() {
        // First testing for Ids in restricted range. Here CnxPossibleValidIdException should
        // be thrown.
        IdWrapper idWrapper = new IdWrapper("m01234", IdWrapper.Type.MODULE);

        try {
            CommonHack.handleCnxInvalidUrlException(idWrapper, new CnxInvalidUrlException("test",
                    null /* throwable */));
            fail("Should have failed with " + CnxPossibleValidIdException.class.getName());
        } catch (CnxPossibleValidIdException e) {
            // expected
        }

        // Now testing for Ids beyond restricted range. Here CnxInvalidUrlException should be
        // thrown.
        idWrapper = new IdWrapper("m100000", IdWrapper.Type.MODULE);

        try {
            CommonHack.handleCnxInvalidUrlException(idWrapper, new CnxInvalidUrlException("test",
                    null /* throwable */));
            fail("Should have failed with " + CnxInvalidUrlException.class.getName());
        } catch (CnxPossibleValidIdException e) {
            fail("Expected : " + CnxInvalidUrlException.class.getName());
        } catch (CnxInvalidUrlException e) {
            // expected.
        }
    }
}
