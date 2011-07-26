/*
 *  Copyright 2011 Google Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.cnx.html;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class CounterTests {
    private Counter counter;

    @Before public void createCounter() {
        counter = new Counter();
    }

    @Test public void newCounterShouldReturnOne() {
        assertEquals(1, counter.getNextNumber("foo", "bar"));
    }

    @Test public void nextShouldIncrease() {
        for (int i = 1; i <= 1000; i++) {
            assertEquals(i, counter.getNextNumber("foo", "bar"));
        }
    }

    @Test public void nextShouldNotAffectOtherTypes() {
        counter.getNextNumber("foo", "bar");
        assertEquals(1, counter.getNextNumber("foo", "other"));
        assertEquals(2, counter.getNextNumber("foo", "bar"));
    }

    @Test public void nextShouldNotAffectOtherElements() {
        counter.getNextNumber("foo", "bar");
        assertEquals(1, counter.getNextNumber("other", "bar"));
        assertEquals(2, counter.getNextNumber("foo", "bar"));
    }

    @Test public void resetShouldSetToOne() {
        assertEquals(1, counter.getNextNumber("foo", "bar"));
        assertEquals(2, counter.getNextNumber("foo", "bar"));
        counter.reset("foo", "bar");
        assertEquals(1, counter.getNextNumber("foo", "bar"));
    }

    @Test public void resetShouldNotAffectOtherTypes() {
        counter.getNextNumber("foo", "bar");
        counter.getNextNumber("foo", "other");
        counter.reset("foo", "bar");
        assertEquals(2, counter.getNextNumber("foo", "other"));
    }

    @Test public void resetShouldNotAffectOtherElements() {
        counter.getNextNumber("foo", "bar");
        counter.getNextNumber("other", "bar");
        counter.reset("foo", "bar");
        assertEquals(2, counter.getNextNumber("other", "bar"));
    }
}
