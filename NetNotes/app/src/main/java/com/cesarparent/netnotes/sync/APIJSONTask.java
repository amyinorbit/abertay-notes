package com.cesarparent.netnotes.sync;

import android.os.AsyncTask;
import android.util.Log;

import com.cesarparent.utils.JSONAble;

import org.json.JSONArray;
import org.json.JSONException;

/**
 * Created by cesar on 04/03/2016.
 * 
 */
class APIJSONTask extends AsyncTask<Object, Void, APIResponse> {

    private APITaskDelegate _delegate;
    private String          _endpoint;
    private String          _token;
    private String          _transaction;

    public APIJSONTask(String endpoint, String token, String transaction, APITaskDelegate delegate) {
        _delegate = delegate;
        _endpoint = endpoint;
        _token = token;
        _transaction = transaction;
    }

    @Override
    protected APIResponse doInBackground(Object... params) {
        
        APIRequest request = new APIRequest(_endpoint, _transaction);
        request.setAuthtorization(_token);
        // Do stuff with notes.
        JSONArray body = new JSONArray();
        if(params.length > 0) {
            try {
                for(Object obj : params) {
                    if(obj instanceof JSONAble) {
                        body.put(((JSONAble) obj).toJSON());
                    } else {
                        body.put(obj.toString());
                    }
                }
            }
            catch (JSONException e) {
                Log.e("APIJSONTask", "Error creating JSON request body" + e.getMessage());
                cancel();
            }
        }
        request.putData(body);
        return request.send();
    }
    
    public boolean cancel() {
        _delegate.taskWasCancelled();
        return super.cancel(true);
    }

    @Override
    protected void onPostExecute(APIResponse response) {
        Log.d("APIJSONTask", "Received response: " + response.getStatus());
        if(response.getBody() != null) {
            Log.d("APIJSONTask", "Response body: " + response.getBody().toString());
        }
        _delegate.taskDidReceiveResponse(response);
    }
}