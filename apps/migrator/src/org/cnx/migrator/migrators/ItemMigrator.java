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
package org.cnx.migrator.migrators;

import static com.google.common.base.Preconditions.checkNotNull;

import org.cnx.atompubclient.CnxAtomPubClient;
import org.cnx.migrator.config.MigratorConfiguration;
import org.cnx.migrator.util.Log;
import org.cnx.migrator.workqueue.WorkItem;

/**
 * Base class of migration items migrators.
 * 
 * @author tal
 */
public abstract class ItemMigrator implements WorkItem {

    /** Migrator config in effect */
    private final MigratorConfiguration config;

    /** CNX client to use to write to repository */
    private final CnxAtomPubClient cnxClient;

    protected ItemMigrator(MigratorConfiguration config, CnxAtomPubClient cnxClient) {
        this.config = checkNotNull(config);
        this.cnxClient = checkNotNull(cnxClient);
    }

    protected MigratorConfiguration getConfig() {
        return config;
    }

    protected CnxAtomPubClient getCnxClient() {
        return cnxClient;
    }

    /** Print a diagnostic message */
    protected void message(String format, Object... args) {
        Log.message(format, args);
    }
}
