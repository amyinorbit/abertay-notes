package com.cesarparent.netnotes.push;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.util.Log;
import com.cesarparent.netnotes.CPApplication;
import com.cesarparent.netnotes.R;
import com.cesarparent.netnotes.sync.APIRequest;
import com.cesarparent.netnotes.sync.APIResponse;
import com.cesarparent.netnotes.sync.Authenticator;
import com.cesarparent.netnotes.sync.Sync;
import com.cesarparent.utils.Notification;
import com.cesarparent.utils.NotificationCenter;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;

/**
 * Created by cesar on 10/03/2016.
 * 
 * Service used to register a new, or changed Google API Registration ID with the server.
 */
public class PushTokenService extends IntentService {

    /**
     * Creates the Push Token Service.
     */
    public PushTokenService() {
        super("RegistrationIntentService");
    }

    /**
     * Receives the start intent, extracts the token from it and sends it to the server.
     * @param intent        The intent that started the service.
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        InstanceID instanceID = InstanceID.getInstance(this);

        Log.d("PushTokenService", "Fetching Token…");
        try {
            String token = instanceID.getToken(getString(R.string.gcm_sender_id),
                                               GoogleCloudMessaging.INSTANCE_ID_SCOPE,
                                               null);
            sendToken(token);
            Log.d("PushTokenService", "GCM Token: " + token);
            NotificationCenter.postNotification(Notification.TOKEN_REGISTRATION_COMPLETE);
        }
        catch(IOException e) {
            // TODO: To stuff
        }
    }

    /**
     * Sends a token to the server.
     * @param token     The token to send to the server.
     */
    private void sendToken(@NonNull String token) {
        if(!Authenticator.isLoggedIn()) {
            Log.e("PushTokenService", "User not logged in, cannot send token");
            return;
        }
        APIRequest req = new APIRequest(APIRequest.Endpoint.TOKEN, "0");
        req.setAuthtorization(Authenticator.getAuthToken());
        JSONObject payload = new JSONObject();
        try {
            payload.put("token", token);
        }
        catch(JSONException e) {
            Log.e("PushTokenService", "JSON Error: "+e.getMessage());
            return;
        }
        req.putData(payload);
        APIResponse res = req.send();
        
        if(res.getStatus() != Sync.Status.SUCCESS) {
            Log.e("PushTokenService", "Error sending token to server ("+res.getStatus()+")");
            SharedPreferences.Editor editor = getSharedPreferences(CPApplication.PREFS_TAG,
                                                                   MODE_PRIVATE).edit();
            editor.remove(Authenticator.KEY_PUSH_TOKEN);
            editor.putBoolean(Authenticator.KEY_PUSH_TOKEN_SENT, false);
            editor.apply();
        } else {
            Log.d("PushTokenService", "Token sent to server");
            SharedPreferences.Editor editor = getSharedPreferences(CPApplication.PREFS_TAG,
                                                                   MODE_PRIVATE).edit();
            editor.putString(Authenticator.KEY_PUSH_TOKEN, token);
            editor.putBoolean(Authenticator.KEY_PUSH_TOKEN_SENT, true);
            editor.apply();
        }
    }
    
}
