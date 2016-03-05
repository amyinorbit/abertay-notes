package com.cesarparent.netnotes.sync;

import android.os.AsyncTask;
import android.util.Log;

import com.cesarparent.netnotes.model.Note;

import org.json.JSONArray;
import org.json.JSONException;

/**
 * Created by cesar on 04/03/2016.
 * 
 */
class APIJSONTask extends AsyncTask<Note, Void, APIResponse> {

    private APITaskDelegate _delegate;
    private String          _endpoint;
    private String          _token;

    public APIJSONTask(String endpoint, String token, APITaskDelegate delegate) {
        _delegate = delegate;
        _endpoint = endpoint;
        _token = token;
    }

    @Override
    protected APIResponse doInBackground(Note[] params) {
        
        APIRequest request = new APIRequest(_endpoint, "POST");
        request.setAuthtorization(_token);
        // Do stuff with notes.
        if(params.length > 0) {
            JSONArray body = new JSONArray();
            try {
                for(Note note : params) {
                    body.put(note.toJSON());
                }
                request.putData(body);
            }
            catch (JSONException e) {
                Log.e("APIJSONTask", "Error creating JSON request body" + e.getMessage());
                cancel();
            }
        }
        return request.send();
    }
    
    public boolean cancel() {
        _delegate.taskWasCancelled();
        return super.cancel(true);
    }

    @Override
    protected void onPostExecute(APIResponse response) {
        _delegate.taskDidReceiveResponse(response);
    }
}