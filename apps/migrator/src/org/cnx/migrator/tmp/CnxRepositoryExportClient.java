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

package org.cnx.migrator.tmp;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;

/**
 * Temp CNX repository exports client.
 * <p
 * ? TODO(tal): delete this class when the official export API will be stable.
 * 
 * @author Tal Dayan
 * 
 */
public class CnxRepositoryExportClient {

    private static final Pattern EXPORT_UPLOAD_URL_PATTERN = Pattern
            .compile("^upload url: (http:[\\n]+)$");

    private final HttpClient httpClient;
    private final String repositoryBaseUrl;

    public CnxRepositoryExportClient(final String repositoryBaseUrl) {
        this.repositoryBaseUrl = checkNotNull(repositoryBaseUrl);
        this.httpClient = new HttpClient();
    }

    public String getExportUploadUrl() throws Exception {
        final String url = repositoryBaseUrl + "/export";
        final GetMethod method = new GetMethod(url);
        method.setFollowRedirects(false);

        String responseBody = null;
        try {
            httpClient.executeMethod(method);
            responseBody = method.getResponseBodyAsString();

            final Matcher matcher = EXPORT_UPLOAD_URL_PATTERN.matcher(responseBody);
            if (!matcher.matches()) {
                throw new Exception(
                        "Could not find export upload URL in repository response from: " + url);
            }
            return matcher.group(1);
        } catch (Exception e) {
            throw new Exception("HTTP error connecting to :" + url, e);
        }
    }

    public void uploadExport(String uploadUrl, File file) {
        throw new RuntimeException("Not implemented yet");
    }

}
