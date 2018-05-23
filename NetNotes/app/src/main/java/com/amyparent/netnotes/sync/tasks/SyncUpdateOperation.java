package com.cesarparent.netnotes.sync.tasks;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
 * Task used to send updated and new notes to the server.
 */
public class SyncUpdateOperation extends SyncOperation {
    private static final String SELECT_SQL = "SELECT uniqueID, text, createDate, sortDate " +
                                             "FROM note WHERE seqID < 0";

    /**
     * Creates a new SyncUpdateOperation.
     * @param onResult      The callback called when the request finishes.
     */
    public SyncUpdateOperation(@Nullable Sync.ResultCallback onResult) {
        super(APIRequest.Endpoint.NOTES, onResult);
    }

    /**
     * Fetches the notes that were either created or updated since the last successful request
     * (marked with seqID = -1 in database) and exports them as JSON.
     * @return  A JSONArray of updated and created notes.
     */
    @Nullable
    @Override
    protected JSONArray getChanges() {
        JSONArray changes = new JSONArray();
        
        // Try/Catch block with resources, equivalent to try/catch/finally{close()}
        try (Cursor c = DBController.sharedInstance().fetch(SELECT_SQL)) {
            for (int i = 0; i < c.getCount(); ++i) {
                c.moveToPosition(i);
                Note n = new Note(c.getString(0), c.getString(1), c.getString(2), c.getString(3));
                changes.put(n.toJSON());
            }
        } catch (JSONException e) {
            Log.e("SyncUpdateOperation", "Error creating request body: "+e.getMessage());
            return null;
        }
        // Tag the notes with the operation's ID so they can be marked as up-to-date on response.
        DBController.sharedInstance().update("UPDATE note SET seqID = ? WHERE seqID < 0", ID);
        return changes;
    }

    /**
     * Called on request fail. Resets the notes seqIDs to -1 so they will be caught and sent
     * to the server by the next request.
     */
    @Override
    protected void onFail() {
        new DBController.Update("UPDATE note SET seqID = -1 WHERE seqID = ?", null)
                .executeOnExecutor(DBController.SERIAL_QUEUE, ID);
    }

    /**
     * Updates the notes database according to the server's response. Marks any note tagged with
     * this operation's ID as up-to-date (seqID = 0).
     * @param data          The data extracted from the response.
     * @return  true if the data was processed successfully, false otherwise.
     */
    @Override
    protected boolean processResponseData(@NonNull final JSONArray data) {

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
                        Log.e("SyncUpdateOperation", "Error parsing response: " + e.getMessage());
                        return false;
                    }
                }
                db.execSQL("UPDATE note SET seqID = 0 WHERE seqID = ?", new Object[]{ID});
                return true;
            }
        });
    }
}
