package com.cesarparent.netnotes.model;

import android.database.sqlite.SQLiteDatabase;
import com.cesarparent.utils.Utils;

/**
 * Created by cesar on 23/02/2016.
 *
 * Main Model manager. provides a way to create, get, edit and delete notes.
 */
public class NoteStore {

    private static NoteStore _instance = new NoteStore();

    private DatabaseController _db;

    public static synchronized NoteStore sharedStore() {
        return _instance;
    }

    public NoteStore() {
        _db = new DatabaseController();
    }

    public void addNote(final Note note) {
        //_notes.put(note.uniqueID(), note);
        //final Note copy = note.detachedCopy();
        _db.runInTransaction(new DatabaseController.UpdateBlock() {
            @Override
            public void execute(SQLiteDatabase db) {
                String sql = "INSERT INTO note (uniqueID, text, sortDate, createDate) VALUES(?, ?, ?, ?)";
                DatabaseHelper.executeUpdate(db, sql,
                                             note.uniqueID(),
                                             note.text(),
                                             Utils.JSONDate(note.sortDate()),
                                             Utils.JSONDate(note.creationDate()));
            }
        });
    }

    public void getNote(String id) {
        
    }
}
