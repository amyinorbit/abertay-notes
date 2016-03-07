package com.cesarparent.netnotes.model;

/**
 * Created by cesar on 05/03/2016.
 * 
 * POJO object used to avoid loading the entire note database in memory. Displayed in the main
 * list.
 */
public class NoteHandle {
    
    public String uniqueID;
    
    public String title;
    
    public NoteHandle(String uniqueID, String title) {
        this.uniqueID = uniqueID;
        this.title = title;
    }
    
}
