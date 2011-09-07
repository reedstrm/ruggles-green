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

package org.cnx.cnxml;

import static org.junit.Assert.*;

import org.junit.Test;

public class JdomHtmlGeneratorTests {
    @Test
    public void subfigurePrefixShouldProduceAlphabet() {
        assertEquals("(a)", JdomHtmlGenerator.getSubfigurePrefix(0));
        assertEquals("(d)", JdomHtmlGenerator.getSubfigurePrefix(3));
        assertEquals("(z)", JdomHtmlGenerator.getSubfigurePrefix(25));
    }

    @Test
    public void subfigurePrefixShouldHandleTwoDigits() {
        assertEquals("(aa)", JdomHtmlGenerator.getSubfigurePrefix(26));
        assertEquals("(az)", JdomHtmlGenerator.getSubfigurePrefix(51));
        assertEquals("(ca)", JdomHtmlGenerator.getSubfigurePrefix(78));
        assertEquals("(zz)", JdomHtmlGenerator.getSubfigurePrefix(701));
    }

    @Test
    public void subfigurePrefixShouldHandleThreeDigits() {
        assertEquals("(aaa)", JdomHtmlGenerator.getSubfigurePrefix(702));
        assertEquals("(abc)", JdomHtmlGenerator.getSubfigurePrefix(730));
        assertEquals("(zzz)", JdomHtmlGenerator.getSubfigurePrefix(18277));
    }

    @Test
    public void subfigurePrefixShouldHandleFourDigits() {
        assertEquals("(aaaa)", JdomHtmlGenerator.getSubfigurePrefix(18278));
        assertEquals("(feed)", JdomHtmlGenerator.getSubfigurePrefix(108969));
        assertEquals("(zzzz)", JdomHtmlGenerator.getSubfigurePrefix(475253));
    }
}
