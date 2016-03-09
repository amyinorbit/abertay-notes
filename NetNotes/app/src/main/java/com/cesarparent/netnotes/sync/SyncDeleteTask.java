package com.cesarparent.netnotes.sync;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import com.cesarparent.netnotes.model.DBController;

import org.json.JSONArray;

/**
 * Created by cesar on 09/03/2016.
 * 
 * 
 */
public class SyncDeleteTask extends SyncTask {

    private static final String SELECT_SQL = "SELECT uniqueID FROM deleted WHERE seqID > ?";
    
    public SyncDeleteTask() {
        super(APIRequest.ENDPOINT_DELETE);
    }

    @Override
    protected JSONArray getChanges(String transaction) {
        JSONArray changes = new JSONArray();
        Cursor c = DBController.sharedInstance().fetch(SELECT_SQL, transaction);
        for (int i = 0; i < c.getCount(); ++i) {
            c.moveToPosition(i);
            changes.put(c.getString(0));
        }
        c.close();
        return changes;
    }

    @Override
    protected boolean processResponseData(final JSONArray data, final String transaction) {
        return DBController.sharedInstance().updateBlock(new DBController.UpdateBlock() {
            @Override
            public boolean run(SQLiteDatabase db) {
                for (int i = 0; i < data.length(); ++i) {
                    try {
                        String uniqueID = data.getString(i);
                        db.execSQL("DELETE FROM note WHERE uniqueID = ?",
                                   new Object[]{uniqueID});
                        db.execSQL("INSERT OR REPLACE INTO deleted (uniqueID, seqID) VALUES (?, ?)",
                                   new Object[]{
                                           uniqueID,
                                           transaction
                                   });
                    } catch (Exception e) {
                        Log.e("SyncUpdateTask", "Error parsing response: " + e.getMessage());
                        return false;
                    }
                }
                return true;
            }
        });
    }


}
