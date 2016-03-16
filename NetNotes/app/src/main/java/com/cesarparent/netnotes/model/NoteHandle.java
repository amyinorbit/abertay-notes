package com.cesarparent.netnotes.model;

import com.cesarparent.utils.Utils;

/**
 * Created by cesar on 05/03/2016.
 * 
 * POJO object used to avoid loading the entire note database in memory. Displayed in the main
 * list.
 */
public class NoteHandle {
    
    public final String uniqueID;     // The note's unique ID.
    public final String title;        // The note's truncated contents.

    /**
     * Creates a new Note handle.
     * @param uniqueID  The note's unique ID.
     * @param contents  The note's contents, will be truncated to 128 characters.
     */
    public NoteHandle(String uniqueID, String contents) {
        this.uniqueID = uniqueID;
        this.title = Utils.safeSubString(contents, 0, 127);
    }
    
}
