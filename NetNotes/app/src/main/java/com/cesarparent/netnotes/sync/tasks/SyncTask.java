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
    
    protected final int ID;                 // A random number identifying the task.
    private String _endpoint;               // The API endpoint requested.
    private Sync.ResultCallback _onResult;  // The result callback.

    /**
     * Creates a new SyncTask to a given endpoint, with a given callback.
     * @param endpoint      The API endpoint to request.
     * @param onResult      The callback to run when the response is received.
     */
    public SyncTask(String endpoint, Sync.ResultCallback onResult) {
        _endpoint = endpoint;
        _onResult = onResult;
        
        Random generator = new Random();
        ID = 1+Math.abs(generator.nextInt());
        
        Log.d("Sync", "Starting Sync Task ID#"+ID);
    }

    /**
     * Sends the request to the server.
     * @return  The server response's status.
     */
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
        
        // Start the request:
        // 1. get the transaction ID and fetch changes from the database.
        String transaction = getTransactionID();
        JSONArray changes = getChanges();
        if(changes == null) {
            return Sync.Status.SUCCESS;
        }
        
        // 2. Send the API request. Authorisation is managed by SyncUtils.
        APIRequest req = new APIRequest(_endpoint, transaction);
        req.setAuthtorization(SyncUtils.getAuthToken());
        req.putData(changes);
        final APIResponse res = req.send();

        // If the request was unauthorised, invalidate the saved credentials
        // so the user is prompted to log in again.
        if(res.getStatus() == Sync.Status.FAIL_UNAUTHORIZED) {
            SyncUtils.invalidateCredentials();
            SyncUtils.invalidateSyncDates();
        }
        
        // If the response isn't successful, don't process the body.
        if(res.getStatus() != Sync.Status.SUCCESS) {
            return res.getStatus();
        }

        // Parse the received JSON and dispatch it.
        return processResponseJSON(res) ? Sync.Status.SUCCESS : Sync.Status.FAIL;
    }

    /**
     * Processes the status, and starts a in-memory model refresh if the request was succsful.
     * @param status    The repsonse's status.
     */
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

    /**
     * Checks there is a valid changeset sent with the response, and forwards it to the concrete
     * implementation for processing if there's one.
     * @param res       The server's response.
     * @return  true if the response was processed successfully, false otherwise.
     */
    private boolean processResponseJSON(final APIResponse res) {
        final JSONArray updates = res.getChangeSet();
        if (updates == null) {
            Log.e("SyncUpdateTask", "Invalid response payload");
            return false;
        }
        setTransactionID(res.getTransactionID());
        
        return processResponseData(updates);
    }

    /**
     * Returns the transaction ID based on which endpoint is being requested.
     * @return  The updates or deletes transaction ID.
     */
    private String getTransactionID() {
        if(_endpoint.equals(APIRequest.ENDPOINT_NOTES)) {
            return SyncUtils.getUpdateTransactionID();
        } else {
            return SyncUtils.getDeleteTransactionID();
        }
    }

    /**
     * Saves a transaction ID for the endpoint being requested.
     * @param id        The transaction ID for the current endpoint.
     */
    private void setTransactionID(String id) {
        if(_endpoint.equals(APIRequest.ENDPOINT_NOTES)) {
            SyncUtils.setUpdateTransactionID(id);
        } else {
            SyncUtils.setDeleteTransactionID(id);
        }
    }

    /**
     * Called when the server returns anything but success. Override this to implement
     * behaviours that should be run on fail.
     */
    protected abstract void onFail();

    /**
     * Returns the changeset that will be sent with the request to the server, as a JSON array.
     * @return  A JSON Array containing the changes to send to the server.8
     */
    protected abstract JSONArray getChanges();

    /**
     * Processes the server response's data.
     * @param data          The data extracted from the response.
     * @return  true if the data was processed successfully, false otherwise.
     */
    protected abstract boolean processResponseData(JSONArray data);
}
