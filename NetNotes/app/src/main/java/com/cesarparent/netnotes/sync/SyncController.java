package com.cesarparent.netnotes.sync;

import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;

import com.cesarparent.netnotes.CPApplication;
import com.cesarparent.netnotes.model.DBController;
import com.cesarparent.netnotes.model.Model;
import com.cesarparent.netnotes.model.Note;
import com.cesarparent.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.Date;

/**
 * Created by cesar on 23/02/2016.
 *
 *
 */
public class SyncController {
    
    private static final String UPDATES_SQL = "SELECT uniqueID, text, createDate, sortDate " +
                                              "FROM note WHERE sortDate > ?";
    private static final String DELETES_SQL = "SELECT uniqueID FROM deleted WHERE deleteDate > ?";

    private static SyncController   _instance = null;
    private Authenticator           _authenticator;
    private Date                    _lastSync;

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
        _lastSync = new Date(CPApplication.getSharedPreferences().getLong("sync.date", 0));
    }
    
    public Authenticator getAuthenticator() {
        return _authenticator;
    }
    
    public void logIn(String email, String password, APITaskDelegate delegate) {
        APILoginTask task = new APILoginTask(delegate);
        task.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, email, password);
    }
    
    public void triggerSync() {
        if(!getAuthenticator().isLoggedIn()) { return; }
        final String last = Utils.JSONDate(_lastSync);
        Log.i("SyncController", "Starting sync since: "+last);
        new DBController.Fetch(UPDATES_SQL, new DBController.onResult() {
            @Override
            public void run(Cursor c) {
                sendUpdates(c, last);
            }
        }).execute(last);
        
        new DBController.Fetch(DELETES_SQL, new DBController.onResult() {
            @Override
            public void run(Cursor c) {
                sendDeletes(c, last);
            }
        }).execute(last);
        
    }
    
    private void sendUpdates(Cursor c, String transaction) {
        final Note notes[] = new Note[c.getCount()];
        for(int i = 0; i < c.getCount(); ++i) {
            c.moveToPosition(i);
            notes[i] = new Note(c.getString(0), c.getString(1), c.getString(2), c.getString(3));
        }
        APIJSONTask update = new APIJSONTask(APIRequest.ENDPOINT_NOTES,
                                             getAuthenticator().getToken(),
                                             transaction,
                                             new APITaskDelegate() {
             @Override
             public void taskDidReceiveResponse(APIResponse response) {
                 processAPINotes(response);
             }
        });
        update.execute(notes);
        
    }
    
    private void sendDeletes(Cursor c, String transaction) {
        final String uuids[] = new String[c.getCount()];
        for(int i = 0; i < c.getCount(); ++i) {
            c.moveToPosition(i);
            uuids[i] = c.getString(0);
        }
        APIJSONTask delete = new APIJSONTask(APIRequest.ENDPOINT_DELETE,
                                             getAuthenticator().getToken(),
                                             transaction,
                                             new APITaskDelegate() {
            @Override
            public void taskDidReceiveResponse(APIResponse response) {
                processAPIDeletes(response);
            }
        });
        delete.execute(uuids);
    }
    
    private void processAPINotes(APIResponse res) {
        if(res.getStatus() != APIResponse.SUCCESS) { return; }
        try {
            JSONArray changes = res.getBody().getJSONArray("changes");
            for(int i = 0; i < changes.length(); ++i) {
                // TODO: Change to one single transaction for every add/update
                Model.sharedInstance().addNote(new Note(changes.getJSONObject(i)));
            }
        }
        catch(JSONException e) {
            Log.e("SyncController", "Invalid repsonse JSON");
        }
        catch(ParseException e) {
            Log.e("SyncController", "Invalid date format returned");
        }
    }
    
    private void processAPIDeletes(APIResponse res) {
        if(res.getStatus() != APIResponse.SUCCESS) { return; }
        try {
            JSONArray changes = res.getBody().getJSONArray("changes");
            for(int i = 0; i < changes.length(); ++i) {
                // TODO: Change to one single transaction for every deletion
                Model.sharedInstance().deleteNoteWithUniqueID(changes.getString(i));
            }
        }
        catch(JSONException e) {
            Log.e("SyncController", "Invalid repsonse JSON");
        }
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