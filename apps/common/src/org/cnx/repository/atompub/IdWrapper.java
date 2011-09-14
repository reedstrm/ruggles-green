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

import com.google.common.base.Preconditions;

import org.cnx.exceptions.CnxInvalidUrlException;

/**
 * This will wrap Ids for Collections/Modules/Resources. TODO(arjuns) : move this to upper package.
 * 
 * @author Arjun Satyapal
 */
public class IdWrapper {
    // TODO(arjuns) : Share this with Repository.
    private final static String COLLECTION_ID_PREFIX = "col";
    private final static String MODULE_ID_PREFIX = "m";
    private final static String RESOURCE_ID_PREFIX = "r";

    private Type type;
//    private final Integer idInt;
    private final String id;

    /**
     * Here Id string can be prefixed with 0.
     * 
     * @param idString ids used in URLs. e.g. m0001 etc.
     */
    public static IdWrapper getIdWrapper(String idString) {
        if (idString.startsWith(COLLECTION_ID_PREFIX)) {
            return new IdWrapper(idString, Type.COLLECTION);
        } else if (idString.startsWith(MODULE_ID_PREFIX)) {
            return new IdWrapper(idString, Type.MODULE);
        } else if (idString.startsWith(RESOURCE_ID_PREFIX)) {
            return new IdWrapper(idString, Type.RESOURCE);
        }

        throw new CnxInvalidUrlException("Invalid Id : " + idString, null /*throwable*/);
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
        this.id = idString;
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
