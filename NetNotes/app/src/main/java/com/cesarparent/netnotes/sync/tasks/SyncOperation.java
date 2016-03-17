package com.cesarparent.netnotes.sync.tasks;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.cesarparent.netnotes.CPApplication;
import com.cesarparent.netnotes.model.Model;
import com.cesarparent.netnotes.sync.APIRequest;
import com.cesarparent.netnotes.sync.APIResponse;
import com.cesarparent.netnotes.sync.Authenticator;
import com.cesarparent.netnotes.sync.Sync;

import org.json.JSONArray;

import java.util.Random;

/**
 * Created by cesar on 09/03/2016.
 * 
 * Base class for Asynchronous API synchronisation tasks
 */
public abstract class SyncOperation extends AsyncTask<Void, Void, Sync.Status> {
    
    protected final int ID;                 // A random number identifying the task.
    private APIRequest.Endpoint _endpoint;  // The API endpoint requested.
    private Sync.ResultCallback _onResult;  // The result callback.

    /**
     * Creates a new SyncOperation to a given endpoint, with a given callback.
     * @param endpoint      The API endpoint to request.
     * @param onResult      The callback to run when the response is received.
     */
    public SyncOperation(APIRequest.Endpoint endpoint, @Nullable Sync.ResultCallback onResult) {
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
            return Sync.Status.FAIL;
        }
        
        // 2. Send the API request. Authorisation is managed by Authenticator.
        APIRequest req = new APIRequest(_endpoint, transaction);
        req.setAuthtorization(Authenticator.getAuthToken());
        req.putData(changes);
        final APIResponse res = req.send();

        // If the request was unauthorised, invalidate the saved credentials
        // so the user is prompted to log in again.
        if(res.getStatus() == Sync.Status.FAIL_UNAUTHORIZED) {
            Authenticator.invalidateCredentials();
            Authenticator.invalidateSyncDates();
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
        Log.d("Sync", "Finishing Sync Operation ID#"+ID);
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
    private boolean processResponseJSON(@NonNull final APIResponse res) {
        final JSONArray updates = res.getChangeSet();
        if (updates == null) {
            Log.e("SyncUpdateOperation", "Invalid response payload");
            return false;
        }
        setTransactionID(res.getTransactionID());
        
        return processResponseData(updates);
    }

    /**
     * Returns the transaction ID based on which endpoint is being requested.
     * @return  The updates or deletes transaction ID.
     */
    @NonNull
    private String getTransactionID() {
        if(_endpoint.equals(APIRequest.Endpoint.NOTES)) {
            return Authenticator.getUpdateTransactionID();
        } else {
            return Authenticator.getDeleteTransactionID();
        }
    }

    /**
     * Saves a transaction ID for the endpoint being requested.
     * @param id        The transaction ID for the current endpoint.
     */
    private void setTransactionID(@NonNull String id) {
        if(_endpoint.equals(APIRequest.Endpoint.NOTES)) {
            Authenticator.setUpdateTransactionID(id);
        } else {
            Authenticator.setDeleteTransactionID(id);
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
    @Nullable
    protected abstract JSONArray getChanges();

    /**
     * Processes the server response's data.
     * @param data          The data extracted from the response.
     * @return  true if the data was processed successfully, false otherwise.
     */
    protected abstract boolean processResponseData(@NonNull JSONArray data);
}
