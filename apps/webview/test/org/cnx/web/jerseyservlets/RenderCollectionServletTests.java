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
package org.cnx.web.jerseyservlets;

import static org.cnx.web.jerseyservlets.RenderCollectionServlet.COLLECTION_VERSION_MODULE_VERSION_RESOURCE_MAPPING_URL;
import static org.cnx.web.jerseyservlets.RenderCollectionServlet.COLLECTION_VERSION_MODULE_VERSION_URI;
import static org.cnx.web.jerseyservlets.RenderCollectionServlet.COLLECTION_VERSION_MODULE_VERSION_XML_URI;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 * Tests for {@link RenderCollectionServlet}.
 * 
 * @author Arjun Satyapal
 */
public class RenderCollectionServletTests {
    @Test
    public void test_uris() {
        assertEquals("/{collectionId}/{collectionVersion}/module/{moduleId}/{moduleVersion}",
                COLLECTION_VERSION_MODULE_VERSION_URI);

        assertEquals("/{collectionId}/{collectionVersion}/module/{moduleId}/{moduleVersion}/xml",
                COLLECTION_VERSION_MODULE_VERSION_XML_URI);

        assertEquals(
                "/{collectionId}/{collectionVersion}/module/{moduleId}/{moduleVersion}/resources",
                COLLECTION_VERSION_MODULE_VERSION_RESOURCE_MAPPING_URL);
    }
}
