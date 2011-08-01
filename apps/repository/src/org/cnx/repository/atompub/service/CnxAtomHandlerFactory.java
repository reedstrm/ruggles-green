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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * rome-propono based Factory to provide AtomHandler.
 * This is controlled using SystemProperty
 *     com.sun.syndication.propono.atom.server.AtomHandlerFactory
 * whose value is provided in appengine-web.xml
 *
 * @author Arjun Satyapal
 */
public class CnxAtomHandlerFactory extends AtomHandlerFactory {
    /*
     * handlerType will determine which AtomHandler is returned by Factory.
     */
    private CnxAtomHandlerEnum handlerType;

    @Override
    public AtomHandler newAtomHandler(HttpServletRequest req, HttpServletResponse res) {
        return null;
        /*
         * rome-propono assumes that there is only one handler for the whole system. Whereas it is
         * better to have different AtomHandlers for each AtomPub collection.
         */
    }

    public CnxAtomHandlerEnum getHandlerType() {
        return handlerType;
    }

    /**
     * @param handlerType Type of CNX AtomHandler to be created.
     */
    public void setHandlerType(CnxAtomHandlerEnum handlerType) {
        this.handlerType = handlerType;
    }
}
