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
 * See http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html
 * 
 * @author Arjun Satyapal
 */
public enum HttpStatusCategories {
    INFORMATIONAL(1),
    SUCCESSFUL(2),
    REDIRECTION(3),
    CLIENT_ERROR(4),
    SERVER_ERROR(5);
    
    private int startingDigit;
    
    private HttpStatusCategories(int startingDigit) {
        this.startingDigit = startingDigit;
    }
    
    public int getStartingDigit() {
        return startingDigit;
    }
}
