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

//import java.awt.PageAttributes.MediaType;

import javax.annotation.Nullable;

import com.google.appengine.api.blobstore.BlobInfo;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;

/**
 * Blobstore related utils.
 * 
 * @author Tal Dayan
 */
public class BlobstoreUtil {

    final static ImmutableMap<String, String> extensionMap = constructExtensionMap();

    /**
     * Key header name to be set when serving a blob.
     * 
     * When serving a blob, blobstore service sets a header with "BlobKey = <value>" and then App
     * Engine replaces the body of the response with the content of the blob. For technical reasons,
     * when using the AtomPub API, this setting need to be done outside of the repository service,
     * using the additional headers field of its result.
     * 
     * NOTE(tal): this header name MUST match the header name used by blobstore.
     */
    public static final String BLOB_KEY_HEADER = "BlobKey";

    /**
     * Content type header name to be set when serving a blob.
     */
    public static final String BLOB_CONTENT_TYPE_HEADER = "Content-Type";

    /**
     * Content disposition header name to be set when serving a blob.
     */
    public static final String BLOB_CONTENT_DISPOSITION_HEADER = "Content-Disposition";

    /**
     * Create required additional headers to return when serving a blob.
     */
    public static ImmutableMap<String, String> additionalHeaders(BlobKey blobKey,
            String contentType, String fileName) {
        final String contentDisposition = String.format("filename=\"%s\"", fileName);
        return ImmutableMap.of(BLOB_KEY_HEADER, blobKey.toString(), BLOB_CONTENT_TYPE_HEADER,
                contentType, BLOB_CONTENT_DISPOSITION_HEADER, contentDisposition);
    }

    /**
     * Construct file extension to content type map. Called once during initialization. Provides
     * mapping from lower case non empty file extension to content type.
     */
    private static ImmutableMap<String, String> constructExtensionMap() {
        // TODO(tal): read this from a configuration data file instead.
        //
        // NOTE(tal): order does not matter but we order keys alpahbetically for readability.
        // All extension keys should start with '.' and be all lower case.
        Builder<String, String> builder = ImmutableMap.builder();
        builder.put(".cdf", "application/vnd.wolfram.cdf.text");
        builder.put(".dwg", "image/x-dwg");
        builder.put(".eps", "application/postscript");
        builder.put(".jpg", "image/jpeg");
        builder.put(".llb", "application/octet-stream");
        builder.put(".nbp", "application/mathematica");
        builder.put(".png", "image/png");
        builder.put(".ps", "application/postscript");
        builder.put(".tex", "application/x-tex");
        builder.put(".vi", "application/x-labview-vi");
        builder.put(".viinfo", "application/x-labview-vi");
        builder.put(".zip", "application/zip");
        return builder.build();
    }

    /**
     * Guess the actual content type of an incoming blob. We use heuristic to provide a better (?)
     * answer than the bloblstore service. Logic may change from time to time though it will affect
     * only new blobs since we persist the results of incoming resource and export blobs.
     * <p>
     * The current algorithm tries to match the blob file name extension to a known list of
     * extensions. If found, returning the predefined content type for that extension. Otherwise
     * returning the content type from the blob info.
     * 
     * @param the blobInfo of the blob in question.
     * 
     * @return the content type to use when serving this blob.
     */
    public static String guessBlobContentType(BlobInfo blobInfo) {
        final String lowerCaseFileName = blobInfo.getFilename();
        final int extensionStartIndex = lowerCaseFileName.lastIndexOf('.');
        if (extensionStartIndex < 0) {
            return blobInfo.getContentType();
        }
        final String lowerCaseExtension = lowerCaseFileName.substring(extensionStartIndex);
        @Nullable
        final String mappedContentType = extensionMap.get(lowerCaseExtension);
        return (mappedContentType != null) ? mappedContentType : blobInfo.getContentType();
    }
}
