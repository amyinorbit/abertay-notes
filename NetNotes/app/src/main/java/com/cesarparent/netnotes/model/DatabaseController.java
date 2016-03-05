package com.cesarparent.netnotes.model;

import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

/**
 * Created by cesar on 04/03/2016.
 */
public class DatabaseController {

    private DatabaseHelper _helper;

    public interface UpdateBlock {
        void execute(SQLiteDatabase db);
    }
    
    public DatabaseController() {
        _helper = new DatabaseHelper();
    }
    
    public void runInTransaction(final UpdateBlock block) {
        runInSerialQueue(new Runnable() {
            @Override
            public void run() {
                SQLiteDatabase database = _helper.getWritableDatabase();
                database.beginTransaction();
                block.execute(database);
                database.endTransaction();
            }
        });
    }
    
    public void runFetch() {
        
    }
    
    private void runInSerialQueue(Runnable r) {
        (new DBTask()).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, r);
    }
    
    protected class DBTask extends AsyncTask<Runnable, Void, Void> {
        
        @Override
        public Void doInBackground(Runnable[] params) {
            for(Runnable r : params) {
                r.run();
            }
            return null;
        }
        
        @Override
        public void onPostExecute(Void result) {
            
        }
    }
    
    protected class FetchTask {
        
    }
}
