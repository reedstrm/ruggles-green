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

import java.io.File;

/** An abstraction of a CNX input data directory that is part of a sharded directory */
public class DirectoryShard extends Directory {
    private final int shardIndex;

    public DirectoryShard(File dir, int shardIndex) {
        super(dir);
        this.shardIndex = shardIndex;
    }

    public int getShardIndex() {
        return shardIndex;
    }
}
