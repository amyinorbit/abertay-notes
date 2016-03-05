package com.cesarparent.netnotes.sync;

import android.os.AsyncTask;
import android.util.Log;

import com.cesarparent.utils.Utils;
import com.cesarparent.utils.NotificationCenter;

import org.json.JSONException;

/**
 * Created by cesar on 04/03/2016.
 * 
 */
class APILoginTask extends AsyncTask<String, Void, APIResponse> {

    private APITaskDelegate _delegate;
    private String          _email;

    public APILoginTask(APITaskDelegate delegate) {
        _delegate = delegate;
    }

    @Override
    protected APIResponse doInBackground(String[] params) {
        // This isn't a user-side error, so assert 2 or GTFO.
        if(params.length != 2) {
            Log.e("APILoginTask", "Invalid number of parameters given for Login");
            cancel(true);
            System.exit(2);
        }
        _email = params[0];
        APIRequest request = new APIRequest(APIRequest.ENDPOINT_LOGIN, "POST");
        request.setAuthtorization(Utils.HTTPBasicAuth(params[0], params[1]));
        return request.send();
    }

    @Override
    protected void onPostExecute(APIResponse response) {
        if(response.getStatus() == 200) {
            try {
                String token = response.getBody().getString("token");
                SyncController.sharedInstance().getAuthenticator().setCredentials(_email, token);
                NotificationCenter.defaultCenter().postNotification(SyncController.kLoggedInNotification,
                                                                    token);
            }
            catch(JSONException e) {
                Log.e("APILoginTask", "Invalid Response Format");
            }
        } else {
            SyncController.sharedInstance().getAuthenticator().invalidateCredentials();
        }
        _delegate.taskDidReceiveResponse(response);
        Log.d("APILoginTask", "Login status: " + response.getStatus());
    }
}