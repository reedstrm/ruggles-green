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
package org.cnx.repository.atompub.utils;

import com.sun.syndication.feed.atom.Category;
import com.sun.syndication.propono.atom.common.Categories;
import com.sun.syndication.propono.atom.common.Collection;
import java.net.URL;
import org.cnx.common.repository.atompub.CnxAtomPubUtils;

/**
 * Utility class for CNX AtomCollections.
 * 
 * @author Arjun Satyapal
 */
public class CnxAtomCollectionUtils {
    // Utility class.
    private CnxAtomCollectionUtils() {
    }

    public static Collection getCollectionForCnxResource(URL resourceScheme) {
        return getCnxCollection(CnxAtomPubUtils.COLLECTION_RESOURCE_TITLE, resourceScheme,
                CnxAtomCategoryUtils.getCnxResourceCategoryEle(resourceScheme));
    }

    public static Collection getCollectionForCnxModule(URL moduleScheme) {
        return getCnxCollection(CnxAtomPubUtils.COLLECTION_MODULE_TITLE, moduleScheme,
                CnxAtomCategoryUtils.getCnxModuleCategoryEle(moduleScheme));
    }

    public static Collection getCollectionForCnxCollection(URL cnxCollectionScheme) {
        return getCnxCollection(CnxAtomPubUtils.COLLECTION_CNX_COLLECTION_TITLE,
                cnxCollectionScheme,
                CnxAtomCategoryUtils.getCnxCollectionCategoryEle(cnxCollectionScheme));
    }

    private static Collection getCnxCollection(String collectionName, URL collectionPath,
            Category category) {
        Collection collection = new Collection(collectionName, null, collectionPath.toString());

        Categories categories = new Categories();
        categories.addCategory(category);
        collection.addCategories(categories);

        return collection;
    }
}
