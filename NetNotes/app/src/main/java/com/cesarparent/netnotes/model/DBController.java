package com.cesarparent.netnotes.model;


import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.util.Log;


import com.cesarparent.netnotes.CPApplication;
import com.cesarparent.netnotes.sync.SyncUtils;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by cesar on 23/02/2016.
 * 
 * DBController provides a simple API used to communicate with the notes database. Asynchronous
 * Database Tasks should be preferred over raw calls to fetch() and update(): They will run in a
 * thread separate from the UI thread, in a serial queue.
 */
public class DBController extends SQLiteOpenHelper {
    
    // Callback used to pass multiple database updates.
    public interface UpdateCallback {
        boolean run(SQLiteDatabase db);
    }

    // Callback block used to return asynchronous fetch results.
    public interface ResultBlock {
        void run(Cursor c);
    }
    
    // Database Helper variables
    public static final int DB_VERSION = 1;
    public static final String DB_NAME = "netnotes.db";
    
    // SQL queries used to install the database
    
    private static final String SQL_CREATE_NOTE = 
            "CREATE TABLE note ("+
            "uniqueID   CHAR(36) PRIMARY KEY,"+
            "text       TEXT NOT NULL DEFAULT \"\","+
            "createDate DATETIME,"+
            "sortDate   DATETIME," + 
            "seqID      BIGINT)";
    
    private static final String SQL_CREATE_DELETE =
            "CREATE TABLE deleted ("+
            "uniqueID   CHAR(36) PRIMARY KEY,"+
            "seqID      BIGINT)";
    
    private static final String[] SQL_CREATE = {SQL_CREATE_NOTE, SQL_CREATE_DELETE};
    
    // SQL Queries used to delete the database
    
    private static final String SQL_DELETE_NOTE = "DROP TABLE note";
    private static final String SQL_DELETE_DELETE = "DROP TABLE deleted";
    private static final String[] SQL_DROP = {SQL_DELETE_NOTE, SQL_DELETE_DELETE};
    
    public static final Executor SERIAL_QUEUE = Executors.newSingleThreadExecutor();
    
    // The DBController singleton instance.
    private static DBController _instance = null;
    
    // The lock used to synchronise database access
    private static final Object DB_LOCK = new Object();

    /**
     * Returns the shared DBController singleton object.
     * @return  The shared DBController singleton object.
     */
    public static synchronized DBController sharedInstance() {
        if(_instance == null) {
            _instance = new DBController();
        }
        return _instance;
    }

    /**
     * Creates a new DBController. Can only be called by sharedInstance().
     */
    private DBController() {
        super(CPApplication.getContext(), DB_NAME, null, DB_VERSION);
    }
    
    // DatabaseHelper overrides
    
    /**
     * Called when the database is first created.
     * @param db    The SQLite database.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d("DBController", "Creating Database Schema...");
        for(String query : SQL_CREATE) {
            db.execSQL(query);
        }
    }

    /**
     * Called when an upgrade is done, deletes the data and re-create tables.
     * @param db            The SQLite database.
     * @param oldVersion    The previous version.
     * @param newVersion    The new version.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        SyncUtils.invalidateSyncDates();
        for(String query : SQL_DROP) {
            db.execSQL(query);
        }
        onCreate(db);
    }

    /**
     * Called when a downgrade is requested, defaults to upgrade behaviour.
     * @param db            The SQLite database.
     * @param oldVersion    The previous version.
     * @param newVersion    The new version.
     */
    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
    
    // Commodity functions
    
    /**
     * Runs a synchronised database query and returns the results as a cursor.
     * @param query     The SQL query.
     * @param params    The SQL query's parameters.
     * @return          A Cursor object with the fetch query's results
     */
    public Cursor fetch(String query, String... params) {
        synchronized (DB_LOCK) {
            return getReadableDatabase().rawQuery(query, params);
        }
    }
    
    /**
     * Runs synchronised a database update in a transaction.
     * @param query     The SQL query.
     * @param params    The SQL query's parameters.
     */
    public void update(String query, Object... params) {
        synchronized (DB_LOCK) {
            SQLiteDatabase db = getWritableDatabase();
            db.beginTransaction();
            db.execSQL(query, params);
            db.setTransactionSuccessful();
            db.endTransaction();
        }
    }
    /**
     * Runs a Database update block, synchronised and in a transaction. The update block can
     * call any number of updates on the database object it is given.
     * @param           A UpdateCallback block which is given a valid, writable SQLite database.
     * @return          True if block ran successfully, false otherwise.
     */
    public boolean updateBlock(UpdateCallback block) {
        synchronized (DB_LOCK) {
            SQLiteDatabase db = getWritableDatabase();
            db.beginTransaction();
            boolean result = block.run(db);
            if(result) {
                db.setTransactionSuccessful();
            }
            db.endTransaction();
            return result;
        }
    }
    
    // AsyncTask stuff
    
    /// Asynchronous task used to run a database update in a non-UI thread.
    /// Database tasks should be ran on the DBController's SERIAL_QUEUE.
    public static class Update extends AsyncTask<Object, Void, Void> {
        
        String          _query;     // The SQL query.
        Runnable        _done;      // The callback, ran when the update is done.
        
        /**
         * Creates a new Asynchronous Update Task.
         * @param query     The SQL update query.
         * @param done      The completion callback.
         */
        public Update(String query, Runnable done) {
            _query = query;
            _done = done;
        }

        @Override
        protected Void doInBackground(Object... params) {
            synchronized (DB_LOCK) {
                SQLiteDatabase db = sharedInstance().getWritableDatabase();
                db.beginTransaction();
                db.execSQL(_query, params);
                db.setTransactionSuccessful();
                db.endTransaction();
                return null;
            }
        }
        
        @Override
        protected void onPostExecute(Void param) {
            if(_done != null) {
                _done.run();
            }
        }
    }


    /// Asynchronous task used to run a database fetch in a non-UI thread.
    /// Database tasks should be ran on the DBController's SERIAL_QUEUE.
    public static class Fetch extends AsyncTask<String, Void, Cursor> {
        
        String          _query;         // The SQL query.
        ResultBlock     _result;        // The callback called with the fetch's results.
        
        /**
         * Creates a new Asynchronous Fetch Task.
         * @param query     The SQL update query.
         * @param result    The completion callback.
         */
        public Fetch(String query, ResultBlock result) {
            _query = query;
            _result = result;
        }
        
        @Override
        protected Cursor doInBackground(String... params) {
            synchronized (DB_LOCK) {
                SQLiteDatabase db = sharedInstance().getReadableDatabase();
                return db.rawQuery(_query, params);
            }
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
