package com.cesarparent.netnotes.model;

import com.cesarparent.netnotes.sync.JSONAble;
import com.cesarparent.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

/**
 * Created by cesar on 15/02/2016.
 *
 * Represents a note, that can be stored in the application's database and synchronised
 * with the server.
 */
public class Note implements JSONAble {
    
    //public static final String 

    private String  _text;
    private Date    _creationDate;
    private Date    _sortDate;
    private String  _uniqueID;

    public Note(String uniqueID, String text, String createDate, String sortDate) {
        _uniqueID = uniqueID;
        _text = text;
        try {
            _creationDate = Utils.dateFromJSON(createDate);
            _sortDate = Utils.dateFromJSON(sortDate);
        }
        catch(ParseException e) {
            _creationDate = new Date();
            _sortDate = new Date();
        }
    }

    public Note(String text) {
        _sortDate = new Date();
        _creationDate = new Date();
        _text = text;
        _uniqueID = UUID.randomUUID().toString();
    }

    public Note(JSONObject obj) throws JSONException, ParseException {
        _text = obj.getString("text");
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZZZ", Locale.UK);
        _creationDate = format.parse(obj.getString("createDate"));
        _sortDate = format.parse(obj.getString("sortDate"));
        _uniqueID = obj.getString("uniqueID");
    }
    
    public String toString() {
        return "Note#"+_uniqueID+" { "+_text+" }";
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject obj = new JSONObject();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZZZ", Locale.UK);
        obj.put("uniqueID", _uniqueID);
        obj.put("text", _text);
        obj.put("createDate", format.format(_creationDate));
        obj.put("sortDate", format.format(_sortDate));
        return obj;
    }
    
    public NoteHandle getHandle() {
        return new NoteHandle(_uniqueID, _text.substring(0, 128)+"…");
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
