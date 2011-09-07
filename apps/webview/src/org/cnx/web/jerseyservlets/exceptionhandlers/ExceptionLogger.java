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

import com.google.common.base.Throwables;

import org.cnx.exceptions.CnxException;
import org.cnx.exceptions.CnxRuntimeException;
import org.cnx.repository.atompub.CnxMediaTypes;
import org.cnx.web.WebViewConfiguration;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

/**
 * Utility class to help exception handlers.
 * 
 * @author Arjun Satyapal
 */
public class ExceptionLogger {
    private final Logger logger;
    private final long errorTrackingCode;
    private final WebViewConfiguration config;

    public ExceptionLogger(String className) {
        logger = Logger.getLogger(className);
        this.errorTrackingCode = System.currentTimeMillis();
        config = new WebViewConfiguration();
    }

    private void updateTrackingCode(StringBuilder builder) {
        builder.append("ErrorTrackingCode = ").append(errorTrackingCode).append("\n");
    }

    // TODO(arjuns) : Add chrome around these exceptions.
    private String getErrorMessageToDisplay(Status jerseyStatus, Throwable throwable) {
        StringBuilder builder = new StringBuilder();
        builder.append("<h1> HTTP ERROR : ").append(jerseyStatus.getStatusCode())
                .append("</h1><br/>");
        updateTrackingCode(builder);

        if (config.isStackEnabled()) {
            builder.append("<br/><pre>").append(Throwables.getStackTraceAsString(throwable))
                    .append("</pre>");
        } else {
            builder.append("<br/>").append(throwable.getMessage());
        }

        return builder.toString();
    }

    public Response getResponseForException(Throwable throwable) {
        // Defaulting to internal server error.
        Status jerseyStatus = Status.INTERNAL_SERVER_ERROR;
        if (throwable instanceof CnxException) {
            CnxException exception = (CnxException) throwable;
            jerseyStatus = exception.getJerseyStatus();
        } else if (throwable instanceof CnxRuntimeException) {
            CnxRuntimeException exception = (CnxRuntimeException) throwable;
            jerseyStatus = exception.getJerseyStatus();
        } else {
            logger.severe("Unknown exception type : " + Throwables.getStackTraceAsString(throwable));
        }

        String errorMsg = getErrorMessageToDisplay(jerseyStatus, throwable);
        ResponseBuilder responseBuilder = Response.status(jerseyStatus).entity(errorMsg);
        responseBuilder.header("Content-Type", CnxMediaTypes.TEXT_HTML_UTF8);
        return responseBuilder.build();
    }

    public void logException(Throwable throwable) {
        logExceptionMessage(Level.SEVERE, throwable);
    }

    public void logExceptionMessage(Level logLevel, Throwable throwable) {
        StringBuilder builder = new StringBuilder();
        updateTrackingCode(builder);
        builder.append(Throwables.getStackTraceAsString(throwable));

        logger.log(logLevel, builder.toString());
    }
}
