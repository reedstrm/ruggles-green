package org.cnx.repository.facilities.stats;

import org.apache.hadoop.io.NullWritable;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.tools.mapreduce.AppEngineMapper;

/**
 * Mapreduce to count entities of a given kind.
 * <p>
 * Use <host_and_port>/mapreduce/status dashboard to access.
 * 
 * @author Tal
 */
public class EntityCounterMapper extends AppEngineMapper<Key, Entity, NullWritable, NullWritable> {

    @Override
    public void map(Key key, Entity value, Context context) {
        context.getCounter("ENTITY_" + value.getKey().getKind(), "count").increment(1);
    }
}
