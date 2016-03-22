package com.cesarparent.netnotes.sync;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import com.cesarparent.netnotes.CPApplication;
import com.cesarparent.netnotes.R;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by cesar on 03/03/2016.
 * 
 * Wrapper around  HttpURLConnection that handles requests to the sync server.
 */
public class APIRequest {

    /**
     * Defines the valid API endpoints that can be requested.
     */
    public enum Endpoint {
        NOTES   ("/notes"),
        DELETE  ("/deleted"),
        LOGIN   ("/login"),
        SIGNUP  ("/signup"),
        TOKEN   ("/token");
        
        private String _url;    // The endpoint's URL.
        
        /**
         * Creates an endpoint.
         * @param url   The endpoibt's URL.
         */
        Endpoint(String url) {
            _url = url;
        }

        /**
         * Returns the string representation for that endpoint.
         * @return  The string representation for that endpoint.
         */
        public String toString() {
            return _url;
        }
    }
    
    private HttpURLConnection   _connection;    // The connection used to send the request.
    private long                _startTime;     // The UNIX timestamp at which the request was sent.
    private String              _body;          // The body's data;

    /**
     * Creates a new APIRequest.
     * @param endpoint      The API endpoint to send the request to.
     * @param transaction   The last transaction ID for the requested endpoint.
     */
    public APIRequest(Endpoint endpoint, @NonNull String transaction) {
        try {
            URL url = new URL(CPApplication.string(R.string.api_location)+endpoint);
            _connection = (HttpURLConnection)url.openConnection();
            _connection.setDoOutput(false);
            _connection.setDoInput(true);
            _connection.setUseCaches(false);
            _connection.setRequestMethod("POST");
            _body = null;
            
            // This is needed to prevent sending garbage when there is no request payload.
            _connection.setFixedLengthStreamingMode(0);
            _connection.setRequestProperty("X-NetNotes-Transaction", transaction);
            _connection.setRequestProperty("X-NetNotes-DeviceID", CPApplication.getDeviceID());
            _connection.setRequestProperty("Content-Length", "0");
        }
        catch(Exception e) {
            System.exit(2);
        }
        _startTime = System.currentTimeMillis();
    }

    /**
     * Sets the request authorization token.
     * @param token     The request authorization token.
     */
    public void setAuthtorization(@Nullable String token) {
        if(token == null) { return; }
        _connection.setRequestProperty("Authorization", token);
    }

    /**
     * Writes an Object to the request body.
     * @param body  The request body.
     */
    public void putData(@NonNull Object body) {
        _connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        _body = body.toString();
        Log.d("APIRequest", "Request Body: " + body.toString());
    }

    /**
     * Writes a String to the request body.
     */
    private void writeData() {
        if(_body == null) { return; }
        
        _connection.setDoOutput(true);
        byte[] bytes = _body.getBytes();
        _connection.setFixedLengthStreamingMode(bytes.length);

        OutputStream os = null;
        try {
            os = new BufferedOutputStream(_connection.getOutputStream());
            os.write(bytes);
        }
        catch(IOException e) {
            Log.e("APIRequest", "Error writing request body: " + e.getMessage());
        }
        finally { // This is really ugly. Thanks, Java and your sh***y Exception system.
            try {
                if(os != null) { os.close(); }
            }
            catch(IOException e){
                Log.e("APIRequest", "Error closing an output buffer: "+e.getMessage());
            }
        }
    }

    /**
     * Sends the requests to the server and returns the server response. This runs synchronously
     * and should *never* be called on the UI thread.
     * @return  The server response.
     */
    @NonNull
    public APIResponse send() {
        
        // Check that we have an active network, otherwise abort
        ConnectivityManager cm = (ConnectivityManager)CPApplication
                .getContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        if(info == null || !info.isConnectedOrConnecting()) {
            return new APIResponse(Sync.Status.FAIL_NO_NETWORK);
        }
        
        try {
            writeData();
            _connection.connect();
            int code = _connection.getResponseCode();
            if(code == 200) {
                String line, json = "";
                InputStream is = _connection.getInputStream();
                
                // More try/catch, this time with resources
                try(BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                    while((line = reader.readLine()) != null) {
                        json += line + "\n";
                    }

                    String transaction = _connection.getHeaderField("X-NetNotes-Transaction");
                    _calculateTimeOffset();
                    return new APIResponse(json, Sync.Status.SUCCESS, transaction);
                }
                catch(IOException e) {
                    Log.e("APIRequest", "Error reading server response: " + e.getMessage());
                    return new APIResponse(Sync.Status.FAIL);
                }
            }
            else if(code == 401) {
                return new APIResponse(Sync.Status.FAIL_UNAUTHORIZED);
            }
            else if(code == 409) {
                return new APIResponse(Sync.Status.FAIL_CONFLICT);
            }
            else if(code > 401 && code < 500) {
                return new APIResponse(Sync.Status.FAIL_BAD_REQUEST);
            }
            else if(code >= 500) {
                return new APIResponse(Sync.Status.FAIL_SERVER_ERROR);
            }
            
        }
        catch(IOException e) {
            Log.e("APIRequest", "Connection Error: " + e.getMessage());
            return new APIResponse(Sync.Status.FAIL_CONNECTION_ERROR);
        }
        finally {
            if(_connection != null) { _connection.disconnect(); }
        }
        return new APIResponse(Sync.Status.FAIL);
    }

    /**
     * Calculates the time offset between the phone's clock and the server. The server time
     * is assumed to have been polled request_duration/2 seconds ago.
     * This is necessary because note merging is clock-dependant on the server side.
     */
    private void _calculateTimeOffset() {
        // Check that the server DID return the time.
        String timeString = _connection.getHeaderField("X-NetNotes-Time");
        if(timeString == null) {
            return;
        }
        // Convert to a millisecond-level timestamp
        long client = (_startTime + System.currentTimeMillis()) / 2;
        long server = Long.parseLong(_connection.getHeaderField("X-NetNotes-Time")) * 1000;
        long offset = server - client;
        Authenticator.setTimeOffset(offset);
        Log.d("APIRequest", "Calculated time offset: " + offset + "ms");
    }
}
