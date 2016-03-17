package com.cesarparent.netnotes.sync;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.cesarparent.netnotes.CPApplication;
import com.cesarparent.netnotes.R;
import com.cesarparent.netnotes.model.Model;
import com.cesarparent.netnotes.sync.tasks.APILoginOperation;
import com.cesarparent.netnotes.sync.tasks.SyncDeleteOperation;
import com.cesarparent.netnotes.sync.tasks.SyncUpdateOperation;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by cesar on 23/02/2016.
 *
 * Sync provides a simple API to synchronise the local notes database with the server.
 */
public class Sync {

    /**
     * Sync.Status is used to communicate the result of a sync operation.
     */
    public enum Status {
        SUCCESS                 (R.string.status_success),
        FAIL                    (R.string.status_fail),
        FAIL_LOGGED_OUT         (R.string.status_fail_logged_out),
        FAIL_BAD_REQUEST        (R.string.status_fail_bad_request),
        FAIL_UNAUTHORIZED       (R.string.status_fail_unauthorized),
        FAIL_CONFLICT           (R.string.status_fail_conflict),
        FAIL_SERVER_ERROR       (R.string.status_fail_server_error),
        FAIL_NO_NETWORK         (R.string.status_fail_no_network),
        FAIL_CONNECTION_ERROR   (R.string.status_fail_connection_error);
        
        private String message; // The status message.

        /**
         * Creates a new Status with an Android string resource
         * @param resID     Android string resource for the status.
         */
        Status(int resID) {
            message = CPApplication.string(resID);
        }

        /**
         * Returns a string representation of the status.
         * @return  A string representation of the status.
         */
        public String toString() {
            return message;
        }
    }
    
    /// The Serial queue on which every API task should run.
    private static final Executor SERIAL_QUEUE = Executors.newSingleThreadExecutor();

    /**
     * Callback lambda function used to pass the results of an asynchronous sync operation.
     */
    public interface ResultCallback {
        void onSyncResult(Status status);
    }

    /**
     * Sends a login request to the synchronisation API server.
     * @param signup        Whether the operation should create an account or log into one.
     * @param email         The user's email address.
     * @param password      The user's password.
     * @param onResult      Callback called when the operation finishes.
     */
    public static void logIn(boolean signup,
                             @NonNull String email,
                             @NonNull String password,
                             @Nullable ResultCallback onResult) {
        APILoginOperation task = new APILoginOperation(signup, onResult);
        task.executeOnExecutor(SERIAL_QUEUE, email, password);
    }

    /**
     * Logs the user out. Credentials are invalidated, and transaction IDs reset.
     */
    public static void logOut() {
        Authenticator.invalidateCredentials();
        Authenticator.invalidateSyncDates();
        Model.flushDeleted();
    }

    /**
     * Refreshes the data sequentially (deletes, then updates) and runs a callback when both
     * operations are finished.
     * @param onResult       Callback called when the operation finishes.
     */
    public static void refresh(@Nullable final ResultCallback onResult) {
        if(!Authenticator.isLoggedIn()) {
            if(onResult != null) {
                onResult.onSyncResult(Status.FAIL_LOGGED_OUT);
            }
            return;
        }
        new SyncDeleteOperation(new ResultCallback() {
            @Override
            public void onSyncResult(Status status) {
                if(status == Status.SUCCESS) {
                    new SyncUpdateOperation(onResult).executeOnExecutor(SERIAL_QUEUE);
                } else if(onResult != null) {
                    onResult.onSyncResult(status);
                }
            }
        }).executeOnExecutor(SERIAL_QUEUE);
    }

    /**
     * Refreshes deleted notes.
     */
    public static void refreshDelete() {
        if(!Authenticator.isLoggedIn()) { return; }
        new SyncDeleteOperation(null).executeOnExecutor(SERIAL_QUEUE);
    }

    /**
     * Refreshes updated and created notes.
     */
    public static void refreshUpdate() {
        if(!Authenticator.isLoggedIn()) { return; }
        new SyncUpdateOperation(null).executeOnExecutor(SERIAL_QUEUE);
    }

    /**
     * Refreshes the data silently.
     */
    public static void refresh() {
        if(!Authenticator.isLoggedIn()) { return; }
        
        new SyncDeleteOperation(null).executeOnExecutor(SERIAL_QUEUE);
        new SyncUpdateOperation(null).executeOnExecutor(SERIAL_QUEUE);
    }
}