/**
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
package org.cnx.repository.atompub.servlets;

import static org.cnx.repository.atompub.utils.CnxAtomCategoryUtils.getCnxCollectionCategoryEle;
import static org.cnx.repository.atompub.utils.CnxAtomCategoryUtils.getCnxModuleCategoryEle;
import static org.cnx.repository.atompub.utils.CnxAtomCategoryUtils.getCnxResourceCategoryEle;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.cnx.repository.atompub.utils.CnxAtomPubConstants;
import org.cnx.repository.atompub.utils.CustomMediaTypes;
import org.cnx.repository.atompub.utils.PrettyXmlOutputter;

import com.sun.syndication.propono.atom.common.Categories;

/**
 * REST Resource for fetching ServiceDocument.
 * 
 * @author Arjun Satyapal
 */
@Path(CnxAtomPubConstants.CATEGORIES_DOCUMENT_PATH)
public class CnxCategoriesDocumentServlet {
    private final String CATEGORY_DOCUMENT_GET = "/";

    @GET
    @Produces(CustomMediaTypes.APPLICATION_ATOM_XML)
    @Path(CATEGORY_DOCUMENT_GET)
    public Response getServiceDocument(@Context HttpServletRequest req,
            @Context HttpServletResponse response) {
        // TODO(arjuns) : Add caching and exception handling.

        CnxAtomPubConstants constants =
            new CnxAtomPubConstants(req.getRequestURL().toString(), req.getServerPort());
        Categories categories = new Categories();
        categories.addCategory(getCnxResourceCategoryEle(constants.getCollectionResourceScheme()));
        categories.addCategory(getCnxModuleCategoryEle(constants.getCollectionModuleScheme()));
        categories.addCategory(getCnxCollectionCategoryEle(constants
            .getCollectionCnxCollectionScheme()));

        return Response.ok()
            .entity(PrettyXmlOutputter.prettyXmlOutputElement(categories.categoriesToElement()))
            .build();
    }
}