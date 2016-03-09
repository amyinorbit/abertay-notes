package com.cesarparent.netnotes.model;


import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.util.Log;


import com.cesarparent.netnotes.CPApplication;

/**
 * Created by cesar on 23/02/2016.
 * 
 * DatabaseHelper extends SQLiteOpenHelper for opening/closing databases.
 */
public class DBController extends SQLiteOpenHelper {
    
    public interface UpdateBlock {
        boolean run(SQLiteDatabase db);
    }

    public interface ResultBlock {
        void run(Cursor c);
    }
    
    // Basic DatabaseHelper data
    public static final int kDBVersion = 1;
    public static final String kDBName = "netnotes.db";
    
    private static final String SQL_CREATE_NOTE = 
            "CREATE TABLE note ("+
            "uniqueID   CHAR(36) PRIMARY KEY,"+
            "text       TEXT NOT NULL DEFAULT \"\","+
            "createDate DATETIME,"+
            "sortDate   DATETIME," + 
            "seqID      BIGINT UNSIGNED)";
    
    private static final String SQL_CREATE_DELETE =
            "CREATE TABLE deleted ("+
            "uniqueID   CHAR(36) PRIMARY KEY,"+
            "seqID      BIGINT UNSIGNED)";
    
    private static final String[] SQL_CREATE = {SQL_CREATE_NOTE, SQL_CREATE_DELETE};
    
    private static final String SQL_DELETE_NOTE = "DROP TABLE note";
    private static final String SQL_DELETE_DELETE = "DROP TABLE deleted";
    
    private static final String[] SQL_DROP = {SQL_DELETE_NOTE, SQL_DELETE_DELETE};
    
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
        for(String query : SQL_CREATE) {
            db.execSQL(query);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        for(String query : SQL_DROP) {
            db.execSQL(query);
        }
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
    
    // Commodity functions
    
    public Cursor fetch(String query, String... params) {
        return getReadableDatabase().rawQuery(query, params);
    }
    
    public void update(String query, Object... params) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        db.execSQL(query, params);
        db.endTransaction();
    }
    
    public boolean updateBlock(UpdateBlock block) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        boolean result = block.run(db);
        if(result) {
            db.setTransactionSuccessful();
        }
        db.endTransaction();
        return result;
    }
    
    // AsyncTask stuff
    
    public static class Update extends AsyncTask<Object, Void, Void> {
        
        String          _query;
        Runnable        _done;
        
        public Update(String query, Runnable done) {
            _query = query;
            _done = done;
        }
        
        @Override
        protected Void doInBackground(Object... params) {
            SQLiteDatabase db = sharedInstance().getWritableDatabase();
            db.beginTransaction();
            db.execSQL(_query, params);
            db.setTransactionSuccessful();
            db.endTransaction();
            return null;
        }
        
        @Override
        protected void onPostExecute(Void param) {
            if(_done != null) {
                _done.run();
            }
        }
    }
    
    public static class Fetch extends AsyncTask<String, Void, Cursor> {
        
        String          _query;
        ResultBlock     _result;
        
        public Fetch(String query, ResultBlock result) {
            _query = query;
            _result = result;
        }
        
        @Override
        protected Cursor doInBackground(String... params) {
            SQLiteDatabase db = sharedInstance().getReadableDatabase();
            return db.rawQuery(_query, params);
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
