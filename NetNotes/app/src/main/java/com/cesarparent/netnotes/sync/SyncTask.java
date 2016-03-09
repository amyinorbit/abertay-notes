package com.cesarparent.netnotes.sync;

import android.os.AsyncTask;
import android.util.Log;
import com.cesarparent.netnotes.model.Model;
import org.json.JSONArray;

/**
 * Created by cesar on 09/03/2016.
 * 
 * Base class for Asynchronous API synchronisation tasks
 */
public abstract class SyncTask extends AsyncTask<Void, Void, Boolean> {
    
    private String _endpoint;
    
    public SyncTask(String endpoint) {
        _endpoint = endpoint;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        
        String transaction = getTransactionID();

        JSONArray changes = getChanges(transaction);
        if(changes == null) {
            return false;
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
            return false;
        }
        if(res.getStatus() != APIResponse.SUCCESS) {
            return false;
        }

        // Parse the received JSON
        return processResponseJSON(res);
    }

    @Override
    protected void onPostExecute(Boolean refresh) {
        if(refresh) {
            Model.refresh();
        }
    }
    
    private boolean processResponseJSON(final APIResponse res) {
        final JSONArray updates = res.getChangeSet();
        if (updates == null) {
            Log.e("SyncUpdateTask", "Invalid response payload");
            return false;
        }
        setTransactionID(res.getTransactionID());
        return updates.length() != 0 && processResponseData(updates, res.getTransactionID());
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
