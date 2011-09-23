package org.cnx.repository.facilities.datawipe;

import org.apache.hadoop.io.NullWritable;
import org.cnx.repository.service.impl.operations.Services;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.tools.mapreduce.AppEngineMapper;

/**
 * Mapreduce to delete all entities of given kind.
 * <p>
 * Use <host_and_port>/mapreduce/status dashboard to access.
 * 
 * @author Tal
 */
public class BlobstoreWiperMapper extends AppEngineMapper<Key, Entity, NullWritable, NullWritable> {
    @Override
    public void map(Key key, Entity value, Context context) {
        // We expect only blob info entities
        final String entityKind = value.getKey().getKind();
        if (!"__BlobInfo__".equals(entityKind)) {
            context.getCounter("NOT_A_BLOB_INFO_ENTITY_" + entityKind, "count").increment(1);
            return;
        }

        try {
            final BlobKey blobKey = new BlobKey(value.getKey().getName());
            Services.blobstore.delete(blobKey);
            context.getCounter("BLOBS_DELETED", "count").increment(1);
        } catch (Throwable e) {
            context.getCounter("BLOB_DELETIONS_FAILED", "count").increment(1);
        }
    }
}
