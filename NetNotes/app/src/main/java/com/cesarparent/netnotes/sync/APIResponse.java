package com.cesarparent.netnotes.sync;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by cesar on 03/03/2016.
 * 
 */
public class APIResponse {
    
    private Sync.Status     _status;
    private String          _transaction;
    private JSONObject      _body;
    
    public APIResponse(Sync.Status status) {
        _status = status;
        _body = null;
    }
    
    public APIResponse(String data, Sync.Status status, String time) {
        _status = status;
        try {
            _body = new JSONObject(data);
            Log.d("APIResponse", "Received Payload: " + _body);
        }
        catch(JSONException e) {
            _body = null;
            _status = Sync.Status.FAIL;
        }
        _transaction = time;
    }
    
    public String getTransactionID() {
        return _transaction;
    }
    
    public Sync.Status getStatus() {
        return _status;
    }
    
    public JSONObject getBody() {
        return _body;
    }
    
    public JSONArray getChangeSet() {
        try {
            return _body.getJSONArray("changes");
        }
        catch(JSONException e) {
            return null;
        }
    }
}
