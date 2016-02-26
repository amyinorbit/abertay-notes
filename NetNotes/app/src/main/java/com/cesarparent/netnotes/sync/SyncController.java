package com.cesarparent.netnotes.sync;

import com.cesarparent.netnotes.model.Note;

import java.lang.ref.WeakReference;

/**
 * Created by cesar on 23/02/2016.
 *
 *
 */
public class SyncController {

    private static SyncController _instance = null;
    private WeakReference _delegate;

    public synchronized SyncController sharedInstance() {
        if(_instance == null) {
            _instance = new SyncController();
        }
        return _instance;
    }

    private SyncController() {
    }

    public void setDelegate(SyncDelegate delegate) {
        _delegate = new WeakReference<>(delegate);
    }

    public void postNote(Note note) {
        // Get a note that can be used on other threads.
        Note copy = note.detachedCopy();
    }

    public void deleteNote(Note note) {
        String uuid = new String(note.uniqueID());
    }
}