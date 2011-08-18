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

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletResponse;

/**
 * General utils for completion servlets.
 * 
 * @author Tal Dayan
 */
public class ServletUtil {
    /**
     * Setup to return a servlet error status.
     * 
     * @param resp the servlet response.
     * @param httpStatus the http status to return
     * @param message diagnostic text message to return
     * @param e optional exception to log, ignored if null.
     * @param log the logger to use.
     * @param level the log level to use.
     * @throws IOException
     */
    public static void setServletError(HttpServletResponse resp, int httpStatus, String message,
            @Nullable Throwable e, Logger log, Level level) throws IOException {
        final String httpMessage;
        if (e != null) {
            log.log(level, message, e);
            httpMessage = message + " " + e.getMessage();
        } else {
            log.log(level, message);
            httpMessage = message;
        }
        resp.sendError(httpStatus, httpMessage);
    }
}
