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

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;

/**
 * Resource related utils.
 * 
 * @author Tal Dayan
 */
public class ResourceUtil {

    /**
     * Name of resource id param in resource upload completion URL. Should be web safe. Modifying
     * this value will break pending resource uploads.
     */
    private static final String RESOURCE_ID_PARAM = "id";

    /**
     * Return a resource id parameter from a request encoded by
     * {@link #encodeUploadCompletionParameters}.
     * 
     * @param the incoming request
     * @param defaultValue a String to return if param not found
     * 
     * @return the param value or defaultValue if param not found.
     */
    public static String getResourceIdParam(HttpServletRequest req, @Nullable String defaultValue) {
        String value = req.getParameter(RESOURCE_ID_PARAM);
        return (value == null) ? defaultValue : value;
    }

    /**
     * Construct request parameters representing a given export reference.
     * 
     * @return a string with the encoded parameters in the form name=value&name=value&... This
     *         encoding is compatible with {@link #exportReferenceFromRequestParameters}
     */
    public static String encodeUploadCompletionParameters(String resourceId) {
        return RESOURCE_ID_PARAM + "=" + resourceId;
    }

}
