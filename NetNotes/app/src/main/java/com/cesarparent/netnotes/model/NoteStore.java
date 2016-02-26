package com.cesarparent.netnotes.model;

import com.cesarparent.netnotes.sync.SyncDelegate;
import com.cesarparent.utils.WeakMapTable;

import java.util.Set;
import java.util.TreeSet;

/**
 * Created by cesar on 23/02/2016.
 *
 * Main Model manager. provides a way to create, get, edit and delete notes.
 */
public class NoteStore implements SyncDelegate {

    private static NoteStore _instance = new NoteStore();

    private WeakMapTable<String, Note> _notes;

    private Set<String> _ids;

    public static synchronized NoteStore sharedStore() {
        return _instance;
    }

    public NoteStore() {
        _notes = new WeakMapTable<>();
        _ids = new TreeSet<>();
    }

    public void addNote(Note note) {
        _notes.put(note.uniqueID(), note);
        // TODO: Send note to SQLite
        // TODO: Send note to SyncController
    }

    public Note noteWithID(String id) {
        if(!_notes.containsKey(id)) {
           return null;
        }
        return _notes.get(id);
    }

    public int size() {
        return _notes.size();
    }

    /// SyncDelegate implementation

    @Override
    public void shouldLogin() {

    }

    @Override
    public void didLogin() {

    }

    @Override
    public void didReceiveUpdates() {

    }

    @Override
    public void didReceiveDeletes() {

    }

    @Override
    public void didReceiveErrors() {

    }
}
