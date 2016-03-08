package com.cesarparent.netnotes.model;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.cesarparent.utils.Notification;
import com.cesarparent.utils.NotificationCenter;
import com.cesarparent.utils.Utils;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by cesar on 05/03/2016.
 */
public class Model {
    
    public interface NoteCompletionBlock {
        void run(Note note);
    }
    
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
    
    public void deleteNoteWithUniqueID(String uniqueID) {
        Log.d("Model", "Deleting Note#" + uniqueID);
        
        new DBController.Update("DELETE FROM note WHERE uniqueID = ?", new Runnable() {
            @Override
            public void run() {
                _fetchHandles();
            }
        }).execute(uniqueID);
        new DBController.Update("INSERT OR REPLACE INTO deleted (uniqueID, deleteDate) VALUES (?, ?)", null)
                .execute(uniqueID, Utils.JSONDate(new Date()));
    }
    
    // TODO: Implement note fetching
    public void getNoteWithUniqueID(String uniqueID, final NoteCompletionBlock done) {
        
        new DBController.Fetch("SELECT uniqueID, text, createDate, sortDate " +
                                       "FROM note WHERE uniqueID = ?", new DBController.onResult() {
            @Override
            public void run(Cursor c) {
                if (c.getCount() < 1) {
                    return;
                }
                c.moveToFirst();
                done.run(new Note(c.getString(0), c.getString(1), c.getString(2), c.getString(3)));
            }
        }).execute(uniqueID);
    }
    
    public void addNote(final Note note) {
        final String args[] = {
                note.uniqueID(),
                note.text(),
                Utils.JSONDate(note.creationDate()),
                Utils.JSONDate(note.sortDate())
        };
        new DBController.Update("INSERT OR REPLACE INTO note" +
                                "(uniqueID, text, createDate, sortDate)" + 
                                "VALUES (?, ?, ?, ?)", new Runnable() {
            @Override
            public void run() {
                Log.d("Model", "Insert finished: " + note);
                _fetchHandles();
            }
        }).execute(note.uniqueID(),
                   note.text(),
                   Utils.JSONDate(note.creationDate()),
                   Utils.JSONDate(note.sortDate())
        );
    }
    
    public void flushDeleted() {
        new DBController.Update("DELETE FROM deleted", null).execute();
    }
    
    private void _fetchHandles() {
        
        new DBController.Fetch("SELECT uniqueID, SUBSTR(text, 1, 128) FROM note ORDER BY sortDate DESC",
                               new DBController.onResult() {
            @Override
            public void run(Cursor c) {
                _handles.clear();
                for (int i = 0; i < c.getCount(); ++i) {
                    c.moveToPosition(i);
                    _handles.add(new NoteHandle(c.getString(0), c.getString(1)));
                }
                NotificationCenter.defaultCenter().postNotification(Notification.MODEL_UPDATE, null);
            }
        }).execute();
    }
}
