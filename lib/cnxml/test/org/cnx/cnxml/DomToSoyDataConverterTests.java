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

package org.cnx.cnxml;

import org.cnx.cnxml.DomToSoyDataConverterImpl;
import org.junit.Test;
import static org.junit.Assert.*;

public class DomToSoyDataConverterTests {
    @Test public void cleanAttributeNameShouldNotModifyIdentifiers() {
        final String name = "fooBar_42";
        assertEquals(name, DomToSoyDataConverterImpl.xmlAttributeNameToSoyIdentifier(name));
    }

    @Test public void cleanAttributeNameShouldConvertHyphensToUnderscores() {
        assertEquals("target_id",
                DomToSoyDataConverterImpl.xmlAttributeNameToSoyIdentifier("target-id"));
    }

    @Test public void cleanAttributeNameShouldRemoveSpecials() {
        assertEquals("xmlnsbib",
                DomToSoyDataConverterImpl.xmlAttributeNameToSoyIdentifier("xmlns:bib"));
    }
}
