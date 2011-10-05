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
import com.google.inject.Injector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import org.cnx.common.exceptions.CnxException;
import org.cnx.common.exceptions.CnxPossibleValidIdException;
import org.cnx.common.exceptions.CnxRuntimeException;
import org.cnx.common.http.HttpStatusEnum;
import org.cnx.common.repository.ContentType;
import org.cnx.common.repository.atompub.CnxAtomPubUtils;
import org.cnx.web.ErrorPages;

/**
 * Utility class to help exception handlers.
 * 
 * @author Arjun Satyapal
 */
public class ExceptionLogger {
    private final Logger logger;
    private final Throwable throwable;
    private final long errorTrackingCode;
    private final ErrorPages errorPages;

    public ExceptionLogger(Injector injector, Throwable throwable) {
        this.throwable = throwable;

        this.logger = Logger.getLogger(throwable.getClass().getName());
        this.errorTrackingCode = System.currentTimeMillis();
        this.errorPages = injector.getInstance(ErrorPages.class);
    }

    private void updateTrackingCode(StringBuilder builder) {
        builder.append("ErrorTrackingCode = ").append(errorTrackingCode).append("\n");
    }

    // TODO(arjuns) : Add chrome around these exceptions.
    private String getErrorMessageToDisplay(HttpStatusEnum httpStatus, Throwable throwable) {
        StringBuilder builder = new StringBuilder();
        switch (httpStatus) {
            case NOT_FOUND:
                builder.append(errorPages.render404());
                break;

            case SEE_OTHER:
                if (throwable instanceof CnxPossibleValidIdException) {
                    CnxPossibleValidIdException exception = (CnxPossibleValidIdException) throwable;
                    // TODO(arjuns) : Move this to constant
                    String redirectUrl =
                            "http://cnx.org/content/" + exception.getId().getId()
                                    + "/" + CnxAtomPubUtils.LATEST_VERSION_STRING;
                    builder.append(errorPages.render404OldSite(redirectUrl));
                    break;
                }

                logger.severe("ExceptionType[" + throwable.getClass().getName()
                        + " is having StatusCode = " + httpStatus);

                //$FALL-THROUGH$
            default:
                logException();
                builder.append(errorPages.renderError(httpStatus.getStatusCode(),
                        Long.toString(errorTrackingCode), throwable.getMessage(), throwable));
        }

        return builder.toString();
    }

    public Response getResponseForException() {
        // Defaulting to internal server error.
        HttpStatusEnum httpStatus = HttpStatusEnum.INTERNAL_SERVER_ERROR;
        if (throwable instanceof CnxException) {
            CnxException exception = (CnxException) throwable;
            httpStatus = exception.getHttpStatus();
        } else if (throwable instanceof CnxRuntimeException) {
            CnxRuntimeException exception = (CnxRuntimeException) throwable;
            httpStatus = exception.getHttpStatus();
        } else {
            logger.severe("Unknown exception type : " + Throwables.getStackTraceAsString(throwable));
        }

        String errorMsg = getErrorMessageToDisplay(httpStatus, throwable);
        ResponseBuilder responseBuilder =
                Response.status(httpStatus.getStatusCode()).entity(errorMsg);
        responseBuilder.header("Content-Type", ContentType.TEXT_HTML_UTF8);
        return responseBuilder.build();
    }

    private void logException() {
        logExceptionMessage(Level.SEVERE);
    }

    private void logExceptionMessage(Level logLevel) {
        StringBuilder builder = new StringBuilder();
        updateTrackingCode(builder);
        builder.append(Throwables.getStackTraceAsString(throwable));

        logger.log(logLevel, builder.toString());
    }
}
