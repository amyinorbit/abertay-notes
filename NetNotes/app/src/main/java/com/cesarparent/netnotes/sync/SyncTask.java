package com.cesarparent.netnotes.sync;

import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;

import com.cesarparent.netnotes.model.DBController;
import com.cesarparent.netnotes.model.Model;
import com.cesarparent.netnotes.model.Note;
import com.cesarparent.utils.JSONAble;
import com.cesarparent.utils.SQLObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;

/**
 * Created by cesar on 09/03/2016.
 * 
 * Base class for Asynchronous API synchronisation tasks
 */
public abstract class SyncTask<T extends SQLObject & JSONAble> extends AsyncTask<Void, Void, Boolean> {
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

    protected abstract JSONArray getChanges(String transaction);
    
    protected abstract T create(JSONObject obj);
    
    private boolean processResponse(final APIResponse res) {
        final JSONArray updates = res.getChangeSet();
        if(updates == null) {
            Log.e("SyncUpdateTask", "Invalid response payload");
            return false;
        }
        Authenticator.setUpdateTransactionID(res.getTransactionID());
        if(updates.length() == 0) { return false; }
        DBController.sharedInstance().updateBlock(new DBController.UpdateBlock() {
            @Override
            public void run(SQLiteDatabase db) {
                for(int i = 0; i < updates.length(); ++i) {
                    try {
                        T n = create(updates.getJSONObject(i));
                        db.execSQL("INSERT OR REPLACE INTO note" +
                                           "(uniqueID, text, createDate, sortDate, seqID)" +
                                           "VALUES (?, ?, ?, ?, ?)",
                                   n.getDatabaseValues());
                        db.setTransactionSuccessful();
                    }
                    catch(Exception e) {
                        Log.e("SyncUpdateTask", "Error parsing response: "+e.getMessage());
                    }
                }
            }
        });
        return true;
    }
}
