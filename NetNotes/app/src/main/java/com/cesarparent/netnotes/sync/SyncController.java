package com.cesarparent.netnotes.sync;

import android.os.AsyncTask;
import com.cesarparent.netnotes.model.Model;

/**
 * Created by cesar on 23/02/2016.
 *
 *
 */
public class SyncController {

    private static SyncController   _instance = null;

    /**
     * Get the shared Sync Controller singleton instance for the app.
     * @return  The shared SyncController
     */
    public static synchronized SyncController sharedInstance() {
        if(_instance == null) {
            _instance = new SyncController();
        }
        return _instance;
    }
    
    /**
     * Creates a Sync Controller. Only called by sharedInstance().
     * When created, the controller will check if credentials are stored on the device, and
     * if yes check that they are still valid.
     */
    private SyncController() {
    }
    
    public void logIn(String email, String password, APITaskDelegate delegate) {
        APILoginTask task = new APILoginTask(delegate);
        Model.flushDeleted();
        task.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, email, password);
    }
    
    public void logOut() {
        Authenticator.invalidateCredentials();
        Model.flushDeleted();
    }
    
    public void triggerSync() {
        if(!Authenticator.isLoggedIn()) { return; }
        
        new SyncDeleteTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
        new SyncUpdateTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
    }
}