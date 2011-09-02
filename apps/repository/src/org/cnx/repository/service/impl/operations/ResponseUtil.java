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

package org.cnx.repository.service.impl.operations;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nullable;

import org.cnx.repository.service.api.RepositoryResponse;
import org.cnx.repository.service.api.RepositoryStatus;

/**
 * Internal utilities related to repository responses.
 * 
 * @author Tal Dayan
 */
class ResponseUtil {
    /**
     * Internal method to construct and log error responses.
     * 
     * @param <T> result type
     * @param status response status. Asserted to have isError() true;
     * @param statusDescription A short human friendly description of the error.
     * @param log the logger to use.
     * @param level the logging level.
     * @param e optional exception to log. Null if no exception.
     * @return A logged repository error response
     */
    private static <T> RepositoryResponse<T> internalLoggedError(RepositoryStatus status,
            String statusDescription, Logger log, Level level, @Nullable Throwable e) {
        final String message;
        if (e != null) {
            message = statusDescription + " (" + e.getMessage() + ")";
            log.log(level, statusDescription, e);
        } else {
            message = statusDescription;
            log.log(level, statusDescription);
        }
        return RepositoryResponse.newError(status, message);
    }

    /**
     * Map repository status to log level.
     */
    private static Level statusLogLevel(RepositoryStatus status) {
        checkNotNull(status, "null status");
        switch (status) {
            case SERVER_ERROR:
                return Level.SEVERE;
            case OK:
                return Level.INFO;
            default:
                return Level.WARNING;
        }
    }

    static <T> RepositoryResponse<T> loggedError(RepositoryStatus status, String statusDescription,
            Logger log, Throwable e) {
        return internalLoggedError(status, statusDescription, log, statusLogLevel(status), e);
    }

    static <T> RepositoryResponse<T> loggedError(RepositoryStatus status, String statusDescription,
            Logger log) {
        return internalLoggedError(status, statusDescription, log, statusLogLevel(status), null);
    }

    /**
     * A method to construct and log an OK repository response.
     * 
     * @param <T> the result type.
     * @param statusDescription response description (short, human readable).
     * @param result the operation result.
     * @param log log to use for logging.
     * @return the logged repository response.
     */
    public static <T> RepositoryResponse<T>
            loggedOk(String statusDescription, T result, Logger log) {
        log.info(statusDescription);
        return RepositoryResponse.newOk(statusDescription, result);
    }

}
