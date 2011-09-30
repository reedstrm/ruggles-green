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
package org.cnx.common.repository.atompub.objects;

import com.google.common.collect.Maps;
import com.sun.syndication.feed.atom.Content;
import com.sun.syndication.feed.atom.Entry;
import java.net.URISyntaxException;
import java.util.Map;
import org.cnx.common.repository.atompub.CnxAtomPubLinkRelations;
import org.cnx.common.repository.atompub.CnxAtomPubUtils;
import org.cnx.common.repository.atompub.IdWrapper;
import org.cnx.common.repository.atompub.VersionWrapper;

/**
 * Wrapper object for CNX Resource.
 * @author Arjun Satyapal
 */
public class ResourceInfoWrapper extends AtomPubResource {
    public static final String MD5HASH = "md5hash";
    public static final String CONTENT_SIZE = "content-size";
    
    private String fileName;
    private String fileContentType;
    private Long contentSize;
    private String md5hash;
    
    public ResourceInfoWrapper(IdWrapper id, VersionWrapper version) {
        super(id, version);
    }
    
    @Override
    protected StringBuilder getStringBuilder() {
        return super.getStringBuilder()
                .append(", fileName:").append(fileName)
                .append(", fileContentType:").append(fileContentType)
                .append(", size:").append(contentSize)
                .append(", md5hash:").append(md5hash);
    }
    
    @Override
    public String toString() {
        return this.getStringBuilder().toString();
    }

    public static ResourceInfoWrapper fromEntry(Entry entry) throws URISyntaxException {
        IdWrapper id = CnxAtomPubUtils.getIdFromAtomPubId(entry.getId());
        ResourceInfoWrapper resourceInfo = new ResourceInfoWrapper(id, null /*version*/);
        
        resourceInfo.setSelfUri(CnxAtomPubLinkRelations.getSelfUri(entry));
        resourceInfo.setPublished(entry.getPublished());
        
        Content titleex = entry.getTitleEx();
        resourceInfo.setFileName(titleex.getValue());
        resourceInfo.setFileContentType(titleex.getType());

        String[] keyValue = entry.getSummary().getValue().split("\\n");
        Map<String, String> map = Maps.newConcurrentMap();
        for (String currKeyValue : keyValue) {
            String modifiedString = currKeyValue.replace("}", "").replace("{", "");
            
            String[] pair = modifiedString.split("=");
            map.put(pair[0], pair[1]);
        }
        
        resourceInfo.setMd5hash(map.get(MD5HASH));
        resourceInfo.setContentSize(Long.parseLong(map.get(CONTENT_SIZE)));
        
        return resourceInfo;
    }

    public String getFileName() {
        return fileName;
    }

    protected void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileContentType() {
        return fileContentType;
    }

    protected void setFileContentType(String fileContentType) {
        this.fileContentType = fileContentType;
    }

    public Long getContentSize() {
        return contentSize;
    }

    protected void setContentSize(Long contentSize) {
        this.contentSize = contentSize;
    }

    public String getMd5hash() {
        return md5hash;
    }

    protected void setMd5hash(String md5hash) {
        this.md5hash = md5hash;
    }
}
