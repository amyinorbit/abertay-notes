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
        FAIL_LOGGED_OUT         (R.string.status_fail_logged_out),
        FAIL_BAD_REQUEST        (R.string.status_fail_bad_request),
        FAIL_UNAUTHORIZED       (R.string.status_fail_unauthorized),
        FAIL_NO_NETWORK         (R.string.status_fail_no_network);
        
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
        void run(Status status);
    }
    
    @UiThread
    public static void logIn(String email, String password, ResultCallback onResult) {
        APILoginTask task = new APILoginTask(onResult);
        task.executeOnExecutor(SERIAL_QUEUE, email, password);
    }
    
    @UiThread
    public static void logOut() {
        Authenticator.invalidateCredentials();
        Authenticator.invalidateSyncDates();
        Model.flushDeleted();
    }
    
    @UiThread
    public static void refresh(@NonNull final ResultCallback onResult) {
        if(!Authenticator.isLoggedIn()) {
            onResult.run(Status.FAIL_LOGGED_OUT);
        }
        new SyncDeleteTask(new ResultCallback() {
            @Override
            public void run(Status status) {
                Log.d("Sync", "Status: "+status);
                if(status == Status.SUCCESS) {
                    new SyncUpdateTask(onResult).executeOnExecutor(SERIAL_QUEUE);
                } else {
                    onResult.run(status);
                }
            }
        }).executeOnExecutor(SERIAL_QUEUE);
    }
    
    @UiThread
    public static void refresh() {
        if(!Authenticator.isLoggedIn()) { return; }
        
        new SyncDeleteTask(null).executeOnExecutor(SERIAL_QUEUE);
        new SyncUpdateTask(null).executeOnExecutor(SERIAL_QUEUE);
    }
}