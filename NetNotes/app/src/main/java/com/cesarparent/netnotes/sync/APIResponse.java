package com.cesarparent.netnotes.sync;

import org.json.JSONArray;
import org.json.JSONException;

/**
 * Created by cesar on 03/03/2016.
 * 
 */
public class APIResponse {
    
    public static final int DATA_ERROR =   -1;
    public static final int SUCCESS =       200;
    public static final int BAD_REQUEST =   400;
    public static final int UNAUTHORIZED =  401;
    
    private int _status;
    
    public APIResponse(int status) {
        _status = status;
    }
    
    public APIResponse(JSONArray data) {
        
    }
    
    public APIResponse(String data) {
        
    }
    
    public int getStatus() {
        return _status;
    }
    
    public void getBody() {
        
    }
}
