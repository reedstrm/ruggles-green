package org.cnx.repository.service.impl.schema;

// TODO(tal): move test to a parallel source directory structure.

import java.util.Random;

public class KeyUtilTest {
    @Test
    public void testIdToString() {
        assertEquals("AAAAAAAAAAAAA", KeyUtil.idToString("", 0));
        assertEquals("AAAAAAAAAAAAB", KeyUtil.idToString("", 1));
        assertEquals("Pffffffffffff", KeyUtil.idToString("", -1));
        assertEquals("JQdSUGIIBENCW", KeyUtil.idToString("", -7460683158697921450L));
        assertEquals("Hffffffffffff", KeyUtil.idToString("", 9223372036854775807L));
        assertEquals("IAAAAAAAAAAAA", KeyUtil.idToString("", -9223372036854775808L));

        assertEquals("XAAAAAAAAAAAAA", KeyUtil.idToString("X", 0));
        assertEquals("XAAAAAAAAAAAAB", KeyUtil.idToString("X", 1));
        assertEquals("XPffffffffffff", KeyUtil.idToString("X", -1));
        assertEquals("XJQdSUGIIBENCW", KeyUtil.idToString("X", -7460683158697921450L));
        assertEquals("XHffffffffffff", KeyUtil.idToString("X", 9223372036854775807L));
        assertEquals("XIAAAAAAAAAAAA", KeyUtil.idToString("X", -9223372036854775808L));

        assertEquals("CnxLiveawesome", KeyUtil.idToString("C", -8597248582506601250L));
    }

    @Test
    public void compatability() {
        final long values[] =
            { 0, 1, -1, 0x9876543210123456L, Long.MAX_VALUE, Long.MIN_VALUE, 0xffffffffffffffffL,
                0x8000000000000000L, 0x7fffffffffffffffL };

        for (Long value : values) {
            assertEquals(value, KeyUtil.stringToId("X", KeyUtil.idToString("X", value)));
        }
    }

    @Test
    public void randomCompatibility() {
        final Random rnd = new Random(1);
        for (int i = 0; i < 10000; i++) {
            final Long value = rnd.nextLong();
            assertEquals(value, KeyUtil.stringToId("X", KeyUtil.idToString("X", value)));
        }
    }
}
