package org.cnx.repository.facilities.datawipe;

import org.apache.hadoop.io.NullWritable;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.tools.mapreduce.AppEngineMapper;
import com.google.appengine.tools.mapreduce.DatastoreMutationPool;

/**
 * Mapreduce to delete all blobstore blobs.
 * <p>
 * Use <host_and_port>/mapreduce/status dashboard to access.
 * 
 * @author Tal
 */
public class DataWiperMapper extends AppEngineMapper<Key, Entity, NullWritable, NullWritable> {
    @Override
    public void map(Key key, Entity value, Context context) {

        final DatastoreMutationPool mutationPool =
                this.getAppEngineContext(context).getMutationPool();
        mutationPool.delete(value.getKey());
        context.getCounter("DELETED_" + value.getKey().getKind(), "count").increment(1);
    }
}
