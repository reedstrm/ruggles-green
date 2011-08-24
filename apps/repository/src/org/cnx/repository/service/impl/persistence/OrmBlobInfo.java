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
package org.cnx.repository.service.impl.persistence;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Date;

import com.google.appengine.api.blobstore.BlobInfo;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.datastore.Entity;

/**
 * Container for blob info. Embedded in ORM entities that refer to a blob. This class represent a
 * list of related properties, not a stand alone entity.
 * 
 * @author Tal Dayan
 */
public class OrmBlobInfo {

    private static final String BLOB_KEY_PROPERTY = "blob_key";
    private static final String BLOB_CONTENT_TYPE_PROPERTY = "blob_content_type";
    private static final String BLOB_SIZE_PROPERTY = "blob_size";
    private static final String BLOB_MD5_HASH_PROPERTY = "blob_md5";
    private static final String BLOB_CREATION_PROPERTY = "blob_creation";
    private static final String BLOB_FILE_NAME_PROPERTY = "blob_filename";

    private final BlobKey blobKey;

    /** A cached value of the blob content type. Copied from BlobInfo. */
    private final String contentType;

    /** A cached value of the blob size in bytes. Copied from BlobInfo. */
    private final long size;

    /** A cached value of the blob MD5 hash. Copied from BlobInfo. */
    private final String md5Hash;

    /** A cached value of the blob creation time. Copied from BlobInfo. */
    private final Date creationTime;

    /** A cached value of blob upload file name. Copied from BlobInfo. */
    private final String fileName;

    /** Construct from individual properties. */
    private OrmBlobInfo(BlobKey blobKey, String contentType, long size, String md5Hash,
        Date creationTime, String fileName) {
        this.blobKey = checkNotNull(blobKey);
        this.contentType = checkNotNull(contentType);
        this.size = size;
        this.md5Hash = checkNotNull(md5Hash);
        this.creationTime = checkNotNull(creationTime);
        this.fileName = checkNotNull(fileName);
    }

    /** Construct from blobstore blob info. */
    public OrmBlobInfo(BlobInfo blobInfo) {
        this(blobInfo.getBlobKey(), blobInfo.getContentType(), blobInfo.getSize(), blobInfo
            .getMd5Hash(), blobInfo.getCreation(), blobInfo.getFilename());
    }

    /** Deserialize from a datastore entity. */
    public OrmBlobInfo(Entity entity) {
        this((BlobKey) entity.getProperty(BLOB_KEY_PROPERTY), (String) entity
            .getProperty(BLOB_CONTENT_TYPE_PROPERTY),
            (Long) entity.getProperty(BLOB_SIZE_PROPERTY), (String) entity
                .getProperty(BLOB_MD5_HASH_PROPERTY), (Date) entity
                .getProperty(BLOB_CREATION_PROPERTY), (String) entity
                .getProperty(BLOB_FILE_NAME_PROPERTY));
    }

    /**
     * Serialize to given entity of enclosing ORM entity.
     */
    protected void serializeToEntity(Entity entity) {
        entity.setProperty(BLOB_KEY_PROPERTY, blobKey);
        entity.setProperty(BLOB_CONTENT_TYPE_PROPERTY, contentType);
        entity.setProperty(BLOB_SIZE_PROPERTY, size);
        entity.setProperty(BLOB_MD5_HASH_PROPERTY, md5Hash);

        entity.setProperty(BLOB_CREATION_PROPERTY, creationTime);
        entity.setProperty(BLOB_FILE_NAME_PROPERTY, fileName);
    }

    public BlobKey getBlobKey() {
        return blobKey;
    }

    public String getContentType() {
        return contentType;
    }

    public long getSize() {
        return size;
    }

    public String getMd5Hash() {
        return md5Hash;
    }

    public Date getCreationTime() {
        return creationTime;
    }

    public String getFileName() {
        return fileName;
    }
}
