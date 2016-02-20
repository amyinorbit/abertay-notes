package com.cesarparent.netnotes.model;

import java.util.Date;
import java.util.UUID;

/**
 * Created by cesar on 15/02/2016.
 *
 * Represents a note, that can be stored in the application's database and synchronised
 * with the server.
 */
public class Note {

    private String  _text;
    private Date    _creationDate;
    private Date    _sortDate;
    private UUID    _uniqueID;


    public Note(String text) {
        _sortDate = new Date();
        _creationDate = new Date();
        _text = text;
        _uniqueID = UUID.randomUUID();
    }

    public UUID uniqueID() {
        return _uniqueID;
    }

    public Date creationDate() {
        return _creationDate;
    }

    public Date sortDate() {
        return _sortDate;
    }

    public String text() {
        return _text;
    }

    public void setText(String text) {
        _text = text;
        _sortDate = new Date();
    }

}
