package com.cesarparent.netnotes.model;

import com.cesarparent.utils.Utils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.text.ParseException;
import java.util.Date;
import java.util.UUID;

/**
 * Created by cesar on 15/02/2016.
 *
 * Represents a note, that can be stored in the application's database and synchronised
 * with the server.
 */
public class Note implements Serializable {
    
    private String  _text;              // The note's contents.
    private Date    _creationDate;      // The note's creation date.
    private Date    _sortDate;          // The note's last-modified date.
    private String  _uniqueID;          // The note's unique ID.

    /**
     * Creates a new Note from existing date.
     * @param uniqueID      The note's unique ID.
     * @param text          The note's contents.
     * @param createDate    The note's creation date.
     * @param sortDate      The note's last-modified date.
     */
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

    /**
     * Creates a new Note with given content.
     * @param text          The note's contents.
     */
    public Note(String text) {
        _sortDate = new Date();
        _creationDate = new Date();
        _text = text;
        _uniqueID = UUID.randomUUID().toString();
    }

    /**
     * Creates a new empty Note.
     */
    public Note() {
        this("");
    }

    /**
     * Creates a new Note from existing JSON data.
     * @param obj               The JSONObject containing the data.
     * @throws JSONException    If the JSONObject doesn't contain the right data.
     * @throws ParseException   If a JSON date is of an invalid format.
     */
    public Note(JSONObject obj) throws JSONException, ParseException {
        _text = obj.getString("text");
        _creationDate = Utils.dateFromJSON(obj.getString("createDate"));
        _sortDate = Utils.dateFromJSON(obj.getString("sortDate"));
        _uniqueID = obj.getString("uniqueID");
    }

    /**
     * Returns a string representation of the note for logging.
     * @return  A string representation of the note for logging.
     */
    public String toString() {
        return "Note#"+_uniqueID+" { "+_text+" }";
    }

    /**
     * Returns a JSON representation of the note.
     * @return  A JSON representation of the note.
     * @throws JSONException
     */
    public JSONObject toJSON() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("uniqueID", _uniqueID);
        obj.put("text", _text);
        obj.put("createDate", creationDate());
        obj.put("sortDate", sortDate());
        return obj;
    }

    /**
     * Returns a handle pointing to this note.
     * @return  A handle pointing to this note.
     */
    public NoteHandle getHandle() {
        return new NoteHandle(_uniqueID, _text);
    }

    /**
     * Returns the note's unique ID.
     * @return  The note's unique ID.
     */
    public String uniqueID() {
        return _uniqueID;
    }

    /**
     * Returns the note's creation date as a string.
     * @return  The note's creation date as a string.
     */
    public String creationDate() {
        return Utils.JSONDate(_creationDate);
    }

    /**
     * Returns the note's last-modified date as a string.
     * @return  The note's last-modified date as a string.
     */
    public String sortDate() {
        return Utils.JSONDate(_sortDate);
    }

    /**
     * Returns the note's contents.
     * @return  The note's contents.
     */
    public String text() {
        return _text;
    }

    /**
     * Sets the note's contents, and changes the last-modified date to now.
     * @param text          The note's contents.
     */
    public void setText(String text) {
        _text = text;
        _sortDate = new Date();
    }

}
