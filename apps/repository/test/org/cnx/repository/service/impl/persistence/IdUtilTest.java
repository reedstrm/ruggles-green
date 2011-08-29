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

public class IdUtilTest {

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
    public void keyToId() {
        final OrmEntitySpec spec0 = new OrmEntitySpec("spec0", "");
        final OrmEntitySpec spec1 = new OrmEntitySpec("spec1", "X");

        assertEquals("1", IdUtil.keyToId(spec0, KeyFactory.createKey("spec0", 1)));
        assertEquals("9223372036854775807",
                IdUtil.keyToId(spec0, KeyFactory.createKey("spec0", 9223372036854775807L)));

        assertEquals("X1", IdUtil.keyToId(spec1, KeyFactory.createKey("spec1", 1)));
        assertEquals("X9223372036854775807",
                IdUtil.keyToId(spec1, KeyFactory.createKey("spec1", 9223372036854775807L)));
    }

    @Test
    public void compatability() {
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
            // NOTE(tal): since Random is deterministic, one this test converge,
            // it will always converge (no risk for a long sequence of < 1 ids).
            if (id >= 1) {
                i++;
                final Key actual = KeyFactory.createKey("spec", id);
                final Key expected = IdUtil.idToKey(spec, IdUtil.keyToId(spec, actual));
                assertEquals(actual, expected);
            }
        }
    }
}
