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
import java.net.URL;
import org.cnx.repository.atompub.ServletUris;

/**
 * Utility class for AtomCategories.
 * 
 * TODO(arjuns) : Revisit this class.
 * @author Arjun Satyapal
 */
public class CnxAtomCategoryUtils {
    // Utility class.
    private CnxAtomCategoryUtils() {
    }

    /**
     * Get Category for CNX Resources.
     * 
     * @param resourceScheme Scheme for CNX Resources.
     */
    public static Category getCnxResourceCategoryEle(URL resourceScheme) {
        return getCnxCategoryEle(resourceScheme, ServletUris.Resource.RESOURCE_SERVLET,
                ServletUris.Resource.RESOURCE_SERVLET);
    }

    /**
     * Get Category for CNX Modules..
     * 
     * @param moduleScheme Scheme for CNX Modules..
     */
    public static Category getCnxModuleCategoryEle(URL moduleScheme) {
        return getCnxCategoryEle(moduleScheme, ServletUris.Module.MODULE_SERVLET,
                ServletUris.Module.MODULE_SERVLET);
    }

    /**
     * Get Category for CNX Collection.
     * 
     * @param cnxCollectionScheme Scheme for CNX Collections.
     */
    public static Category getCnxCollectionCategoryEle(URL cnxCollectionScheme) {
        return getCnxCategoryEle(cnxCollectionScheme,
                ServletUris.Collection.COLLECTION_SERVLET,
                ServletUris.Collection.COLLECTION_SERVLET);
    }

    private static Category getCnxCategoryEle(URL scheme, String label, String term) {
        Category category = new Category();
        category.setLabel(label);
        category.setTerm(term);
        category.setScheme(scheme.toString());

        return category;
    }
}
