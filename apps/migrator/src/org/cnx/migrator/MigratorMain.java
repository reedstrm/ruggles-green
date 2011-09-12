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
package org.cnx.migrator;

/**
 * Thin command line wrapper for the migrator.
 * 
 * @author tal
 */
public class MigratorMain {
    public static void main(String args[]) {
        // TODO(tal): initialize the configuration from the command line args
        final MigratorConfiguration config = new MigratorConfiguration(args);
        final Migrator migrator = new Migrator(config);
        migrator.migrateAll();
        System.out.println("\nAll done OK");
    }
}
