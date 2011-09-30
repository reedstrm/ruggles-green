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
package org.cnx.common.repository.atompub;

import java.net.URI;
import java.net.URISyntaxException;
import org.cnx.common.exceptions.CnxBadRequestException;

/**
 * Some utilities for AtomPub.
 * 
 * @author Arjun Satyapal
 */
public class CommonUtils {
    // Utility class.
    private CommonUtils() {
    }
    
    public static URI getURI(String uriString) throws CnxBadRequestException {
        try {
            return new URI(uriString);
        } catch (URISyntaxException e) {
            throw new CnxBadRequestException("Invalid uri : " + uriString, e);
        }
    }
    
    /**
     * This will append tail to origUri. This method ensures that there is only one '/' between
     * origUri and tail. Other then that, remaining part is left untouched.
     * 
     * TODO(arjuns) : Add test for this.
     * 
     * @param origUri Original Uri
     * @param tail Uri to be appended to origUri.
     * @return URI with concatenated values of origUri and tail.
     * @throws URISyntaxException 
     */
    public static URI appendUri(URI origUri, URI tail) throws URISyntaxException {
        String origUriString = origUri.toString();
        StringBuilder newUriStringBuilder = new StringBuilder(origUriString);
        
        // This ensures that before tail is appended, we have / in newUriStringBuilder.
        if (!origUriString.endsWith("/")) {
            newUriStringBuilder.append("/");
        }
        
        String tailUriString = tail.toString();
        int beginIndex = 0;
        
        // This ensures that if tail starts with '/' then it is ignored because newUriStringBuilder
        // alread has a '/' at the end.
        if (tailUriString.startsWith("/")) {
            beginIndex = 1;
        }
        
        newUriStringBuilder.append(tailUriString.substring(beginIndex));
        return new URI(newUriStringBuilder.toString());
    }
}
