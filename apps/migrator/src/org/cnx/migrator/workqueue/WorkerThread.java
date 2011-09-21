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
package org.cnx.migrator.workqueue;

import static com.google.common.base.Preconditions.checkNotNull;

import org.cnx.migrator.util.Log;

/**
 * A migrator worker thread. The thread accepts jobs from the work queue and execute them.
 * 
 * @author tal
 */
public class WorkerThread extends Thread {

    /** For debugging only */
    private final String workerId;

    /** The work queue that this worker serves. */
    private final WorkQueue workQueue;

    /**
     * @param workerId an identifier of this worker. For debugging only.
     * @param workQueue the work queue from which this thread should process work items.
     */
    public WorkerThread(String workerId, WorkQueue workQueue) {
        this.workerId = checkNotNull(workerId);
        this.workQueue = checkNotNull(workQueue);

        // Not main thread.
        setDaemon(true);
    }

    @Override
    public void run() {
        try {
            for (;;) {
                WorkItem item = workQueue.getNextItemToWork();
                item.doWork();
                workQueue.itemCompleted(item);
            }
        } catch (Throwable e) {
            Log.printStackTrace(e);
            Log.message("Worker thread %s encountered an error, aborting.", workerId);
            System.exit(-1);
        }
    }
}
