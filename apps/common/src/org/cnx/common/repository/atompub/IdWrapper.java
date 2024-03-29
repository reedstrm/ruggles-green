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

import static com.google.common.base.Preconditions.checkArgument;

import org.cnx.common.repository.RepositoryConstants;

import org.cnx.common.exceptions.CnxInvalidUrlException;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This will wrap Ids for Collections/Modules/Resources. TODO(arjuns) : move this to upper package.
 * 
 * @author Arjun Satyapal
 */
public class IdWrapper {
    /**
     * Regex for the post prefix portion of the id. We match exactly strings that can be formatted
     * by "%04d" with numeric value >= 1.
     * 
     * It pass 4 digits numbers with at least one non zero digit and 5 or more digit numbers with
     * non zero first digit.
     */
    private static final String SUB_ID_REGEX = "(^[1-9][\\d]{3,20})|" +
            "(^[0]{3}[1-9])|" + "(^[0]{2}[1-9][\\d])|" + "([0][1-9][\\d]{2})";

    private static final Pattern SUB_ID_PATTERN = Pattern.compile(SUB_ID_REGEX);
    
    
    // TODO(arjuns) : Share this with Repository.
    final static String COLLECTION_ID_PREFIX = "col";
    final static String MODULE_ID_PREFIX = "m";
    final static String RESOURCE_ID_PREFIX = "r";

    private Type type;
    private final String id;

    /**
     * Here Id string can be prefixed with 0.
     *
     * This method is only for CnxAtomPubClient to obtain IdWrapper from generic Id.
     * All other should use Constructor and pass expected type.
     * 
     * @param id ids used in URLs. e.g. m0001 etc.
     */
    static IdWrapper getIdWrapper(String id) {
        if (id.startsWith(COLLECTION_ID_PREFIX)) {
            return new IdWrapper(id, Type.COLLECTION);
        } else if (id.startsWith(MODULE_ID_PREFIX)) {
            return new IdWrapper(id, Type.MODULE);
        } else if (id.startsWith(RESOURCE_ID_PREFIX)) {
            return new IdWrapper(id, Type.RESOURCE);
        }

        throwInvalidIdExecption(id, null /* type */, null/* throwable */);

        return null;
    }

    /**
     * Constructor for IdWrapper.
     * 
     * TODO(arjuns) : Add regex checks.
     * 
     * @param id String format for module/collection Ids.
     * @param idType Type of Id.
     */
    public IdWrapper(String id, Type idType) {
        try {
            switch (idType) {
                case MODULE:
                    checkArgument(id.startsWith(MODULE_ID_PREFIX));
                    validateId(id.substring(MODULE_ID_PREFIX.length()), Type.MODULE);
                    break;

                case COLLECTION:
                    checkArgument(id.startsWith(COLLECTION_ID_PREFIX));
                    validateId(id.substring(COLLECTION_ID_PREFIX.length()), Type.COLLECTION);
                    break;

                case RESOURCE:
                    checkArgument(id.startsWith(RESOURCE_ID_PREFIX));
                    validateId(id.substring(RESOURCE_ID_PREFIX.length()), Type.RESOURCE);
                    break;

                default:
                    throw new CnxInvalidUrlException("Illegal IdType[" + idType + "].", null /* throwable */);
            }
        } catch (Exception e) {
            throwInvalidIdExecption(id, idType, e);
        }

        this.type = idType;
        this.id = id;
    }

    /**
     * Validate id for a module.
     * 
     * @param idWithoutPrefix ModuleId to be validated.
     */
    static void validateId(String idWithoutPrefix, Type type) {
        checkArgument(idWithoutPrefix.length() >= 4);

        try {
            long longId = Long.parseLong(idWithoutPrefix);
            checkArgument(longId > 0);
        } catch (NumberFormatException e) {
            throwInvalidIdExecption(idWithoutPrefix, type, null /* throwable */);
        }

        Matcher matcher = SUB_ID_PATTERN.matcher(idWithoutPrefix);
        if (!matcher.matches()) {
            throwInvalidIdExecption(idWithoutPrefix, type, null /* throwable */);
        }
    }

    private static void throwInvalidIdExecption(String id, Type type, Throwable throwable)
            throws CnxInvalidUrlException {
        throw new CnxInvalidUrlException("Illegal Id[" + id + "],  idType[" + type + "].",
                throwable);
    }

    /**
     * Here Ids are returned that are used in URLs.
     * 
     * @return Ids used over URLs.
     */
    public String getId() {
        return id;
    }

    public Type getType() {
        return type;
    }
    
    /**
     * Returns true  if Id is under forced range.
     */
    public boolean isIdUnderForcedRange() {
        Long idLong = Long.parseLong(id.substring(type.getPrefix().length()));
        
        if (idLong < RepositoryConstants.MIN_NON_RESERVED_KEY_ID) {
            return true;
        }
        
        return false;
    }

    @Override
    public String toString() {
        return id;
    }

    /**
     * Currently this is not supported deliberately. In future if required, this can be supported.
     */
    @Override
    public boolean equals(Object object) {
        if (object instanceof IdWrapper) {
            IdWrapper that = (IdWrapper) object;
            return (id.equals(that.id) && type == that.type);
        }
        
        return false;
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
