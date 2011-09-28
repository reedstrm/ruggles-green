package org.cnx.repository.facilities.datawipe;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.hadoop.io.NullWritable;
//import org.apache.hadoop.mapreduce.Mapper.Context;
import org.cnx.repository.service.impl.operations.Services;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.repackaged.com.google.common.collect.Lists;
import com.google.appengine.tools.mapreduce.AppEngineMapper;

/**
 * Mapreduce to delete all entities of given kind.
 * <p>
 * Use <host_and_port>/mapreduce/status dashboard to access.
 * 
 * @author Tal
 */
public class BlobstoreWiperMapper extends AppEngineMapper<Key, Entity, NullWritable, NullWritable> {

    /** Delete this number of blobs per delete request */
    private static final int BLOBS_PER_DELETE_REQUEST = 100;

    /** Bufferes the blobk keys to be deleted */
    private final ArrayList<BlobKey> keysToDelete = Lists.newArrayList();

    /** At the end, flush pending keys */
    @Override
    public void taskCleanup(Context context) throws IOException, InterruptedException {
        // Delete any leftover blobs.
        flush(context);
    }

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
            keysToDelete.add(blobKey);
            if (keysToDelete.size() >= BLOBS_PER_DELETE_REQUEST) {
                flush(context);
            }
        } catch (Throwable e) {
            context.getCounter("EXCEPTION_" + e.getClass().getSimpleName(), "count").increment(1);
        }
    }

    private void flush(Context context) {
        if (keysToDelete.isEmpty()) {
            return;
        }

        final int MAX_TRIES = 5;

        int i;
        for (i = 0 ; i < MAX_TRIES; i++) {
            try {
                Services.blobstore.delete(keysToDelete.toArray(new BlobKey[0]));
                context.getCounter("BLOBS_GROUPS_DELETED", "count").increment(1);
                context.getCounter("BLOBS_DELETED", "count").increment(keysToDelete.size());
                break;
            } catch (Throwable e) {
                final String exceptionClass = e.getClass().getSimpleName();
                context.getCounter("BLOBS_GROUPS_TRYS_FAILED_" + exceptionClass, "count").increment(1);
                context.getCounter("BLOBS_TRYS_FAILED_"+ exceptionClass, "count_" ).increment(
                        keysToDelete.size());
            }
        }

        if (i >= MAX_TRIES) {
            context.getCounter("BLOBS_GROUPS_FAILED", "count").increment(1);
            context.getCounter("BLOBS_FAILED", "count").increment(
                    keysToDelete.size());
        }

        keysToDelete.clear();
    }
}
