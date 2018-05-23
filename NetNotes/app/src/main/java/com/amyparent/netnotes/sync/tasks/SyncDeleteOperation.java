package com.cesarparent.netnotes.sync.tasks;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import com.cesarparent.netnotes.model.DBController;
import com.cesarparent.netnotes.sync.APIRequest;
import com.cesarparent.netnotes.sync.Sync;

import org.json.JSONArray;

/**
 * Created by cesar on 09/03/2016.
 * 
 * Task used to send deleted notes to the server.
 */
public class SyncDeleteOperation extends SyncOperation {

    private static final String SELECT_SQL = "SELECT uniqueID FROM deleted WHERE seqID < 0";

    /**
     * Creates a new SyncDeleteOperation.
     * @param onResult      The callback called when the request finishes.
     */
    public SyncDeleteOperation(@Nullable Sync.ResultCallback onResult) {
        super(APIRequest.Endpoint.DELETE, onResult);
    }

    /**
     * Fetches the IDs of the notes deleted since last transaction.
     * @return  A JSONArray of deleted note IDs.
     */
    @Nullable
    @Override
    protected JSONArray getChanges() {
        JSONArray changes = new JSONArray();
        Cursor c = DBController.sharedInstance().fetch(SELECT_SQL);
        for (int i = 0; i < c.getCount(); ++i) {
            c.moveToPosition(i);
            changes.put(c.getString(0));
        }
        c.close();
        // Tag the deletions with the operation's ID to make sure no other task sends them to the
        // server.
        DBController.sharedInstance().update("UPDATE deleted SET seqID = ? WHERE seqID < 0", ID);
        return changes;
    }

    /**
     * Called on request fail. Resets the deletedNote IDs to -1 so they will be caught and sent
     * to the server by the next request.
     */
    @Override
    protected void onFail() {
        new DBController.Update("UPDATE deleted SET seqID = -1 WHERE seqID = ?", null)
                .executeOnExecutor(DBController.SERIAL_QUEUE, ID);
    }

    /**
     * Deletes notes that the server has marked as deleted, and remove any deletedNote that
     * was tagged with the operation's ID from the database.
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
                        String uniqueID = data.getString(i);
                        db.execSQL("DELETE FROM note WHERE uniqueID = ?",
                                   new Object[]{uniqueID});
                    } catch (Exception e) {
                        Log.e("SyncUpdateOperation", "Error parsing response: " + e.getMessage());
                        return false;
                    }
                }
                db.execSQL("DELETE FROM deleted WHERE seqID = ?", new Object[]{ID});
                return true;
            }
        });
    }


}
