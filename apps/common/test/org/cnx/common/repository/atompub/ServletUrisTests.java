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
package org.cnx.common.repository.atompub;

import static org.junit.Assert.assertEquals;

import org.cnx.common.repository.atompub.ServletUris;

import org.junit.Test;

/**
 * Tests for {@link ServletUris}.
 * 
 * This test is to ensure sanity of URLs. Any mistake here will throw whole system at toss.
 * 
 * @author Arjun Satyapal
 */
public class ServletUrisTests {

    @Test
    public void test_ServletUris_Fields() {
        ServletUris servletUris = new ServletUris();
        validateNumberOfFields(9, servletUris);

        // This class does not have any derived field yet.
    }

    /**
     * Test for {@link ServletUris.ServiceDocument}.
     */
    @Test
    public void test_ServiceDocument_Fields() {
        ServletUris.ServiceDocument serviceDocument = new ServletUris.ServiceDocument();
        validateNumberOfFields(3, serviceDocument);
        // This class does not have any derived field yet.
    }

    /**
     * Test for {@link ServletUris.CategoryDocument}.
     */
    @Test
    public void test_CategoryDocument_Fields() {
        ServletUris.CategoryDocument categoryDocument = new ServletUris.CategoryDocument();
        validateNumberOfFields(2, categoryDocument);
        // This class does not have any derived field yet.
    }

    /**
     * Test for {@link ServletUris.Resource}.
     */
    @Test
    public void test_Resource_Fields() {
        ServletUris.Resource resource = new ServletUris.Resource();
        validateNumberOfFields(5, resource);

        assertEquals("/migration/{resourceId}", ServletUris.Resource.RESOURCE_POST_MIGRATION);
        assertEquals("/{resourceId}", ServletUris.Resource.RESOURCE_PATH);
        assertEquals("/{resourceId}/info", ServletUris.Resource.RESOURCE_INFO);

        // This class has 2 static and 3 derived fields.
    }

    /**
     * Test for {@link ServletUris.Module}.
     */
    @Test
    public void test_Module_Fields() {
        ServletUris.Module module = new ServletUris.Module();
        validateNumberOfFields(8, module);

        assertEquals("/migration/{moduleId}", ServletUris.Module.MODULE_POST_MIGRATION);
        assertEquals("/migration/{moduleId}/{moduleVersion}",
                ServletUris.Module.MODULE_PUT_MIGRATION_VERSION);
        assertEquals("/{moduleId}", ServletUris.Module.MODULE_PATH);
        assertEquals("/{moduleId}/{moduleVersion}", ServletUris.Module.MODULE_VERSION_PATH);
        assertEquals("/{moduleId}/{moduleVersion}/xml", ServletUris.Module.MODULE_VERSION_CNXML);
        assertEquals("/{moduleId}/{moduleVersion}/resources",
                ServletUris.Module.MODULE_VERSION_RESOURCE_MAPPING);

        // This class has 2 static and 6 derived fields.
    }

    /**
     * Test for {@link ServletUris.Collection}.
     */
    @Test
    public void test_Collection_Fields() {
        ServletUris.Collection collection = new ServletUris.Collection();
        validateNumberOfFields(7, collection);

        assertEquals("/migration/{collectionId}", ServletUris.Collection.COLLECTION_POST_MIGRATION);
        assertEquals("/migration/{collectionId}/{collectionVersion}",
                ServletUris.Collection.COLLECTION_PUT_MIGRATION_VERSION);
        assertEquals("/{collectionId}", ServletUris.Collection.COLLECTION_PATH);
        assertEquals("/{collectionId}/{collectionVersion}",
                ServletUris.Collection.COLLECTION_VERSION_PATH);
        assertEquals("/{collectionId}/{collectionVersion}/xml",
                ServletUris.Collection.COLLECTION_VERSION_COLLXML);

        // This class has 2 static and 6 derived fields.
    }

    private void validateNumberOfFields(int expectedNumberOfFields, Object object) {
        assertEquals("Modify this test to reflect updates in Class[" + object.getClass().getName()
                + "]. All the derived fields should have a test here.", expectedNumberOfFields,
                object.getClass().getFields().length);
    }

}
