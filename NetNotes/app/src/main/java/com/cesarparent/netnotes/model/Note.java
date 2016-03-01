package com.cesarparent.netnotes.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
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
    private String  _uniqueID;


    public Note(String text) {
        _sortDate = new Date();
        _creationDate = new Date();
        _text = text;
        _uniqueID = UUID.randomUUID().toString();
    }

    public Note(JSONObject obj) throws JSONException {
        _text = obj.getString("text");
        //SimpleDateFormat=
        _creationDate = new Date(obj.getString("createDate"));

    }


    public Note detachedCopy() {
        Note copy = new Note(this._text);
        copy._sortDate = (Date)_sortDate.clone();
        copy._creationDate = (Date)_creationDate.clone();
        copy._text = _text;
        copy._uniqueID = _uniqueID;

        return copy;
    }

    public String uniqueID() {
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
