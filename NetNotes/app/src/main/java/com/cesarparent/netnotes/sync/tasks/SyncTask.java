package com.cesarparent.netnotes.sync.tasks;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;

import com.cesarparent.netnotes.CPApplication;
import com.cesarparent.netnotes.model.Model;
import com.cesarparent.netnotes.sync.APIRequest;
import com.cesarparent.netnotes.sync.APIResponse;
import com.cesarparent.netnotes.sync.SyncUtils;
import com.cesarparent.netnotes.sync.Sync;

import org.json.JSONArray;

import java.util.Random;

/**
 * Created by cesar on 09/03/2016.
 * 
 * Base class for Asynchronous API synchronisation tasks
 */
public abstract class SyncTask extends AsyncTask<Void, Void, Sync.Status> {
    
    protected final int ID;
    private String _endpoint;
    private Sync.ResultCallback _onResult;
    
    public SyncTask(String endpoint, Sync.ResultCallback onResult) {
        _endpoint = endpoint;
        _onResult = onResult;
        
        Random generator = new Random();
        ID = 1+Math.abs(generator.nextInt());
        
        Log.d("Sync", "Starting Sync Task ID#"+ID);
    }

    @Override
    protected Sync.Status doInBackground(Void... params) {
        
        // Check that we have an active network, otherwise abort
        ConnectivityManager cm = (ConnectivityManager)CPApplication
                .getContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        if(info == null || !info.isConnectedOrConnecting()) {
            return Sync.Status.FAIL_NO_NETWORK;
        }
        
        String transaction = getTransactionID();

        JSONArray changes = getChanges(transaction);
        if(changes == null) {
            return Sync.Status.SUCCESS;
        }

        // Send the request and get data back
        APIRequest req = new APIRequest(_endpoint, transaction);
        req.setAuthtorization(SyncUtils.getAuthToken());
        req.putData(changes);
        final APIResponse res = req.send();

        // If Unauthorised, invalidate credentials
        if(res.getStatus() == Sync.Status.FAIL_UNAUTHORIZED) {
            SyncUtils.invalidateCredentials();
            SyncUtils.invalidateSyncDates();
        }
        if(res.getStatus() != Sync.Status.SUCCESS) {
            return res.getStatus();
        }

        // Parse the received JSON
        return processResponseJSON(res) ? Sync.Status.SUCCESS : Sync.Status.FAIL;
    }

    @Override
    protected void onPostExecute(Sync.Status status) {
        Log.d("Sync", "Finishing Sync Task ID#"+ID);
        if(status == Sync.Status.SUCCESS) {
            Model.refresh();
        } else {
            onFail();
        }
        if(_onResult != null) {
            _onResult.onSyncResult(status);
        }
    }
    
    private boolean processResponseJSON(final APIResponse res) {
        final JSONArray updates = res.getChangeSet();
        if (updates == null) {
            Log.e("SyncUpdateTask", "Invalid response payload");
            return false;
        }
        setTransactionID(res.getTransactionID());
        
        return processResponseData(updates, res.getTransactionID());
    }



    private String getTransactionID() {
        if(_endpoint.equals(APIRequest.ENDPOINT_NOTES)) {
            return SyncUtils.getUpdateTransactionID();
        } else {
            return SyncUtils.getDeleteTransactionID();
        }
    }

    private void setTransactionID(String id) {
        if(_endpoint.equals(APIRequest.ENDPOINT_NOTES)) {
            SyncUtils.setUpdateTransactionID(id);
        } else {
            SyncUtils.setDeleteTransactionID(id);
        }
    }
    
    protected abstract void onFail();

    protected abstract JSONArray getChanges(String transaction);

    protected abstract boolean processResponseData(JSONArray data, String transaction);
}
