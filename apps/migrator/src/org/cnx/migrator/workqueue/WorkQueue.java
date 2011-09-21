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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Set;

import javax.annotation.Nullable;

/**
 * A general work queue. Not CNX migrator specific.
 * 
 * @author tal
 */
public class WorkQueue {
    private final int maxPendingItems;

    /** Queued items to be processes */
    private final LinkedList<WorkItem> pendingItems;

    /** Items currently processed. */
    private final Set<WorkItem> inProgressItems;

    /** Items completed. */
    private int completedItemCount = 0;

    /** Using private lock to avoid external locks on this instance */
    private final Object lock = new Object();

    /**
     * Optional. If not available, all threads are enabled. Otherwise, active thread count is ramped
     * up.
     */
    @Nullable
    private TimeRamp timeRamp = null;

    /** Number of thread created in this queue */
    private final int workersCount;

    /**
     * @param maxPendingItems max number of pending items waiting to be processed.
     */
    public WorkQueue(int maxPendingItems, int workersCount) {

        this.maxPendingItems = maxPendingItems;
        this.workersCount = workersCount;
        this.pendingItems = new LinkedList<WorkItem>();
        this.inProgressItems = new LinkedHashSet<WorkItem>();

        checkArgument(maxPendingItems > 0, "Invalid maxPendingItems: %d", maxPendingItems);
        checkArgument(workersCount > 0, "Invalid workerCount: %d", maxPendingItems);

        // Initialize workers
        for (int i = 0; i < workersCount; i++) {
            new WorkerThread(String.format("Worker %03d", i), this).start();
        }
    }

    /**
     * Get total number of worker threads created for this queue. Not affected by ramp up.
     */
    public int getWorkersCount() {
        // final member, no need to synchronize.
        return workersCount;
    }

    /**
     * Non blocking queuing of a new work item to be processed.
     * 
     * If item was added, queue takes ownership of it and caller should not mutate or access it.
     * 
     * @param item item to add.
     * @return true if added, false if work queue is full.
     */
    public boolean tryToAddItem(WorkItem item) {
        synchronized (lock) {
            if (pendingItems.size() >= maxPendingItems) {
                return false;
            }
            pendingItems.addLast(item);
            if (mayNeedToWakeUpOneMoreThread()) {
                lock.notify();
            }
        }
        return true;
    }

    /**
     * A blocking call to get the next item to process.
     * 
     * @return
     */
    public WorkItem getNextItemToWork() {
        synchronized (lock) {
            while (!mayNeedToWakeUpOneMoreThread()) {
                waitOnLock();
            }
            final WorkItem item = checkNotNull(pendingItems.removeFirst());
            inProgressItems.add(item);

            // If needed, wake one more thread (e.g. for ramp up)
            if (mayNeedToWakeUpOneMoreThread()) {
                lock.notify();
            }
            return item;
        }
    }

    /**
     * Test if need to wake up at least one more thread. Must be called within a syncrhonized
     * section.
     */
    private boolean mayNeedToWakeUpOneMoreThread() {
        // If no pending items no need to wake.
        if (pendingItems.isEmpty()) {
            return false;
        }
        // There is at least one pending item. Test if the ramp up and thread count constrains
        // allow to wake up one more thread.
        final int currentActiveCount = (timeRamp == null) ? workersCount : timeRamp.getValue();
        return currentActiveCount > inProgressItems.size();
    }

    /**
     * Given item was completed.
     * 
     * @param item and item that was retrieved earlier using getNextItem().
     */
    public void itemCompleted(WorkItem item) {
        checkNotNull(item);
        synchronized (lock) {
            checkArgument(inProgressItems.remove(item), "Item was not in inProgress set");
            completedItemCount++;

            // NOTE(tal): we don't notify here to allow thread ramp since this thread will
            // soon call getNextItemWork().
        }
    }

    public WorkQueueStats getStats() {
        synchronized (lock) {
            // Let one thread to wake up in case of ramp up.
            lock.notify();
            return new WorkQueueStats(pendingItems.size(), inProgressItems.size(),
                    completedItemCount);
        }
    }

    /**
     * Prepare for a new work session. Resets completed item count and allow to setup a new ramp up
     * function.
     * 
     * Can be called only when the work queue is idle (no pending or in progress items)
     */
    public void reset(@Nullable TimeRamp timeRamp) {
        synchronized (lock) {
            checkArgument(pendingItems.size() == 0, "Cannot reset while there are pending items");
            checkArgument(inProgressItems.size() == 0, "Cannot reset while there items in progress");

            // TODO(tal): add check here that max ramp value is <= number of worker threads.

            this.timeRamp = timeRamp;
            completedItemCount = 0;
        }
    }

    /** Internal method for exception-less wait() */
    private void waitOnLock() {
        try {
            lock.wait();
        } catch (InterruptedException e) {

            throw new RuntimeException(e);
        }
    }
}
