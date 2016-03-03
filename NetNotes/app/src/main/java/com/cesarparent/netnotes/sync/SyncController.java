package com.cesarparent.netnotes.sync;

import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.cesarparent.netnotes.CPApplication;
import com.cesarparent.netnotes.R;
import com.cesarparent.netnotes.model.Note;

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

    private static SyncController _instance = null;
    
    private WeakReference   _delegate;
    
    private boolean         _loggedIn;
    
    private String          _username;

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
        _delegate = new WeakReference<SyncDelegate>(null);
    }

    /**
     * Sets the sync controller's delegate. The delegate wil be notified of sync events.
     * @param delegate  The controller's delegate.
     */
    public void setDelegate(SyncDelegate delegate) {
        _delegate = new WeakReference<>(delegate);
    }
    
    /**
     * Post a new or update note to the server.
     * @param note  The note to send to the server.
     */
    public void postNote(Note note) {
        if(!_loggedIn) { return; }
        // Get a note that can be used on other threads.
        new PostNoteTask().execute(note.detachedCopy());
    }

    /**
     * Register a note for deletion from the server.
     * @param note  The note to delete from the server.
     */
    public void deleteNote(Note note) {
        if(!_loggedIn) { return; }
        String uuid = note.uniqueID();
    }
    
    
    private class PostNoteTask extends AsyncTask<Note, Void, JSONArray> {
        
        private String _auth;
        
        @Override
        protected void onPreExecute() {
            String username = CPApplication.getContext().getString(R.string.email);
            String password = CPApplication.getContext().getString(R.string.password);
            
            String cred = username + ":" + password;
            this._auth = "Basic "+ Base64.encodeToString(cred.getBytes(), Base64.DEFAULT);
        }
        
        
        @Override
        protected JSONArray doInBackground(Note... params) {
            
            
            try {
                JSONArray body = new JSONArray();
                for(Note note: params) {
                    body.put(note.toJSON());
                }
                
                URL url = new URL(CPApplication.getContext().getString(R.string.api_location)+"/notes");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                conn.setDoOutput(true);
                conn.setDoInput(true);
                conn.setUseCaches(false);
                conn.setChunkedStreamingMode(0);
                conn.setRequestMethod("POST");
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZZZ");
                conn.setRequestProperty("X-NetNotes-Time", format.format(new Date()));
                conn.setRequestProperty("Authorization", _auth);
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                
                
                // Write the body
                OutputStream os = conn.getOutputStream();
                os.write(body.toString().getBytes("UTF-8"));
                os.close();
                
                
                conn.connect();
                Log.d("API CONNECTION", body.toString());
                Log.d("API CONNECTION", "Status: " + conn.getResponseCode());
                Log.d("API CONNECTION", "Auth: "+this._auth);
                
                // now we read
                BufferedReader r = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String json = "", line = "";
                
                while((line = r.readLine()) != null) {
                    json += line + "\n";
                }
                JSONArray obj = new JSONArray(json);
                Log.d("Network Manager", obj.toString());
                return new JSONArray(json);
            }
            catch(MalformedURLException e) {

            }
            catch(Exception e) {
                Log.e("Network Manager", "YOU EFFIN DUN GOOFED:\n"+e.getMessage());
                e.printStackTrace();
            }


            return null;
        }

        @Override
        protected void onPostExecute(JSONArray obj) {
            Toast.makeText(CPApplication.getContext(), "RECEIVED!", Toast.LENGTH_LONG).show();
        }
    }
}