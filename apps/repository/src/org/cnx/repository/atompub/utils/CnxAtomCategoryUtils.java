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

import java.net.URL;

import org.cnx.repository.atompub.CnxAtomPubConstants;

import com.sun.syndication.feed.atom.Category;

/**
 * Utility class for AtomCategories.
 * 
 * @author Arjun Satyapal
 */
public class CnxAtomCategoryUtils {
    // Utility class.
    private CnxAtomCategoryUtils() {
    }

    /**
     * Get Category for Cnx Resources.
     * 
     * @param resourceScheme Scheme for Cnx Resources.
     */
    public static Category getCnxResourceCategoryEle(URL resourceScheme) {
        return getCnxCategoryEle(resourceScheme, CnxAtomPubConstants.COLLECTION_RESOURCE_REL_PATH,
                CnxAtomPubConstants.COLLECTION_RESOURCE_REL_PATH);
    }

    /**
     * Get Category for Cnx Modules..
     * 
     * @param moduleScheme Scheme for Cnx Modules..
     */
    public static Category getCnxModuleCategoryEle(URL moduleScheme) {
        return getCnxCategoryEle(moduleScheme, CnxAtomPubConstants.COLLECTION_MODULE_REL_PATH,
                CnxAtomPubConstants.COLLECTION_MODULE_REL_PATH);
    }

    /**
     * Get Category for Cnx Collection..
     * 
     * @param cnxCollectionScheme Scheme for Cnx Collections.
     */
    public static Category getCnxCollectionCategoryEle(URL cnxCollectionScheme) {
        return getCnxCategoryEle(cnxCollectionScheme,
                CnxAtomPubConstants.COLLECTION_CNX_COLLECTION_REL_PATH,
                CnxAtomPubConstants.COLLECTION_CNX_COLLECTION_REL_PATH);
    }

    private static Category getCnxCategoryEle(URL scheme, String label, String term) {
        Category category = new Category();
        category.setLabel(label);
        category.setTerm(term);
        category.setScheme(scheme.toString());

        return category;
    }
}
