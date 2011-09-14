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

import org.cnx.repository.RepositoryConstants;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.cnx.exceptions.CnxInvalidUrlException;

/**
 * This will wrap Ids for Collections/Modules/Resources. TODO(arjuns) : move this to upper package.
 * 
 * @author Arjun Satyapal
 */
public class IdWrapper {
    // TODO(arjuns) : Share this with Repository.
    final static String COLLECTION_ID_PREFIX = "col";
    final static String MODULE_ID_PREFIX = "m";
    final static String RESOURCE_ID_PREFIX = "r";

    private Type type;
    private final String id;

    /**
     * Here Id string can be prefixed with 0.
     * 
     * @param id ids used in URLs. e.g. m0001 etc.
     */
    public static IdWrapper getIdWrapper(String id) {
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

        Pattern pattern;
        Matcher matcher;
        if (idWithoutPrefix.startsWith("0")) {
            pattern = Pattern.compile("[0-9]{3}[1-9][\\d]*");
            matcher = pattern.matcher(idWithoutPrefix);
        } else {
            pattern = Pattern.compile("\\d{4,}+");
            matcher = pattern.matcher(idWithoutPrefix);
        }

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
