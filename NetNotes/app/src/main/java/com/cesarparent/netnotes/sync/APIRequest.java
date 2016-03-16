package com.cesarparent.netnotes.sync;

import android.util.Log;
import com.cesarparent.netnotes.CPApplication;
import com.cesarparent.netnotes.R;
import org.json.JSONArray;
import org.json.JSONObject;
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
    
    public static final String ENDPOINT_NOTES =     "/notes";
    public static final String ENDPOINT_DELETE =    "/deleted";
    public static final String ENDPOINT_LOGIN =     "/login";
    public static final String ENDPOINT_SIGNUP =    "/signup";
    public static final String ENDPOINT_TOKEN =     "/token";
    
    private HttpURLConnection   _connection;
    private long                _startTime;
    
    public APIRequest(String endpoint, String transaction) {
        try {
            URL url = new URL(CPApplication.string(R.string.api_location)+endpoint);
            _connection = (HttpURLConnection)url.openConnection();
            _connection.setDoOutput(false);
            _connection.setDoInput(true);
            _connection.setUseCaches(false);
            _connection.setRequestMethod("POST");
            _connection.setFixedLengthStreamingMode(0);
            Log.d("APIRequest", "Sync Date: "+transaction);
            _connection.setRequestProperty("X-NetNotes-Transaction", transaction);
            _connection.setRequestProperty("X-NetNotes-DeviceID", CPApplication.getDeviceID());
            _connection.setRequestProperty("Content-Length", "0");
        }
        catch(Exception e) {
            System.exit(2);
        }
        _startTime = System.currentTimeMillis();
    }
    
    public void setAuthtorization(String token) {
        _connection.setRequestProperty("Authorization", token);
    }
    
    public void putData(JSONObject body) {
        _connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        putData(body.toString());
        Log.d("APIRequest", "Request Body: " + body.toString());
    }
    
    
    public void putData(JSONArray body) {
        _connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        putData(body.toString());
        Log.d("APIRequest", "Request Body: " + body.toString());
    }
    
    private void putData(String body) {
        _connection.setDoOutput(true);
        byte[] bytes = body.getBytes();
        _connection.setFixedLengthStreamingMode(bytes.length);

        OutputStream os = null;
        try {
            os = new BufferedOutputStream(_connection.getOutputStream());
            os.write(bytes);
        }
        catch(IOException e) {
            Log.e("APIRequest", "Error writing request body: "+e.getMessage());
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
    
    public APIResponse send() {
        BufferedReader reader = null;
        try {
            _connection.connect();
            int code = _connection.getResponseCode();
            if(code == 200) {
                String line, json = "";
                InputStream is = _connection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(is));
                while((line = reader.readLine()) != null) {
                    json += line + "\n";
                }
                
                String transaction = _connection.getHeaderField("X-NetNotes-Transaction");
                _calculateTimeOffset();
                return new APIResponse(json, Sync.Status.SUCCESS, transaction);
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
            Log.e("APIRequest", "Connection Error: "+e.getMessage());
            e.printStackTrace();
            return new APIResponse(Sync.Status.FAIL_CONNECTION_ERROR);
        }
        finally {
            if(_connection != null) { _connection.disconnect(); }
            try {
                if(reader != null) { reader.close(); }
            }
            catch(IOException e) {
                Log.e("APIRequest", "Error closing an output buffer: "+e.getMessage());
            }
        }
        return new APIResponse(Sync.Status.FAIL);
    }
    
    private void _calculateTimeOffset() {
        // Convert to a millisecond-level timestamp
        long client = (_startTime + System.currentTimeMillis()) / 2;
        long server = Long.parseLong(_connection.getHeaderField("X-NetNotes-Time")) * 1000;
        long offset = server - client;
        SyncUtils.setTimeOffset(offset);
        Log.d("APIRequest", "Calculated time offset: " + offset + "ms");
    }
}
