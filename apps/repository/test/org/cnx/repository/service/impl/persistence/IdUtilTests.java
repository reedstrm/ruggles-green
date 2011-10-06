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
package org.cnx.repository.service.impl.persistence;

import static org.junit.Assert.assertEquals;

import java.util.Random;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

/**
 * TODO(tal): make this test to work. Requires testing jars as explained in
 * http://code.google.com/appengine/docs/java/tools/localunittesting.html
 * 
 * @author Tal Dayan
 */

public class IdUtilTests {

    private final LocalServiceTestHelper helper =
            new LocalServiceTestHelper(
                    new LocalDatastoreServiceTestConfig()
                    .setDefaultHighRepJobPolicyUnappliedJobPercentage(100));

    @Before
    public void setUp() {
        helper.setUp();
    }

    @After
    public void tearDown() {
        helper.tearDown();
    }

    @Test
    public void idToKey() {
        final OrmEntitySpec spec = new OrmEntitySpec("spec", "X");

        // Good ids
        assertEquals(KeyFactory.createKey("spec", 1), IdUtil.idToKey(spec, "X0001"));
        assertEquals(KeyFactory.createKey("spec", 9223372036854775807L), IdUtil.idToKey(spec, "X9223372036854775807"));

        // Bad ids
        assertEquals(null, IdUtil.idToKey(spec, "X0"));
        assertEquals(null, IdUtil.idToKey(spec, "X1"));
        assertEquals(null, IdUtil.idToKey(spec, "X001"));
        assertEquals(null, IdUtil.idToKey(spec, "X00001"));
        assertEquals(null, IdUtil.idToKey(spec, "X-0001"));
    }

    @Test
    public void keyToId() {
        final OrmEntitySpec spec = new OrmEntitySpec("spec", "X");

        assertEquals("X0001", IdUtil.keyToId(spec, KeyFactory.createKey("spec", 1)));
        assertEquals("X9223372036854775807",
                IdUtil.keyToId(spec, KeyFactory.createKey("spec", 9223372036854775807L)));
    }

    @Test
    public void ompatability() {
        final long ids[] = { 1, Long.MAX_VALUE, 0x7fffffffffffffffL };

        final OrmEntitySpec spec = new OrmEntitySpec("spec", "X");
        for (Long id : ids) {
            final Key actual = KeyFactory.createKey("spec", id);
            final Key expected = IdUtil.idToKey(spec, IdUtil.keyToId(spec, actual));
            assertEquals(actual, expected);
        }
    }

    @Test
    public void randomCompatibility() {
        final Random rnd = new Random(1);
        final OrmEntitySpec spec = new OrmEntitySpec("spec", "X");
        for (int i = 0; i < 10000;) {
            final long id = rnd.nextLong();
            // Only key ids >= 1 are valid and used.
            //
            // NOTE(tal): since class Random is deterministic, once this test converges,
            // it will always converge (no risk of a long sequence of < 1 ids).
            if (id >= 1) {
                i++;
                final Key actual = KeyFactory.createKey("spec", id);
                final Key expected = IdUtil.idToKey(spec, IdUtil.keyToId(spec, actual));
                assertEquals(actual, expected);
            }
        }
    }
}
