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

package org.cnx.repository.service.impl.persistence;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.appengine.api.datastore.Transaction;

/**
 * Represents an ongoing transaction of the persistence service.
 * <p>
 * Implementation note: this class wraps the underlying eppengine transaction. It allows to track
 * the history of the transaction and handle safely commit and rollback operations.
 * 
 * @author tal
 * 
 */
public class PersistenceTransaction {

    private static final Logger log = Logger.getLogger(PersistenceTransaction.class.getName());

    /** Transaction states */
    public static enum State {
        /** Transaction created */
        ACTIVE,
        /** Commit in progress */
        COMMIT_STARTED,
        /** Commit complete successfully. Terminal state. */
        COMMIT_OK,
        /** Commit failed. Terminal state. */
        COMMIT_FAILED,
        /** Rollback started */
        ROLLBACK_STARTED,
        /** Rollback done OK. Terminal state. */
        ROLLBACK_OK,
        /** Rollback failed. Terminal state. */
        ROLLBACK_FAILED;
    }

    private State state;

    /** Underlying datastore transaction */
    private Transaction tx;

    /**
     * Internal constructor.
     * 
     * Returns a transaction in the ACTIVE state.
     * 
     * Not for public use. Transaction should be created via
     * {@link PersistenceService#beginTransaction()}
     */
    public PersistenceTransaction(Transaction tx) {
        this.state = State.ACTIVE;
        this.tx = checkNotNull(tx);
        checkArgument(tx.isActive(), "Transaction is not active");
    }

    /** Get the current state of this transaction */
    public State getState() {
        return state;
    }

    /** Check if transaction is in the ACTIVE state */
    public boolean isActive() {
        return state == State.ACTIVE;
    }

    /**
     * Commit an active transaction.
     * 
     * The method asserts that transaction state is active. If commit is successful, transaction
     * state is changed to COMMIT_OK and method returns normally. Otherwise, transaction state is
     * changed to COMMIT_FAILED and a PersisenceException is thrown.
     */
    public void commit() throws PersistenceException {
        checkState(state == State.ACTIVE, "Unexpected state: %s", state);
        try {
            tx.commit();
            state = State.COMMIT_OK;
        } catch (Throwable e) {
            state = State.COMMIT_FAILED;
            throw new PersistenceException("Exception when performing commit", e);
        }
    }

    /**
     * Roll back an active transaction.
     * 
     * The method asserts that transaction state is active. If roll back is successful, transaction
     * state is changed to ROLLBACK_OK and method returns normally. Otherwise, transaction state is
     * changed to ROLLBACK_FAILED and a PersisenceException is thrown.
     */
    public void rollback() throws PersistenceException {
        checkState(state == State.ACTIVE, "Unexpected state: %s", state);
        try {
            tx.rollback();
            state = State.ROLLBACK_OK;
        } catch (Throwable e) {
            state = State.ROLLBACK_FAILED;
            throw new PersistenceException("Exception when performing rollback", e);
        }
    }

    /**
     * If transaction is active then roll it back.
     * 
     * This method does not throw any exception. If the roll back fails, it logs a severe log
     * message. This method is useful for final cleanup of failed transactions.
     */
    public void safeRollback() {
        if (state == State.ACTIVE) {
            try {
                tx.rollback();
                state = State.ROLLBACK_OK;
            } catch (Throwable e) {
                log.log(Level.SEVERE, "Error rolling back a transaction", e);
                state = State.ROLLBACK_FAILED;
            }
        }
    }
}
