package com.cesarparent.netnotes.sync.tasks;

import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import com.cesarparent.netnotes.CPApplication;
import com.cesarparent.netnotes.model.Model;
import com.cesarparent.netnotes.push.PushTokenService;
import com.cesarparent.netnotes.sync.APIRequest;
import com.cesarparent.netnotes.sync.APIResponse;
import com.cesarparent.netnotes.sync.Authenticator;
import com.cesarparent.netnotes.sync.Sync;
import com.cesarparent.utils.Notification;
import com.cesarparent.utils.Utils;
import com.cesarparent.utils.NotificationCenter;
import org.json.JSONException;

/**
 * Created by cesar on 04/03/2016.
 * 
 */
public class APILoginTask extends AsyncTask<String, Void, APIResponse> {

    private Sync.ResultCallback _onResult;
    private String              _email;

    public APILoginTask(Sync.ResultCallback onResult) {
        _onResult = onResult;
    }

    @Override
    protected APIResponse doInBackground(String... params) {
        // This isn't a user-side error, so assert 2 or GTFO.
        if(params.length != 2) {
            Log.e("APILoginTask", "Invalid number of parameters given for Login");
            cancel(true);
            System.exit(2);
        }
        _email = params[0];
        APIRequest request = new APIRequest(APIRequest.ENDPOINT_LOGIN, "0");
        request.setAuthtorization(Utils.HTTPBasicAuth(params[0], params[1]));
        return request.send();
    }

    @Override
    protected void onPostExecute(APIResponse response) {
        Authenticator.invalidateSyncDates();
        Model.flushDeleted();
        if(response.getStatus() == 200) {
            try {
                String token = response.getBody().getString("token");
                Authenticator.setCredentials(_email, token);
                NotificationCenter.defaultCenter().postNotification(Notification.LOGIN_SUCCESS,
                                                                    token);
                callback(Sync.Status.SUCCESS);
            }
            catch(JSONException e) {
                callback(Sync.Status.FAIL_BAD_REQUEST);
                Log.e("APILoginTask", "Invalid Response Format");
            }
        } else {
            Authenticator.invalidateCredentials();
            NotificationCenter.defaultCenter().postNotification(Notification.LOGIN_FAIL,
                                                                null);
            Log.e("APILoginTask", "Invalid Response Format");
            callback(Sync.Status.FAIL_UNAUTHORIZED);
        }
    }
    
    private void callback(Sync.Status status) {
        if(_onResult != null) {
            _onResult.run(status);
        }
    }
}
