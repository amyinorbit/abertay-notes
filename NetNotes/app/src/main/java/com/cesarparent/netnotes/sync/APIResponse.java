package com.cesarparent.netnotes.sync;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by cesar on 03/03/2016.
 * 
 */
public class APIResponse {
    
    public static final int INVALID_STATUS =    -3;
    public static final int CONNECTION_ERROR =  -2;
    public static final int DATA_ERROR =        -1;
    public static final int SUCCESS =           200;
    public static final int BAD_REQUEST =       400;
    public static final int UNAUTHORIZED =      401;
    public static final int SERVER_ERROR =      500;
    
    private int _status;
    
    private JSONObject _body;
    
    public APIResponse(int status) {
        _status = status;
        _body = null;
    }
    
    public APIResponse(JSONObject data, int status) {
        _status = status;
        _body = data;
    }
    
    public APIResponse(String data, int status) {
        _status = status;
        try {
            _body = new JSONObject(data);
        }
        catch(JSONException e) {
            _body = null;
            _status = DATA_ERROR;
        }
    }
    
    public int getStatus() {
        return _status;
    }
    
    public JSONObject getBody() {
        return _body;
    }
}
