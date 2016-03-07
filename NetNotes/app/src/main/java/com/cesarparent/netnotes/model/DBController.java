package com.cesarparent.netnotes.model;


import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;


import com.cesarparent.netnotes.CPApplication;

import java.util.ArrayList;

/**
 * Created by cesar on 23/02/2016.
 * 
 * DatabaseHelper extends SQLiteOpenHelper for opening/closing databases.
 */
public class DBController extends SQLiteOpenHelper {

    
    // Interfaces used as runnable-style callbacks
    public interface DBUpdateBlock {
        void run(SQLiteDatabase db);
    }
    
    public interface DBFetchBlock {
        Cursor run(SQLiteDatabase db);
    }
    
    public interface DBResultBlock {
        void run(Cursor c);
    }
    
    // Basic DatabaseHelper data
    public static final int kDBVersion = 3;
    public static final String kDBName = "netnotes.db";
    
    public static final String TABLE = "note";
    
    public static final String[] NOTE_COLUMNS = {
            "uniqueID",
            "text",
            "createDate",
            "sortDate"
    };
    
    private static final String SQL_CREATE_ENTRIES = 
            "CREATE TABLE note ("+
            "uniqueID   CHAR(36) PRIMARY KEY,"+
            "text       TEXT NOT NULL DEFAULT \"\","+
            "createDate DATETIME,"+
            "sortDate   DATETIME)";
    
    private static final String SQL_DELETE_ENTRIES = "DROP TABLE note";
    
    private static DBController _instance = null;
    
    public static synchronized DBController sharedInstance() {
        if(_instance == null) {
            _instance = new DBController();
        }
        return _instance;
    }
    
    private DBController() {
        super(CPApplication.getContext(), kDBName, null, kDBVersion);
    }
    
    
    // DatabaseHelper overrides
    
    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d("DBController", "Creating Database Schema...");
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
    
    public void runUpdate(DBUpdateBlock update) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        update.run(db);
        db.endTransaction();
    }
    
    public void runUpdateInBackground(DBUpdateBlock update) {
        new DBUpdateTask(update).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
    }
    
    public Cursor fetch(DBFetchBlock fetch) {
        SQLiteDatabase db = getReadableDatabase();
        return fetch.run(db);
    }
    
    public void fetchInBackground(DBFetchBlock fetch, DBResultBlock result) {
        new DBFetchTask(fetch, result).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
    }
    
    
    // AsyncTask stuff
    
    private class DBUpdateTask extends AsyncTask<Void, Void, Void> {
        
        DBUpdateBlock _block;
        
        public DBUpdateTask(DBUpdateBlock block) {
            _block = block;
        }
        
        @Override
        protected Void doInBackground(Void... params) {
            SQLiteDatabase db = getWritableDatabase();
            db.beginTransaction();
            _block.run(db);
            db.endTransaction();
            return null;
        }
    }
    
    private class DBFetchTask extends AsyncTask<Void, Void, Cursor> {
        
        DBFetchBlock _fetch;
        DBResultBlock _result;
        
        public DBFetchTask(DBFetchBlock fetch, DBResultBlock results) {
            _fetch = fetch;
        }
        
        @Override
        protected Cursor doInBackground(Void... params) {
            SQLiteDatabase db = getReadableDatabase();
            return _fetch.run(db);
        }
        
        // We need the result blocks to run on the UI thread
        @Override
        protected void onPostExecute(Cursor results) {
            _result.run(results);
            if(!results.isClosed()) {
                results.close();
            }
        }
    }
}
