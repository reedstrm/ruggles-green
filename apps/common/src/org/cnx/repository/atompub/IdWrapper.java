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

import static com.google.common.base.Preconditions.checkArgument;

import org.cnx.exceptions.CnxInvalidUrlException;
import org.cnx.repository.RepositoryConstants;

/**
 * This will wrap Ids for Collections/Modules/Resources. TODO(arjuns) : move this to upper package.
 * 
 * @author Arjun Satyapal
 */
public class IdWrapper {
    // TODO(arjuns) : Share this with Repository.
    private final static int PREFIX_LENGTH = 1;
    private final static String COLLECTION_ID_PREFIX = "c";
    private final static String MODULE_ID_PREFIX = "m";
    private final static String RESOURCE_ID_PREFIX = "r";

    private Type type;
    private final Integer idInt;
    private final String idUrl;

    /**
     * Here Id string can be prefixed with 0.
     * 
     * @param idString ids used in URLs. e.g. m0001 etc.
     */
    public static IdWrapper getIdWrapperFromUrlId(String idString) {
        if (idString.startsWith(COLLECTION_ID_PREFIX)) {
            return new IdWrapper(idString, Type.COLLECTION);
        } else if (idString.startsWith(MODULE_ID_PREFIX)) {
            return new IdWrapper(idString, Type.MODULE);
        } else if (idString.startsWith(RESOURCE_ID_PREFIX)) {
            return new IdWrapper(idString, Type.RESOURCE);
        }

        throw new CnxInvalidUrlException("Invalid Id : " + idString, null /*throwable*/);
    }

    private static String getIdWithPadding(int id) {
        return String.format("%04d", id);
    }

    /**
     * Here values are not prefixed with 0.
     * 
     * @param repoIdString Ids returned by CNX repository. These will be converted to URL friendly
     *            Ids with 0 padded to have min length of 4.
     */
    public static IdWrapper getIdWrapperFromRepositoryId(String repoIdString) {
        Type idType = Type.getTypeFromPrefix(repoIdString.substring(0, PREFIX_LENGTH));
        String intPart = repoIdString.substring(PREFIX_LENGTH);
        StringBuilder builder = new StringBuilder(idType.getPrefix());
        builder.append(getIdWithPadding(Integer.parseInt(intPart)));

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
    public IdWrapper(String idString, Type idType) {
        switch (idType) {
            case MODULE:
                checkArgument(idString.startsWith(MODULE_ID_PREFIX));
                break;

            case COLLECTION:
                checkArgument(idString.startsWith(COLLECTION_ID_PREFIX));
                break;

            case RESOURCE:
                checkArgument(idString.startsWith(RESOURCE_ID_PREFIX));
                break;

            default:
                throw new CnxInvalidUrlException("Illegal IdType[" + idType + "].", null /* throwable */);
        }

        this.type = idType;
        this.idUrl = idString;

        String integerPart = idString.substring(1);
        
        if (integerPart.length() < 4) {
            throw new CnxInvalidUrlException("Invalid Id[" + idString + "]", null /*throwable*/);
        }

        try {
            idInt = Integer.parseInt(integerPart);
        } catch (NumberFormatException e) {
            throw new CnxInvalidUrlException("Invalid Id : " + idString, e);
        }
    }

    /**
     * Get ID that is understood by CNX repository service. Here Ids don't have padding.
     * 
     * @return Id understood by CNX Repository service.
     */
    public String getIdForRepository() {
        switch (type) {
            case COLLECTION:
                return COLLECTION_ID_PREFIX + idInt;
            case MODULE:
                return MODULE_ID_PREFIX + idInt;
            case RESOURCE:
                return RESOURCE_ID_PREFIX + idInt;

        }

        throw new CnxInvalidUrlException("Invalid IdType[" + type + "].", null /* throwable */);
    }

    /**
     * Here Ids are returned that are used in URLs.
     * 
     * @return Ids used over URLs.
     */
    public String getIdForUrls() {
        return idUrl;
    }
    
    /**
     * Returns possible Id in cnx.org format.
     */
    public String getIdForCnxOrg() {
        switch (type) {
            case MODULE:
                return idUrl;
                
            case COLLECTION:
                return "col" + getIdWithPadding(idInt);
        }
        
        throw new RuntimeException("Code should not reach here.");
    }

    public Type getType() {
        return type;
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
     * Checks if Id belongs to ForcedId range.
     */
    public boolean isIdUnderForcedRange() {
        return idInt > 0 && idInt < RepositoryConstants.MIN_NON_RESERVED_KEY_ID;
    }

    /**
     * Types of Ids supported.
     */
    public static enum Type {
        COLLECTION(COLLECTION_ID_PREFIX),
        MODULE(MODULE_ID_PREFIX),
        RESOURCE(RESOURCE_ID_PREFIX);

        private String prefix;

        private Type(String prefix) {
            this.prefix = prefix;
        }

        public String getPrefix() {
            return prefix;
        }

        public static Type getTypeFromPrefix(String prefix) {
            for (Type currType : Type.values()) {
                if (currType.getPrefix().equals(prefix)) {
                    return currType;
                }
            }

            throw new CnxInvalidUrlException("Invalid prefix : " + prefix, null /* throwable */);
        }
    }
}
