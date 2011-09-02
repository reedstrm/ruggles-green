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

import org.cnx.exceptions.CnxInvalidUrlException;
import org.cnx.exceptions.CnxRuntimeException;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Exception Handler fork CnxRuntimeExceptions for Jersey Servlets.
 * 
 * @author Arjun Satyapal
 */
@Provider
public class CnxRuntimeExceptionHandler implements ExceptionMapper<CnxRuntimeException> {
    private ExceptionLogger exceptionLogger = new ExceptionLogger(
            CnxRuntimeExceptionHandler.class.getName());

    /**
     * Handles {@link CnxInvalidUrlException}.
     */
    @Override
    public Response toResponse(CnxRuntimeException exception) {
        exceptionLogger.logException(exception);
        return exceptionLogger.getResponseForException(exception);
    }
}
