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

package org.cnx.repository.atompub.utils;

import static org.cnx.repository.atompub.utils.PrettyXmlOutputter.prettyXmlOutputEntry;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import com.google.appengine.api.utils.SystemProperty;
import com.google.common.base.Charsets;

import com.sun.syndication.feed.atom.Entry;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.impl.Atom10Parser;

import org.cnx.exceptions.CnxBadRequestException;
import org.cnx.exceptions.CnxException;
import org.jdom.JDOMException;

/**
 * Server related utils.
 *
 * @author Tal Dayan
 */
public class ServerUtil {
    /**
     * Compute local host base url from an incoming httpRequest.
     *
     * @param httpRequest an incoming HTTP request.
     * @return host URL (e.g. "http://myserver.com" or "http://localhost:8888"
     */
    public static String computeHostUrl(HttpServletRequest httpRequest) {
        final String scheme = httpRequest.getScheme();
        final int port = httpRequest.getLocalPort();

        final URL serverUrl;
        try {

            if (SystemProperty.environment.value() == SystemProperty.Environment.Value.Development) {
                serverUrl = new URL(scheme, httpRequest.getLocalName(), port, "");
            } else {
                String requestUrl = httpRequest.getRequestURL().toString();
                String urlOfInterest = requestUrl.substring(0, requestUrl.indexOf(".appspot.com"));

                serverUrl = new URL(urlOfInterest + ".appspot.com");
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("Could not construct host url", e);
        }

        return serverUrl.toString();
    }
    
    /**
     * Return Entry posted by Client from Request.
     */
    public static Entry getPostedEntry(Logger logger, HttpServletRequest req) throws CnxException {
        Entry postedEntry = null;
        try {
            postedEntry =
                Atom10Parser.parseEntry(
                        new BufferedReader(new InputStreamReader(req.getInputStream(),
                            Charsets.UTF_8.displayName())), null);
        } catch (IllegalArgumentException e) {
            throw new CnxBadRequestException("Parsing Error.", e);
        } catch (UnsupportedEncodingException e) {
            throw new CnxBadRequestException("Invalid Encoding.", e);
        } catch (JDOMException e) {
            throw new CnxBadRequestException("Jdom Exception.", e);
        } catch (IOException e) {
            throw new CnxBadRequestException("IOException.", e);
        } catch (FeedException e) {
            throw new CnxBadRequestException("FeedException.", e);
        }

        logger.fine("Received Entry : " + prettyXmlOutputEntry(postedEntry));
        return postedEntry;
    }
}
