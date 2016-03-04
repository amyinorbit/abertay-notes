package com.cesarparent.netnotes.sync;

import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.cesarparent.netnotes.CPApplication;
import com.cesarparent.netnotes.R;
import com.cesarparent.netnotes.model.Note;
import com.cesarparent.utils.NotificationCenter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by cesar on 23/02/2016.
 *
 *
 */
public class SyncController {
    
    /// String contants for notifications
    public static final String kLoggedInNotification =          "kLoggedInNotification";
    public static final String kLoggedOutNotification =         "kLoggedOutNotification";
    public static final String kLoginFailNotification =         "kLoginFailNotification";
    public static final String kUpdateReceivedNotification =    "kUpdateReceivedNotification";
    public static final String kDeleteReceivedNotification =    "kDeleteReceivedNotification";
    
    private static final int API_KEY = "";

    private static SyncController _instance = null;
    
    private WeakReference<SyncDelegate>     _delegate;
    
    private boolean                         _loggedIn;
    
    private String                          _username;

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
        // TODO: Check if there are credentials, and if there are try and login.
        _loggedIn = false;
        _delegate = new WeakReference<>(null);
    }

    /**
     * Sets the sync controller's delegate. The delegate wil be notified of sync events.
     * @param delegate  The controller's delegate.
     */
    public void setDelegate(SyncDelegate delegate) {
        _delegate = new WeakReference<>(delegate);
    }
    
    
    public String getAuthorizationString() {
        // TODO: Store password in keystore, get credentials from login screen.
        String cred =   CPApplication.string(R.string.email) + ":"+
                        CPApplication.string(R.string.password);
        return "Basic "+ Base64.encodeToString(cred.getBytes(), Base64.DEFAULT);
    }
    
    /**
     * Post a new or update note to the server.
     * @param note  The note to send to the server.
     */
    public void postNote(Note note) {
        if(!_loggedIn) { return; }
        // Get a note that can be used on other threads.
    }

    /**
     * Register a note for deletion from the server.
     * @param note  The note to delete from the server.
     */
    public void deleteNote(Note note) {
        if(!_loggedIn) { return; }
        String uuid = note.uniqueID();
    }
    
    
    public static class LoginTask extends AsyncTask<Void, Void, APIResponse> {
        
        @Override
        protected APIResponse doInBackground(Void... params) {
            APIRequest request = new APIRequest(APIRequest.ENDPOINT_LOGIN, "POST");
            return request.send();
        }
        
        @Override
        protected void onPostExecute(APIResponse response) {
            if(response.getStatus() == APIResponse.SUCCESS) {
                NotificationCenter.defaultCenter().postNotification(kLoggedInNotification, null);
            } else {
                NotificationCenter.defaultCenter().postNotification(kLoggedOutNotification, null);
            }
            Log.d("LoginTask", "Login status: "+response.getStatus());
        }
    }
}