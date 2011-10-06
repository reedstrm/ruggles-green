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
package org.cnx.common.http;

/**
 * Http Status Codes. See : http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html
 * 
 * @author Arjun Satyapal
 */
public enum HttpStatusEnum {
    OK(200, "OK", HttpStatusCategories.SUCCESSFUL),
    CREATED(201, "Created", HttpStatusCategories.SUCCESSFUL),
    
    MOVED_PERMANENTLY(301, "Moved Permanently", HttpStatusCategories.REDIRECTION),
    FOUND(302, "Found", HttpStatusCategories.REDIRECTION),
    SEE_OTHER(303, "See Other", HttpStatusCategories.SUCCESSFUL),
    
    BAD_REQUEST(400, "Bad Request", HttpStatusCategories.CLIENT_ERROR),
    UNAUTHORIZED(401, "Unauthorized", HttpStatusCategories.CLIENT_ERROR),
    FORBIDDEN(403, "Forbidden", HttpStatusCategories.CLIENT_ERROR),
    NOT_FOUND(404, "Not Found", HttpStatusCategories.CLIENT_ERROR),
    NOT_ACCEPTABLE(406, "Not Acceptable", HttpStatusCategories.CLIENT_ERROR),
    CONFLICT(409, "Conflict", HttpStatusCategories.CLIENT_ERROR),
    GONE(410, "Gone", HttpStatusCategories.CLIENT_ERROR),
    PRECONDITION_FAILED(412, "Precondition Failed", HttpStatusCategories.CLIENT_ERROR),
    UNSUPPORTED_MEDIA_TYPE(415, "Unsupported Media Type", HttpStatusCategories.CLIENT_ERROR),
    
    INTERNAL_SERVER_ERROR(500, "Internal Server Error", HttpStatusCategories.SERVER_ERROR);
    
    private int statusCode;
    private String reason;
    private HttpStatusCategories statusCategories;
    
    private HttpStatusEnum(int statusCode, String reason, HttpStatusCategories statusCategories) {
        this.statusCode = statusCode;
        this.reason = reason;
        this.statusCategories = statusCategories;
    }
    
    public int getStatusCode() {
        return statusCode;
    }
    
    public String getReason() {
        return reason;
    }
    
    public HttpStatusCategories getStatusCategories() {
        return statusCategories;
    }
    
    public static HttpStatusEnum getHttpStatusEnumByStatusCode(int statusCode) {
        for (HttpStatusEnum curr : HttpStatusEnum.values()) {
            if (curr.getStatusCode() == statusCode) {
                return curr;
            }
        }
        
        throw new RuntimeException("StatusCode[" + statusCode + "] not found.");
    }
    
}
