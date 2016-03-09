package com.cesarparent.netnotes.sync.tasks;

import android.os.AsyncTask;
import android.util.Log;
import com.cesarparent.netnotes.model.Model;
import com.cesarparent.netnotes.sync.APIRequest;
import com.cesarparent.netnotes.sync.APIResponse;
import com.cesarparent.netnotes.sync.Authenticator;
import com.cesarparent.netnotes.sync.Sync;

import org.json.JSONArray;

/**
 * Created by cesar on 09/03/2016.
 * 
 * Base class for Asynchronous API synchronisation tasks
 */
public abstract class SyncTask extends AsyncTask<Void, Void, Sync.Status> {
    
    private String _endpoint;
    private Sync.ResultCallback _onResult;
    
    public SyncTask(String endpoint, Sync.ResultCallback onResult) {
        _endpoint = endpoint;
        _onResult = onResult;
    }

    @Override
    protected Sync.Status doInBackground(Void... params) {
        
        String transaction = getTransactionID();

        JSONArray changes = getChanges(transaction);
        if(changes == null) {
            return Sync.Status.SUCCESS;
        }

        // Send the request and get data back
        APIRequest req = new APIRequest(_endpoint, transaction);
        req.setAuthtorization(Authenticator.getAuthToken());
        req.putData(changes);
        final APIResponse res = req.send();

        // If Unauthorised, invalidate credentials
        if(res.getStatus() == APIResponse.UNAUTHORIZED) {
            Authenticator.invalidateCredentials();
            Authenticator.invalidateSyncDates();
            return Sync.Status.FAIL_UNAUTHORIZED;
        }
        if(res.getStatus() != APIResponse.SUCCESS) {
            return Sync.Status.FAIL_BAD_REQUEST;
        }

        // Parse the received JSON
        return processResponseJSON(res) ? Sync.Status.SUCCESS : Sync.Status.FAIL_BAD_REQUEST;
    }

    @Override
    protected void onPostExecute(Sync.Status status) {
        if(status == Sync.Status.SUCCESS) {
            Model.refresh();
        }
        if(_onResult != null) {
            _onResult.run(status);
        }
    }
    
    private boolean processResponseJSON(final APIResponse res) {
        final JSONArray updates = res.getChangeSet();
        if (updates == null) {
            Log.e("SyncUpdateTask", "Invalid response payload");
            return false;
        }
        setTransactionID(res.getTransactionID());
        
        return updates.length() == 0 || processResponseData(updates, res.getTransactionID());
    }



    private String getTransactionID() {
        if(_endpoint.equals(APIRequest.ENDPOINT_NOTES)) {
            return Authenticator.getUpdateTransactionID();
        } else {
            return Authenticator.getDeleteTransactionID();
        }
    }

    private void setTransactionID(String id) {
        if(_endpoint.equals(APIRequest.ENDPOINT_NOTES)) {
            Authenticator.setUpdateTransactionID(id);
        } else {
            Authenticator.setDeleteTransactionID(id);
        }
    }

    protected abstract JSONArray getChanges(String transaction);

    protected abstract boolean processResponseData(JSONArray data, String transaction);
}
