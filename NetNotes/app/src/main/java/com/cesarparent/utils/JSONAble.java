package com.cesarparent.utils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by cesar on 07/03/2016.
 * 
 * Implemented by objects that can be exported to JSON
 */
public interface JSONAble {
    JSONObject toJSON() throws JSONException;
}
