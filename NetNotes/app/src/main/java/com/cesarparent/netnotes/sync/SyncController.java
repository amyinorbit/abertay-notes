package com.cesarparent.netnotes.sync;

import android.content.SharedPreferences;
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
    
    private static final String API_KEY = "C162E35C-638C-478A-8A57-F89FA72B9AA6";

    private static SyncController _instance = null;
    private Authenticator _authenticator;

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
        _authenticator = new Authenticator();
    }
    
    public Authenticator getAuthenticator() {
        return _authenticator;
    }
    
    public void logIn(String email, String password, APITaskDelegate delegate) {
        APILoginTask task = new APILoginTask(delegate);
        task.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, email, password);
    }
    
    /**
     * Post a new or update note to the server.
     * @param note  The note to send to the server.
     */
    public void postNote(Note note) {
        if(!_authenticator.isLoggedIn()) { return; }
        // Get a note that can be used on other threads.
    }

    /**
     * Register a note for deletion from the server.
     * @param note  The note to delete from the server.
     */
    public void deleteNote(Note note) {
        if(!_authenticator.isLoggedIn()) { return; }
        String uuid = note.uniqueID();
    }
}