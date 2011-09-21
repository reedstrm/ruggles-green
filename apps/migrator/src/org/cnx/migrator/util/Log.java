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
package org.cnx.migrator.util;

/** Provides synchronized diagnostic message printing from multiple threads */
public class Log {

    private static final Object lock = new Object();

    public static void message(String format, Object... args) {
        synchronized (lock) {
            System.out.println(String.format(format, args));
        }
    }

    public static void printStackTrace(Throwable e) {
        synchronized (lock) {
            e.printStackTrace(System.out);
        }
    }
}
