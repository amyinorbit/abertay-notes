package com.cesarparent.netnotes.sync;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import android.util.Log;

import com.cesarparent.netnotes.CPApplication;
import com.cesarparent.netnotes.R;
import com.cesarparent.netnotes.model.Model;
import com.cesarparent.netnotes.sync.tasks.APILoginTask;
import com.cesarparent.netnotes.sync.tasks.SyncDeleteTask;
import com.cesarparent.netnotes.sync.tasks.SyncUpdateTask;

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
    
    public interface ResultCallback {
        void run(Status status);
    }
    
    @UiThread
    public static void logIn(String email, String password, ResultCallback onResult) {
        APILoginTask task = new APILoginTask(onResult);
        task.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, email, password);
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
                    new SyncUpdateTask(onResult).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
                } else {
                    onResult.run(status);
                }
            }
        }).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
    }
    
    @UiThread
    public static void refresh() {
        if(!Authenticator.isLoggedIn()) { return; }
        
        new SyncDeleteTask(null).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
        new SyncUpdateTask(null).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
    }
}