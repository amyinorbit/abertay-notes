package com.cesarparent.netnotes.model;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.cesarparent.utils.Notification;
import com.cesarparent.utils.NotificationCenter;
import com.cesarparent.utils.Utils;

import java.util.ArrayList;

/**
 * Created by cesar on 05/03/2016.
 */
public class Model {
    
    private ArrayList<NoteHandle> _handles;
    
    private static Model _instance = null;
    
    public static synchronized Model sharedInstance() {
        if(_instance == null) {
            _instance = new Model();
        }
        return _instance;
    }
    
    private Model() {
        _handles = new ArrayList<>();
        _fetchHandles();
    }
    
    public int getNotesCount() {
        return _handles.size();
    }
    
    public NoteHandle getHandleAtIndex(int index) {
        return _handles.get(index);
    }
    
    // TODO: Destroy note handle from the set
    // TODO: Destroy the instance from database, log it in sync transaction
    public void deleteNoteWithUniqueID(String uniqueID) {
        Log.d("Model", "Deleting Note#"+uniqueID);
        final String args[] = { uniqueID };
        DBController.sharedInstance().runUpdate(new DBController.DBUpdateBlock() {
            @Override
            public void run(SQLiteDatabase db) {
                db.execSQL("DELETE FROM note WHERE uniqueID = ?", args);
                db.setTransactionSuccessful();
            }
        });
        _fetchHandles();
        NotificationCenter.defaultCenter().postNotification(Notification.MODEL_UPDATE, null);
    }
    
    // TODO: Implement note fetching
    public Note getNoteWithUniqueID(String uniqueID) {
        
        final String args[] = { uniqueID };
        Cursor c = DBController.sharedInstance().fetch(new DBController.DBFetchBlock() {
            @Override
            public Cursor run(SQLiteDatabase db) {
                return db.rawQuery("SELECT uniqueID, text, createDate, sortDate " +
                                           "FROM note WHERE uniqueID = ?", args);
            }
        });
        if(c.getCount() < 1) { return null; }
        c.moveToFirst();
        System.out.println(c.getString(2));
        return new Note(c.getString(0), c.getString(1), c.getString(2), c.getString(3));
    }
    
    public void addNote(Note note) {
        final String args[] = {
                note.uniqueID(),
                note.text(),
                Utils.JSONDate(note.creationDate()),
                Utils.JSONDate(note.sortDate())
        };
        DBController.sharedInstance().runUpdate(new DBController.DBUpdateBlock() {
            @Override
            public void run(SQLiteDatabase db) {
                db.execSQL("INSERT OR REPLACE INTO note (uniqueID, text, createDate, sortDate)" +
                                   "VALUES (?, ?, ?, ?)", args);
                db.setTransactionSuccessful();
            }
        });
        _fetchHandles();
        NotificationCenter.defaultCenter().postNotification(Notification.MODEL_UPDATE, null);
    }
    
    private void _fetchHandles() {
        
        Cursor c = DBController.sharedInstance().fetch(new DBController.DBFetchBlock() {
            @Override
            public Cursor run(SQLiteDatabase db) {
                return db.rawQuery("SELECT uniqueID, SUBSTR(text, 1, 128)" + 
                                   "FROM note ORDER BY sortDate DESC", null);
            }
        });

        _handles.clear();
        System.out.println("Cursor: "+c.getCount());
        for(int i = 0; i < c.getCount(); ++i) {
            c.moveToPosition(i);
            _handles.add(new NoteHandle(c.getString(0), c.getString(1)));
        }
    }
}
