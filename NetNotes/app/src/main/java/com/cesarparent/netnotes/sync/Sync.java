package com.cesarparent.netnotes.sync;

import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import android.util.Log;

import com.cesarparent.netnotes.CPApplication;
import com.cesarparent.netnotes.R;
import com.cesarparent.netnotes.model.Model;
import com.cesarparent.netnotes.sync.tasks.APILoginTask;
import com.cesarparent.netnotes.sync.tasks.SyncDeleteTask;
import com.cesarparent.netnotes.sync.tasks.SyncUpdateTask;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by cesar on 23/02/2016.
 *
 *
 */
public class Sync {
    
    
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
        
        private String message;
        
        Status(int resID) {
            message = CPApplication.string(resID);
        }
        
        public String toString() {
            return message;
        }
    }
    
    private static final Executor SERIAL_QUEUE = Executors.newSingleThreadExecutor();
    
    public interface ResultCallback {
        void onSyncResult(Status status);
    }
    
    @UiThread
    public static void logIn(boolean signup, String email, String password, ResultCallback onResult) {
        APILoginTask task = new APILoginTask(signup, onResult);
        task.executeOnExecutor(SERIAL_QUEUE, email, password);
    }
    
    @UiThread
    public static void logOut() {
        SyncUtils.invalidateCredentials();
        SyncUtils.invalidateSyncDates();
        Model.flushDeleted();
    }
    
    @UiThread
    public static void refresh(@NonNull final ResultCallback onResult) {
        if(!SyncUtils.isLoggedIn()) {
            onResult.onSyncResult(Status.FAIL_LOGGED_OUT);
            return;
        }
        new SyncDeleteTask(new ResultCallback() {
            @Override
            public void onSyncResult(Status status) {
                Log.d("Sync", "Status: "+status);
                if(status == Status.SUCCESS) {
                    new SyncUpdateTask(onResult).executeOnExecutor(SERIAL_QUEUE);
                } else {
                    onResult.onSyncResult(status);
                }
            }
        }).executeOnExecutor(SERIAL_QUEUE);
    }
    
    @UiThread
    public static void refreshDelete() {
        if(!SyncUtils.isLoggedIn()) { return; }
        new SyncDeleteTask(null).executeOnExecutor(SERIAL_QUEUE);
    }
    
    @UiThread
    public static void refreshUpdate() {
        if(!SyncUtils.isLoggedIn()) { return; }
        new SyncUpdateTask(null).executeOnExecutor(SERIAL_QUEUE);
    }
    
    @UiThread
    public static void refresh() {
        if(!SyncUtils.isLoggedIn()) { return; }
        
        new SyncDeleteTask(null).executeOnExecutor(SERIAL_QUEUE);
        new SyncUpdateTask(null).executeOnExecutor(SERIAL_QUEUE);
    }
}