/*
 * Copyright 2011 Google Inc.
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
package org.cnx.repository.atompub.service;

import com.sun.syndication.propono.atom.server.AtomHandler;
import com.sun.syndication.propono.atom.server.AtomHandlerFactory;

import org.cnx.repository.atompub.servlets.servicedocument.CnxAtomServiceDocumentHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Utility class to create Handlers for Cnx AtomPub resources.
 *
 * @author Arjun Satyapal
 */
public class CnxAtomHandlerUtils {
    private CnxAtomHandlerUtils() {
    }

    /**
     * Creates AtomHandler for Cnx AtomPub Service Document.
     */
    public static CnxAtomServiceDocumentHandler createCnxServiceAtomHandler(HttpServletRequest req,
            HttpServletResponse res) {
        return (CnxAtomServiceDocumentHandler) getAtomHandler(req, res, CnxAtomHandlerEnum.SERVICE);
    }

    /**
     * Creates AtomHandler for CnxAtomPub Resources.
     */
    private static AtomHandler createAtomRequestHandler(HttpServletRequest request,
            HttpServletResponse response, CnxAtomHandlerEnum handlerType) {
        CnxAtomHandlerFactory ahf = getCnxAtomHandlerFactory();
        ahf.setHandlerType(handlerType);
        return ahf.newAtomHandler(request, response);
    }

    private static CnxAtomHandlerFactory getCnxAtomHandlerFactory() {
        /*
         * Ensuring that CnxAtomHandlerFactory is created. It is possible that wrong factory is
         * created if propno.properties file is missing. This will ensure that Correct Factory is
         * created.
         */
        return (CnxAtomHandlerFactory) AtomHandlerFactory.newInstance();
    }

    private static AtomHandler getAtomHandler(HttpServletRequest req, HttpServletResponse res,
            CnxAtomHandlerEnum handlerType) {
        return createAtomRequestHandler(req, res, handlerType);
    }
}
