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
package org.cnx.repository.atompub.jerseyservlets;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.utils.SystemProperty;
import com.googlecode.charts4j.collect.Lists;
import java.util.List;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import org.cnx.common.repository.atompub.IdWrapper;

/**
 * 
 * @author Arjun Satyapal
 */
@Path("delete")
public class CnxDeleteEntity {
    private final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    private final Logger logger = Logger.getLogger(CnxDeleteEntity.class.getName());

    static final String ENTITY_NAME = "entityName";
    static final String ID_STRING = "id";
    static final String DELETE_ENTITY_PATH = "/{" + ENTITY_NAME + "}/{" + ID_STRING + "}";

    @GET
    @Produces("text/html")
    @Path(DELETE_ENTITY_PATH)
    public Response deleteAllEntities(@PathParam(ENTITY_NAME) String entityName,
            @PathParam(ID_STRING) String idString) {
        if (SystemProperty.environment.value() == SystemProperty.Environment.Value.Production) {
            return Response.serverError().entity("Not allowed on Production. Only for unittests.")
                    .build();
        }

        IdWrapper idWrapper =
                new IdWrapper(idString, IdWrapper.Type.valueOf(entityName.toUpperCase()));

        String keyId = null;

        List<Key> keysToDelete = Lists.newArrayList();
        switch (idWrapper.getType()) {
            case COLLECTION:
                keyId = idWrapper.getId().substring(3);
                keysToDelete = getListOfKeysToDelete("Collection", "CollectionVer", keyId);
                break;

            case MODULE:
                keyId = idWrapper.getId().substring(1);
                keysToDelete = getListOfKeysToDelete("Module", "ModuleVer", keyId);
                break;

            case RESOURCE:
                keyId = idWrapper.getId().substring(1);
                keysToDelete = getListOfKeysToDelete("Resource", null, keyId);
                break;
        }

        StringBuilder stringBuilder = new StringBuilder("Deleting : ");
        for (Key currKey : keysToDelete) {
            stringBuilder.append(currKey.toString()).append(", ");
        }
        Transaction tx = datastore.beginTransaction();
        datastore.delete(keysToDelete);
        tx.commit();

        logger.info(stringBuilder.toString());
        return Response.ok().build();
    }

    private List<Key>
            getListOfKeysToDelete(String kind, @Nullable String subKind, String keyId) {
        Key parentKey = KeyFactory.createKey(kind, Long.valueOf(keyId));
        List<Key> keysToDelete = Lists.newArrayList();
        

        if (subKind != null) {
            Query q = new Query(subKind, parentKey).setKeysOnly();
            PreparedQuery pq = datastore.prepare(q);
            for (Entity entity : pq.asIterable()) {
                keysToDelete.add(entity.getKey());
            }
        }
        keysToDelete.add(parentKey);
        return keysToDelete;

    }
}
