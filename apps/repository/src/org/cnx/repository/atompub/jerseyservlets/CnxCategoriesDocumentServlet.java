///*
// * Copyright (C) 2011 The CNX Authors
// *
// * Licensed under the Apache License, Version 2.0 (the "License"); you may not
// * use this file except in compliance with the License. You may obtain a copy of
// * the License at
// *
// * http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
// * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
// * License for the specific language governing permissions and limitations under
// * the License.
// */
//package org.cnx.repository.atompub.jerseyservlets;
//
//import static org.cnx.repository.atompub.utils.CnxAtomCategoryUtils.getCnxCollectionCategoryEle;
//import static org.cnx.repository.atompub.utils.CnxAtomCategoryUtils.getCnxModuleCategoryEle;
//import static org.cnx.repository.atompub.utils.CnxAtomCategoryUtils.getCnxResourceCategoryEle;
//
//import com.sun.syndication.propono.atom.common.Categories;
//import javax.servlet.http.HttpServletRequest;
//import javax.ws.rs.GET;
//import javax.ws.rs.Path;
//import javax.ws.rs.Produces;
//import javax.ws.rs.core.Context;
//import javax.ws.rs.core.Response;
//import org.cnx.common.repository.ContentType;
//import org.cnx.common.repository.PrettyXmlOutputter;
//import org.cnx.common.repository.atompub.CnxAtomPubConstants;
//import org.cnx.common.repository.atompub.ServletUris;
//import org.cnx.repository.atompub.service.CnxAtomService;
//import org.cnx.repository.atompub.utils.ServerUtil;
//
///**
// * REST Resource for fetching ServiceDocument.
// * 
// * @author Arjun Satyapal
// */
//@Path(ServletUris.CategoryDocument.CATEGORY_DOCUMENT_SERVLET)
//public class CnxCategoriesDocumentServlet {
//    @GET
//    @Produces(ContentType.TEXT_XML_UTF8)
//    @Path(ServletUris.CategoryDocument.CATEGORY_DOCUMENT_PATH)
//    public Response getServiceDocument(@Context HttpServletRequest req) {
//        // TODO(arjuns) : Add caching.
//
//        CnxAtomService atomService = new CnxAtomService(ServerUtil.computeHostUrl(req));
//
//        CnxAtomPubConstants constants = atomService.getConstants();
//        Categories categories = new Categories();
//        categories.addCategory(getCnxResourceCategoryEle(constants.getAPCResourceScheme()));
//        categories.addCategory(getCnxModuleCategoryEle(constants.getAPCModuleScheme()));
//        categories.addCategory(getCnxCollectionCategoryEle(constants.getAPCCollectionScheme()));
//
//        return Response
//                .ok()
//                .entity(PrettyXmlOutputter.prettyXmlOutputElement(categories.categoriesToElement()))
//                .build();
//    }
//}
