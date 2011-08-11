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

package org.cnx.common.collxml;

import com.google.common.collect.ImmutableList;
import org.cnx.common.collxml.Collection.ModuleLink;
import org.cnx.util.DocumentBuilderProvider;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import static org.junit.Assert.*;

public class CollectionTests {
    private Document collxml;

    @Before public void createDummyDocument() {
        collxml = new DocumentBuilderProvider().get().newDocument();
    }

    @Test public void hasModuleShouldNotFindMissingModule() {
        final Collection coll = new Collection("col123", collxml, null, ImmutableList.of(
                new ModuleLink("m567", "latest", null)
        ));
        assertFalse(coll.hasModule("m123"));
    }

    @Test public void hasModuleShouldFindModule() {
        final Collection coll = new Collection("col123", collxml, null, ImmutableList.of(
                new ModuleLink("m567", "latest", null)
        ));
        assertTrue(coll.hasModule("m567"));
    }

    @Test public void hasModuleShouldFindSubmodule() {
        final Collection coll = new Collection("col123", collxml, null, ImmutableList.of(
                new ModuleLink("m567", "latest", null)
        ));
        assertTrue(coll.hasModule("m567"));
    }

    @Test public void getPreviousNextShouldReturnNullsWhenNotFound() {
        final Collection coll = new Collection("col123", collxml, null,
                ImmutableList.<ModuleLink>of());
        final ModuleLink[] result = coll.getPreviousNext("m0001");
        assertEquals(2, result.length);
        assertNull(result[0]);
        assertNull(result[1]);
    }

    @Test public void getPreviousNextShouldReturnNullsForSingleItem() {
        final Collection coll = new Collection("col123", collxml, null, ImmutableList.of(
                new ModuleLink("m0001", "latest", null)
        ));
        final ModuleLink[] result = coll.getPreviousNext("m0001");
        assertEquals(2, result.length);
        assertNull(result[0]);
        assertNull(result[1]);
    }

    @Test public void getPreviousNextShouldReturnSiblings() {
        final ImmutableList<ModuleLink> links = ImmutableList.of(
                new ModuleLink("m01", "latest", null),
                new ModuleLink("m02", "latest", null),
                new ModuleLink("m03", "latest", null)
        );
        final Collection coll = new Collection("col123", collxml, null, links);
        final ModuleLink[] result = coll.getPreviousNext("m02");
        assertEquals(2, result.length);
        assertEquals(links.get(0), result[0]);
        assertEquals(links.get(2), result[1]);
    }

    @Test public void getPreviousNextShouldHandleFirst() {
        final ImmutableList<ModuleLink> links = ImmutableList.of(
                new ModuleLink("m01", "latest", null),
                new ModuleLink("m02", "latest", null),
                new ModuleLink("m03", "latest", null)
        );
        final Collection coll = new Collection("col123", collxml, null, links);
        final ModuleLink[] result = coll.getPreviousNext("m01");
        assertEquals(2, result.length);
        assertNull(result[0]);
        assertEquals(links.get(1), result[1]);
    }

    @Test public void getPreviousNextShouldHandleLast() {
        final ImmutableList<ModuleLink> links = ImmutableList.of(
                new ModuleLink("m01", "latest", null),
                new ModuleLink("m02", "latest", null),
                new ModuleLink("m03", "latest", null)
        );
        final Collection coll = new Collection("col123", collxml, null, links);
        final ModuleLink[] result = coll.getPreviousNext("m03");
        assertEquals(2, result.length);
        assertEquals(links.get(1), result[0]);
        assertNull(result[1]);
    }
}
