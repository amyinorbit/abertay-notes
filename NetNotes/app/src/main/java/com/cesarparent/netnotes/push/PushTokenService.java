package com.cesarparent.netnotes.push;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.cesarparent.netnotes.CPApplication;
import com.cesarparent.netnotes.R;
import com.cesarparent.netnotes.sync.APIRequest;
import com.cesarparent.netnotes.sync.APIResponse;
import com.cesarparent.netnotes.sync.Authenticator;
import com.cesarparent.utils.Notification;
import com.cesarparent.utils.NotificationCenter;
import com.google.android.gms.gcm.GcmPubSub;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;

/**
 * Created by cesar on 10/03/2016.
 */
public class PushTokenService extends IntentService {
    
    private static final String KEY_TOKEN = "push.token";
    private static final String KEY_TOKEN_SENT = "push.token_sent";
    
    
    public PushTokenService() {
        super("RegistrationIntentService");
    }
    
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
            NotificationCenter.defaultCenter()
                              .postNotification(Notification.TOKEN_REGISTRATION_COMPLETE, null);
        }
        catch(IOException e) {
            // TODO: To stuff
        }
    }
    
    private void sendToken(String token) {
        if(!Authenticator.isLoggedIn()) {
            Log.e("PushTokenService", "User not logged in, cannot send token");
            return;
        }
        APIRequest req = new APIRequest(APIRequest.ENDPOINT_TOKEN, "0");
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
        
        if(res.getStatus() != APIResponse.SUCCESS) {
            Log.e("PushTokenService", "Error sending token to server("+res.getStatus()+")");
            SharedPreferences.Editor editor = getSharedPreferences(CPApplication.PREFS_TAG,
                                                                   MODE_PRIVATE).edit();
            editor.remove(KEY_TOKEN);
            editor.putBoolean(KEY_TOKEN_SENT, false);
            editor.apply();
        } else {
            Log.d("PushTokenService", "Token sent to server");
            SharedPreferences.Editor editor = getSharedPreferences(CPApplication.PREFS_TAG,
                                                                   MODE_PRIVATE).edit();
            editor.putString(KEY_TOKEN, token);
            editor.putBoolean(KEY_TOKEN_SENT, true);
            editor.apply();
        }
        
    }
    
    private void subscribe(String token) throws IOException {
        GcmPubSub.getInstance(this).subscribe(token, "/topics/global", null);
    }
    
}
