package com.cesarparent.netnotes.sync;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;

import com.cesarparent.netnotes.model.DBController;
import com.cesarparent.netnotes.model.Model;
import com.cesarparent.netnotes.model.Note;

import org.json.JSONArray;
import org.json.JSONException;

import java.text.ParseException;

/**
 * Created by cesar on 09/03/2016.
 * 
 * 
 */
public class SyncUpdateTask extends AsyncTask<Void, Void, Boolean> {
    private static final String SELECT_SQL = "SELECT uniqueID, text, createDate, sortDate " +
                                             "FROM note WHERE seqID > ?";
    
    @Override
    protected Boolean doInBackground(Void... params) {
        
        String transaction = Authenticator.getUpdateTransactionID();
        
        JSONArray changes = getChanges(transaction);
        if(changes == null) {
            return false;
        }
        
        // Send the request and get data back
        APIRequest req = new APIRequest(APIRequest.ENDPOINT_NOTES, transaction);
        req.putData(changes);
        req.setAuthtorization(Authenticator.getAuthToken());
        final APIResponse res = req.send();
        
        // If Unauthorised, invalidate credientials
        if(res.getStatus() == APIResponse.UNAUTHORIZED) {
            Authenticator.invalidateCredentials();
            Authenticator.invalidateSyncDates();
            return false;
        }
        if(res.getStatus() != APIResponse.SUCCESS) {
            return false;
        }
        
        // Parse the received JSON
        return processResponse(res);
    }
    
    @Override
    protected void onPostExecute(Boolean refresh) {
        if(refresh) {
            Model.refresh();
        }
    }
    
    private JSONArray getChanges(String transaction) {
        JSONArray changes = new JSONArray();
        // Try/Catch block with resources, equivalent to try/catch/finally{close()}
        try (Cursor c = DBController.sharedInstance().fetch(SELECT_SQL, transaction)) {
            for (int i = 0; i < c.getCount(); ++i) {
                c.moveToPosition(i);
                Note n = new Note(c.getString(0), c.getString(1), c.getString(2), c.getString(3));
                changes.put(n.toJSON());
            }
        } catch (JSONException e) {
            Log.e("SyncUpdateTask", "Error creating request body: "+e.getMessage());
            return null;
        }
        return changes;
    }
    
    private boolean processResponse(final APIResponse res) {
        final JSONArray updates = res.getChangeSet();
        if(updates == null) {
            Log.e("SyncUpdateTask", "Invalid response payload");
            return false;
        }
        Authenticator.setUpdateTransactionID(res.getTransactionID());
        if(updates.length() == 0) { return false; }
        DBController.sharedInstance().updateBlock(db -> {
            for(int i = 0; i < updates.length(); ++i) {
                try {
                    Note n = new Note(updates.getJSONObject(i));
                    db.execSQL("INSERT OR REPLACE INTO note (" + n.getDatabaseColumns() + ") " +
                                       "VALUES (" + n.getDatabasePlaceholders() + ")",
                               n.getDatabaseValues());
                    db.setTransactionSuccessful();
                }
                catch(ParseException | JSONException e) {
                    Log.e("SyncUpdateTask", "Error parsing response: "+e.getMessage());
                }
            }
        });
        return true;
    }
}
