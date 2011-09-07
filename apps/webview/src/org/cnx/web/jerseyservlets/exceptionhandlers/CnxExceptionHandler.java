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
package org.cnx.web.jerseyservlets.exceptionhandlers;

import org.cnx.exceptions.CnxException;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Exception Handler for CnxException for Jersey Servlets.
 * 
 * @author Arjun Satyapal
 */
@Provider
public class CnxExceptionHandler implements ExceptionMapper<CnxException> {
    private ExceptionLogger exceptionLogger = new ExceptionLogger(
            CnxExceptionHandler.class.getName());

    /**
     * Handles {@link CnxException}.
     */
    @Override
    public Response toResponse(CnxException exception) {
        exceptionLogger.logException(exception);
        return exceptionLogger.getResponseForException(exception);
    }
}
