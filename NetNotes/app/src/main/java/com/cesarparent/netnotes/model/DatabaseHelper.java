package com.cesarparent.netnotes.model;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import com.cesarparent.netnotes.CPApplication;

/**
 * Created by cesar on 23/02/2016.
 * 
 * DatabaseHelper extends SQLiteOpenHelper for opening/closing databases.
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    
    public static final int kDBVersion = 1;
    public static final String kDBName = "netnotes.db";
    
    private static final String SQL_CREATE_ENTRIES = 
            "CREATE TABLE note ("+
            "uniqueID   CHAR(36) PRIMARY KEY,"+
            "text       TEXT NOT NULL DEFAULT \"\","+
            "createDate DATETIME,"+
            "sortDate   DATETIME)";
    
    private static final String SQL_DELETE_ENTRIES = "DROP TABLE note";
    
    
    public DatabaseHelper() {
        super(CPApplication.getContext(), kDBName, null, kDBVersion);
    }
    
    public static void executeUpdate(SQLiteDatabase db, String query, String... params) {
        db.execSQL(query, params);
    }
    
    @Override
    public void onCreate(SQLiteDatabase db) {
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
}
