package com.cesarparent.netnotes.sync.tasks;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import com.cesarparent.netnotes.model.DBController;
import com.cesarparent.netnotes.sync.APIRequest;
import com.cesarparent.netnotes.sync.Sync;

import org.json.JSONArray;

/**
 * Created by cesar on 09/03/2016.
 * 
 * 
 */
public class SyncDeleteTask extends SyncTask {

    private static final String SELECT_SQL = "SELECT uniqueID FROM deleted WHERE seqID < 0";
    
    public SyncDeleteTask(Sync.ResultCallback onResult) {
        super(APIRequest.ENDPOINT_DELETE, onResult);
    }

    @Override
    protected JSONArray getChanges(String transaction) {
        JSONArray changes = new JSONArray();
        Cursor c = DBController.sharedInstance().fetch(SELECT_SQL);
        for (int i = 0; i < c.getCount(); ++i) {
            c.moveToPosition(i);
            changes.put(c.getString(0));
        }
        c.close();
        DBController.sharedInstance().update("UPDATE deleted SET seqID = ? WHERE seqID < 0", ID);
        return changes;
    }
    
    @Override
    protected void onFail() {
        new DBController.Update("UPDATE deleted SET seqID = -1 WHERE seqID = ?", null)
                .executeOnExecutor(DBController.SERIAL_QUEUE, ID);
    }

    @Override
    protected boolean processResponseData(final JSONArray data, final String transaction) {
        return DBController.sharedInstance().updateBlock(new DBController.UpdateCallback() {
            @Override
            public boolean run(SQLiteDatabase db) {
                for (int i = 0; i < data.length(); ++i) {
                    try {
                        String uniqueID = data.getString(i);
                        db.execSQL("DELETE FROM note WHERE uniqueID = ?",
                                   new Object[]{uniqueID});
                    } catch (Exception e) {
                        Log.e("SyncUpdateTask", "Error parsing response: " + e.getMessage());
                        return false;
                    }
                }
                db.execSQL("DELETE FROM deleted WHERE seqID = ?", new Object[]{ID});
                return true;
            }
        });
    }


}
