package com.cesarparent.netnotes.sync.tasks;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import com.cesarparent.netnotes.model.DBController;
import com.cesarparent.netnotes.model.Note;
import com.cesarparent.netnotes.sync.APIRequest;
import com.cesarparent.netnotes.sync.Sync;

import org.json.JSONArray;
import org.json.JSONException;

/**
 * Created by cesar on 09/03/2016.
 * 
 * 
 */
public class SyncUpdateTask extends SyncTask {
    private static final String SELECT_SQL = "SELECT uniqueID, text, createDate, sortDate " +
                                             "FROM note WHERE seqID < 0";
    
    public SyncUpdateTask(Sync.ResultCallback onResult) {
        super(APIRequest.ENDPOINT_NOTES, onResult);
    }
    
    @Override
    protected JSONArray getChanges(String transaction) {
        JSONArray changes = new JSONArray();
        // Try/Catch block with resources, equivalent to try/catch/finally{close()}
        try (Cursor c = DBController.sharedInstance().fetch(SELECT_SQL)) {
            for (int i = 0; i < c.getCount(); ++i) {
                c.moveToPosition(i);
                Note n = new Note(c.getString(0), c.getString(1), c.getString(2), c.getString(3));
                changes.put(n.toJSON());
            }
        } catch (JSONException e) {
            Log.e("SyncUpdateTask", "Error creating request body: "+e.getMessage());
            return null;
        }
        // Tag the changes
        DBController.sharedInstance().update("UPDATE note SET seqID = ? WHERE seqID < 0", ID);
        return changes;
    }
    
    @Override
    protected void onFail() {
        new DBController.Update("UPDATE note SET seqID = -1 WHERE seqID = ?", null)
                .executeOnExecutor(DBController.SERIAL_QUEUE, ID);
    }

    @Override
    protected boolean processResponseData(final JSONArray data, final String transaction) {

        return DBController.sharedInstance().updateBlock(new DBController.UpdateCallback() {
            @Override
            public boolean run(SQLiteDatabase db) {
                for (int i = 0; i < data.length(); ++i) {
                    try {

                        Note n = new Note(data.getJSONObject(i));
                        db.execSQL("INSERT OR REPLACE INTO note " +
                                           "(uniqueID, text, createDate, sortDate, seqID)" +
                                           "VALUES (?, ?, ?, ?, ?)",
                                   new Object[]{
                                           n.uniqueID(),
                                           n.text(),
                                           n.creationDate(),
                                           n.sortDate(),
                                           0
                                   });
                    } catch (Exception e) {
                        Log.e("SyncUpdateTask", "Error parsing response: " + e.getMessage());
                        return false;
                    }
                }
                db.execSQL("UPDATE note SET seqID = 0 WHERE seqID = ?", new Object[]{ID});
                return true;
            }
        });
    }
}