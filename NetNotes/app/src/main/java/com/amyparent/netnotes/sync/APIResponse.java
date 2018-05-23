package com.cesarparent.netnotes.sync;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by cesar on 03/03/2016.
 * 
 * APIResponse represents a server response to an API request.
 */
public class APIResponse {
    
    private Sync.Status     _status;        // The response status.
    private String          _transaction;   // The server-sent transaction ID.
    private JSONObject      _body;          // The response payload.

    /**
     * Creates a new empty APIResponse object.
     * @param status        The response status.
     */
    public APIResponse(Sync.Status status) {
        _status = status;
        _body = null;
        _transaction = null;
    }

    /**
     * Creates a new APIResponse with a payload and transaction ID.
     * @param data          The response payload.
     * @param status        The response status.
     * @param transaction   The response transaction ID.
     */
    public APIResponse(@NonNull String data, Sync.Status status, @NonNull String transaction) {
        _status = status;
        try {
            _body = new JSONObject(data);
            Log.d("APIResponse", "Received Payload: " + _body);
        }
        catch(JSONException e) {
            _body = null;
            _status = Sync.Status.FAIL;
        }
        _transaction = transaction;
    }

    /**
     * Returns the transaction ID if there is one set.
     * @return  The response transaction ID, or null if there isn't one.
     */
    @Nullable
    public String getTransactionID() {
        return _transaction;
    }

    /**
     * Returns the response status.
     * @return  The response status.
     */
    public Sync.Status getStatus() {
        return _status;
    }

    /**
     * Returns the response payload.
     * @return  The response payload, or null if there isn't one.
     */
    @Nullable
    public JSONObject getBody() {
        return _body;
    }

    /**
     * Returns the payloads changes set if it contains one.
     * @return  The payload's changes set, or null if there ins't one.
     */
    @Nullable
    public JSONArray getChangeSet() {
        try {
            return _body.getJSONArray("changes");
        }
        catch(JSONException e) {
            return null;
        }
    }
}
