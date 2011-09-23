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
package org.cnx.migrator.io;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.File;

import com.google.common.collect.ImmutableList;

/**
 * An abstraction of a CNX data directory that contains shard subdirectories.
 * 
 * @author tal
 */
public class ShardedDirectory extends Directory {

    // TODO(tal): increase to 1000 or make it a command line flag.
    public static final int SHARD_COUNT = 1000;

    public ShardedDirectory(File dir) {
        super(dir);
    }

    public ImmutableList<Directory> getShards(int minShard, int maxShard) {
        final int n = getSubDirectories().size();
        checkArgument(n == SHARD_COUNT,
                "Expecting %s shards, found %s entires in sharded directory: %s", SHARD_COUNT, n,
                toString());
        ImmutableList.Builder<Directory> builder = new ImmutableList.Builder<Directory>();
        for (int i = minShard; i <= maxShard; i++) {
            final String name = String.format("%03d", i);
            builder.add(new Directory(new File(getUnderlyingDirectory(), name)));
        }
        return builder.build();
    }
}
