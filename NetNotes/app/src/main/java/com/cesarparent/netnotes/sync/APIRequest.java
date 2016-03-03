package com.cesarparent.netnotes.sync;

import android.util.Log;

import com.cesarparent.netnotes.CPApplication;
import com.cesarparent.netnotes.R;
import com.cesarparent.netnotes.views.Utils;

import org.json.JSONArray;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;

/**
 * Created by cesar on 03/03/2016.
 * 
 * Wrapper around  HttpURLConnection that handles requests to the sync server.
 */
public class APIRequest {
    
    public static final String ENDPOINT_NOTES =     "/notes";
    public static final String ENDPOINT_DELETE =    "/deleted";
    public static final String ENDPOINT_LOGIN =     "/login";
    
    private HttpURLConnection _connection;
    
    public APIRequest(String endpoint, String method) {
        try {
            URL url = new URL(CPApplication.string(R.string.api_location)+endpoint);
            _connection = (HttpURLConnection)url.openConnection();
            _connection.setDoOutput(true);
            _connection.setDoInput(true);
            _connection.setUseCaches(false);
            _connection.setRequestMethod(method);
            _connection.setChunkedStreamingMode(0);
            _connection.setRequestProperty("X-NetNotes-Time", Utils.JSONDate(new Date()));
            _connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        }
        catch(Exception e) {
            System.exit(2);
        }
    }
    
    public APIRequest(String endpoint) {
        this(endpoint, "POST");
    }
    
    public void putData(JSONArray body) {
        OutputStream os = null;
        try {
            os = new BufferedOutputStream(_connection.getOutputStream());
            os.write(body.toString().getBytes("UTF-8"));
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
            
            reader = new BufferedReader(new InputStreamReader(_connection.getInputStream()));
        }
        catch(IOException e) {
            
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
        
        
        return new APIResponse(APIResponse.DATA_ERROR);
    }
    
}
