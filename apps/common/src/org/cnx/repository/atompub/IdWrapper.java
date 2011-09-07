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
package org.cnx.repository.atompub;

import com.google.common.base.Preconditions;

import org.cnx.exceptions.CnxRuntimeException;

import javax.ws.rs.core.Response.Status;

/**
 * This will wrap Ids for Collections/Modules/Resources.
 * 
 * @author Arjun Satyapal
 */
public class IdWrapper {
    // TODO(arjuns) : Share this with Repository.
    private final static int PREFIX_LENGTH = 1;
    private final static String COLLECTION_ID_PREFIX = "c";
    private final static String MODULE_ID_PREFIX = "m";
    private final static String RESOURCE_ID_PREFIX = "r";
    private final static String PADDING = "0";
    private final static int MIN_ID_LEN = 4;

    private final String idUrl;
    private final int idInt;
    private IdType idType;

    
    /**
     * Here Id string can be prefixed with 0.
     * 
     * @param idString ids used in URLs. e.g. m0001 etc.
     */
    public static IdWrapper getIdWrapperFromUrlId(String idString) {
        if (idString.startsWith(COLLECTION_ID_PREFIX)) {
            return new IdWrapper(idString, IdType.COLLECTION);
        } else if (idString.startsWith(MODULE_ID_PREFIX)) {
            return new IdWrapper(idString, IdType.MODULE);
        } else if (idString.startsWith(RESOURCE_ID_PREFIX)) {
            return new IdWrapper(idString, IdType.RESOURCE);
        }
        
        throw new RuntimeException("Invalid Id : " + idString);
    }
    
    /**
     * Here values are not prefixed with 0.
     *
     * @param repoIdString Ids returned by CNX repository. These will be converted to 
     *      URL friendly Ids with {@link #PADDING} prefix if required to meet {@link #MIN_ID_LEN}
     */
    public static IdWrapper getIdWrapperFromRepositoryId(String repoIdString) {
        IdType idType = IdType.getIdTypeFromPrefix(repoIdString.substring(0, PREFIX_LENGTH));
        String intPart = repoIdString.substring(PREFIX_LENGTH);
        int requiredNumberOfPaddings = MIN_ID_LEN - intPart.length();
        StringBuilder builder = new StringBuilder(idType.getPrefix());
        
        for (int i = 0; i < requiredNumberOfPaddings; i++) {
            builder.append(PADDING);
        }
        builder.append(intPart);
        
        
        return new IdWrapper(builder.toString(), idType);
    }
    
    /**
     * Constructor for IdWrapper.
     * 
     * TODO(arjuns) : Add regex checks.
     * 
     * @param idString String format for module/collection Ids.
     * @param idType Type of Id.
     */
    public IdWrapper(String idString, IdType idType) {
        switch (idType) {
            case MODULE:
                Preconditions.checkArgument(idString.startsWith(MODULE_ID_PREFIX));
                break;

            case COLLECTION:
                Preconditions.checkArgument(idString.startsWith(COLLECTION_ID_PREFIX));
                break;
                
            case RESOURCE:
                Preconditions.checkArgument(idString.startsWith(RESOURCE_ID_PREFIX));
                break;

            default:
                throw new CnxRuntimeException(Status.BAD_REQUEST,
                        "Illegal IdType[" + idType + "].", null /* throwable */);
        }
        
        this.idType = idType;
        this.idUrl = idString;
        
        String integerPart = idString.substring(1);
        if (integerPart.startsWith(PADDING)) {
            Preconditions.checkArgument(integerPart.length() == 4);
        }

        
        idInt = Integer.parseInt(integerPart);
    }

    /**
     * Get ID that is understood by CNX repository service. 
     * Here ids dont have padding.
     * 
     * @return Id understood by CNX Repository service.
     */
    public String getIdForRepository() {
        switch (idType) {
            case COLLECTION : return COLLECTION_ID_PREFIX + idInt;
            case MODULE: return MODULE_ID_PREFIX + idInt;
            case RESOURCE: return RESOURCE_ID_PREFIX + idInt;
            
        }
        throw new CnxRuntimeException(Status.BAD_REQUEST,
                "Illegal IdType[" + idType + "].", null /* throwable */);
    }
    
    /**
     * Here Ids are returned that are used in URLs. Here if int part of id is < 1000, then 
     * padding is done.
     * 
     * @return Ids used over URLs.
     */
    public String getIdForUrls() {
        return idUrl;
    }
    
    /**
     * This should not be called as toString is ambiguous for this class.
     */
    @Override
    public String toString() {
        throw new RuntimeException("This should not be called.");
    }
    
    /**
     * Currently this is not supported deliberately. In future if required, this can be supported.
     */
    @Override
    public boolean equals(Object that) {
      throw new RuntimeException("This should not be called.");
    }
    
    /**
     * Types of Ids supported.
     */
    public static enum IdType {
        COLLECTION(COLLECTION_ID_PREFIX),
        MODULE(MODULE_ID_PREFIX),
        RESOURCE(RESOURCE_ID_PREFIX);
        
        private String prefix;
        private IdType(String prefix) {
            this.prefix = prefix;
        }
        
        public String getPrefix() {
            return prefix;
        }
        
        public static IdType getIdTypeFromPrefix(String prefix) {
            for (IdType currIdType : IdType.values()) {
                if (currIdType.getPrefix().equals(prefix)) {
                    return currIdType;
                }
            }
            
            throw new RuntimeException("Invalid prefix : " + prefix);
            
        }
    }
}
