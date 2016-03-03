package com.cesarparent.netnotes.model;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.cesarparent.netnotes.CPApplication;

/**
 * Created by cesar on 23/02/2016.
 * 
 * DatabaseHelper extends SQLiteOpenHelper for opening/closing databases.
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    
    public static final int kDBVersion = 1;
    public static final String kDBName = "netnotes.db";
    
    public DatabaseHelper() {
        super(CPApplication.getContext(), kDBName, null, kDBVersion);
    }
    
    @Override
    public void onCreate(SQLiteDatabase db) {
        
    }
    
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        
    }
    
    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        
    }
}
