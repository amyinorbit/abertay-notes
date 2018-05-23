package com.cesarparent.netnotes.model;

import android.database.Cursor;
import android.support.annotation.NonNull;
import android.util.Log;

import com.cesarparent.utils.NotificationCenter;
import com.cesarparent.netnotes.sync.Sync;
import com.cesarparent.utils.Notification;

import java.util.ArrayList;

/**
 * Created by cesar on 05/03/2016.
 * 
 * Model provides an API to access and modify the user's collection of notes stored on the local
 * device.
 */
public class Model {
    
    /// Callback used to return notes from asynchronous database calls.
    public interface NoteCompletionBlock {
        void run(Note note);
    }
    
    /// The list of notes handles, used to return a list of available notes.
    private static ArrayList<NoteHandle> _handles = new ArrayList<>();

    /**
     * Returns the number of notes in the collection.
     * @return  The number of notes in the collection.
     */
    public static int getNotesCount() {
        return _handles.size();
    }
    
    /**
     * Returns a specific note's handle.
     * @param index The index in the handle list.
     * @return      The note handle at index.
     */
    public static NoteHandle getHandleAtIndex(int index) {
        return _handles.get(index);
    }

    /**
     * Deletes a note from the local connection, and trigger a sync to push changes to the server.
     * @param uniqueID  The note's ID.
     */
    public static void deleteNoteWithUniqueID(@NonNull String uniqueID) {
        Log.d("Model", "Deleting Note#" + uniqueID);
        
        new DBController.Update("DELETE FROM note WHERE uniqueID = ?", null).execute(uniqueID);
        
        // deletedNote inserted with sequence ID -1 to mark it for the next sync transaction
        new DBController.Update("INSERT OR REPLACE INTO deleted (uniqueID, seqID) VALUES (?, ?)", new Runnable() {
            @Override
            public void run() {
                refresh();
                Sync.refresh();
            }
        }).executeOnExecutor(DBController.SERIAL_QUEUE,
                             uniqueID,
                             -1);
    }
    
    /**
     * Fetches a complete note for a given unique ID.
     * @param uniqueID  The note's ID.
     * @param done      The callback called with the note.
     */
    public static void getNoteWithUniqueID(@NonNull String uniqueID,
                                           @NonNull final NoteCompletionBlock done) {
        
        new DBController.Fetch("SELECT uniqueID, text, createDate, sortDate " +
                                       "FROM note WHERE uniqueID = ?", new DBController.ResultBlock() {
            @Override
            public void run(Cursor c) {
                if (c.getCount() < 1) {
                    return;
                }
                c.moveToFirst();
                done.run(new Note(c.getString(0), c.getString(1), c.getString(2), c.getString(3)));
            }
        }).executeOnExecutor(DBController.SERIAL_QUEUE, uniqueID);
    }

    /**
     * Adds a note to the collection.
     * @param note  The note to add to the collection.
     */
    public static void addNote(@NonNull final Note note) {
        new DBController.Update("INSERT OR REPLACE INTO note" +
                                "(uniqueID, text, createDate, sortDate, seqID)" + 
                                "VALUES (?, ?, ?, ?, ?)", new Runnable() {
            @Override
            public void run() {
                Log.d("Model", "Insert finished: " + note);
                refresh();
                Sync.refresh();
            }
        }).executeOnExecutor(DBController.SERIAL_QUEUE,
                             note.uniqueID(),
                             note.text(),
                             note.creationDate(),
                             note.sortDate(),
                             -1
        ); // Note added with Sequence ID -1, to mark it for the next sync update.
    }

    /**
     * Clear the list of deleted notes, used to start with a "clean slate" when logging in a new
     * account.
     */
    public static void flushDeleted() {
        new DBController.Update("DELETE FROM deleted", null)
                .executeOnExecutor(DBController.SERIAL_QUEUE);
        new DBController.Update("UPDATE note SET seqID = -1", null)
                .executeOnExecutor(DBController.SERIAL_QUEUE);
    }

    /**
     * Refresh the model to be up-to-date with the on-device database.
     */
    public static void refresh() {
        new DBController.Fetch("SELECT uniqueID, SUBSTR(text, 1, 128) FROM note ORDER BY sortDate DESC",
                               new DBController.ResultBlock() {
            @Override
            public void run(Cursor c) {
                _handles.clear();
                for (int i = 0; i < c.getCount(); ++i) {
                    c.moveToPosition(i);
                    _handles.add(new NoteHandle(c.getString(0), c.getString(1)));
                }
                NotificationCenter.postNotification(Notification.MODEL_UPDATE);
            }
        }).executeOnExecutor(DBController.SERIAL_QUEUE);
    }
}
