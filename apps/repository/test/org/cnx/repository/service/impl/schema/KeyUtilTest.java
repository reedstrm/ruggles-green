package org.cnx.repository.service.impl.schema;

import static org.junit.Assert.assertEquals;

import java.util.Random;

import org.cnx.repository.service.impl.persistence.IdUtil;
import org.junit.Test;

public class KeyUtilTest {
    @Test
    public void testIdToString() {
        assertEquals("0", IdUtil.idToString("", 0));
        assertEquals("1", IdUtil.idToString("", 1));
        assertEquals("9223372036854775807", IdUtil.idToString("", 9223372036854775807L));

        assertEquals("X0", IdUtil.idToString("X", 0));
        assertEquals("X1", IdUtil.idToString("X", 1));
        assertEquals("X9223372036854775807", IdUtil.idToString("X", 9223372036854775807L));
    }

    @Test
    public void compatability() {
        final long values[] = { 0, 1, Long.MAX_VALUE, 0x7fffffffffffffffL };

        for (Long value : values) {
            assertEquals(value, IdUtil.stringToId("X", IdUtil.idToString("X", value)));
        }
    }

    @Test
    public void randomCompatibility() {
        final Random rnd = new Random(1);
        for (int i = 0; i < 10000; i++) {
            final Long value = Math.abs(rnd.nextLong());
            assertEquals(value, IdUtil.stringToId("X", IdUtil.idToString("X", value)));
        }
    }
}
