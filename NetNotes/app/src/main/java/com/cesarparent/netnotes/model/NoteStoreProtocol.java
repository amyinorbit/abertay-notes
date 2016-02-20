package com.cesarparent.netnotes.model;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by cesar on 15/02/2016.
 *
 * Defines the interface that objects storing notes must conform to.
 */
public interface NoteStoreProtocol {

    Note noteWithUUID(UUID id);

    ArrayList<Note> sortedNotes();

    void registerNoteChanges(Note note);
}
