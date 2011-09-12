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
package org.cnx.migrator.queue;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Set;

/**
 * A general work queue. Not migrator specific.
 * 
 * @author tal
 */
public class WorkQueue<T> {
    private final int maxPendingItems;

    /** Queued items to be processes */
    private final LinkedList<T> pendingItems;

    /** Items currently processed. */
    private final Set<T> inProgressItems;

    /** Items completed. */
    private int completedItemCount = 0;

    /** Using private lock to avoid external locks on this instance */
    private final Object lock = new Object();

    /**
     * @param maxPendingItems max number of pending items waiting to be processed.
     */
    public WorkQueue(int maxPendingItems) {
        checkArgument(maxPendingItems > 0, "Invalid maxPendingItems: %d", maxPendingItems);
        this.maxPendingItems = maxPendingItems;
        this.pendingItems = new LinkedList<T>();
        this.inProgressItems = new LinkedHashSet<T>();
    }

    /**
     * Non blocking addition of a new work item.
     * 
     * If item was added, queue takes ownership of it and caller should not mutate or access it.
     * 
     * @param item item to add.
     * @return true if added, false if work queue is full.
     */
    public boolean tryToAddItem(T item) {
        synchronized (lock) {
            if (pendingItems.size() >= maxPendingItems) {
                return false;
            }
            pendingItems.addLast(item);
            lock.notify();
        }
        return true;
    }

    /**
     * A blocking call to get the next item to process.
     * 
     * @return
     */
    public T getNextItemToWork() {
        synchronized (lock) {
            while (pendingItems.isEmpty()) {
                waitOnLock();
            }
            final T item = checkNotNull(pendingItems.removeFirst());
            inProgressItems.add(item);
            return item;
        }
    }

    /**
     * Given item was completed.
     * 
     * @param item and item that was retrieved earlier using getNextItem().
     */
    public void itemCompleted(T item) {
        checkNotNull(item);
        synchronized (lock) {
            checkArgument(inProgressItems.remove(item), "Item was not in inProgress set");
            completedItemCount++;
        }
    }

    public WorkQueueStats getStats() {
        synchronized (lock) {
            return new WorkQueueStats(pendingItems.size(), inProgressItems.size(),
                    completedItemCount);
        }
    }

    /**
     * Reset completed item counter.
     */
    public void resetCompletedItemCount() {
        synchronized (lock) {
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
