package com.cesarparent.netnotes.model;

import android.database.Cursor;
import android.util.Log;

import com.cesarparent.netnotes.sync.Authenticator;
import com.cesarparent.utils.Notification;
import com.cesarparent.utils.NotificationCenter;
import com.cesarparent.utils.Utils;

import java.util.ArrayList;

/**
 * Created by cesar on 05/03/2016.
 * 
 * Model class used to access
 */
public class Model {
    
    public interface NoteCompletionBlock {
        void run(Note note);
    }
    
    private static ArrayList<NoteHandle> _handles = new ArrayList<>();
    
    public static int getNotesCount() {
        return _handles.size();
    }
    
    public static NoteHandle getHandleAtIndex(int index) {
        return _handles.get(index);
    }
    
    public static void deleteNoteWithUniqueID(String uniqueID) {
        Log.d("Model", "Deleting Note#" + uniqueID);
        
        new DBController.Update("DELETE FROM note WHERE uniqueID = ?", Model::refresh).execute(uniqueID);
        new DBController.Update("INSERT OR REPLACE INTO deleted (uniqueID, seqID) VALUES (?, ?)", null)
                .execute(uniqueID,
                         Authenticator.getDeleteTransactionID()+1);
    }
    
    public static void getNoteWithUniqueID(String uniqueID, final NoteCompletionBlock done) {
        
        new DBController.Fetch("SELECT uniqueID, text, createDate, sortDate " +
                                       "FROM note WHERE uniqueID = ?", c -> {
           if (c.getCount() < 1) {
               return;
           }
           c.moveToFirst();
           done.run(new Note(c.getString(0), c.getString(1), c.getString(2), c.getString(3)));
        }).execute(uniqueID);
    }
    
    public static void addNote(final Note note) {
        new DBController.Update("INSERT OR REPLACE INTO note" +
                                "(uniqueID, text, createDate, sortDate, seqID)" + 
                                "VALUES (?, ?, ?, ?, ?)", () -> {
            Log.d("Model", "Insert finished: " + note);
            refresh();
        }).execute(note.uniqueID(),
                   note.text(),
                   Utils.JSONDate(note.creationDate()),
                   Utils.JSONDate(note.sortDate()),
                   (Authenticator.getUpdateTransactionID()+1)
        );
    }
    
    public static void flushDeleted() {
        new DBController.Update("DELETE FROM deleted", null).execute();
    }
    
    public static void refresh() {
        
        new DBController.Fetch("SELECT uniqueID, SUBSTR(text, 1, 128) FROM note ORDER BY sortDate DESC",
                               c -> {
            _handles.clear();
            for (int i = 0; i < c.getCount(); ++i) {
               c.moveToPosition(i);
                _handles.add(new NoteHandle(c.getString(0), c.getString(1)));
            }
            NotificationCenter.defaultCenter().postNotification(Notification.MODEL_UPDATE, null);
        }).execute();
    }
}
