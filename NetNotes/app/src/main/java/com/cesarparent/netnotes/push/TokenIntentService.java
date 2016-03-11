package com.cesarparent.netnotes.push;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.cesarparent.netnotes.CPApplication;
import com.cesarparent.netnotes.R;
import com.cesarparent.utils.Notification;
import com.cesarparent.utils.NotificationCenter;
import com.google.android.gms.gcm.GcmPubSub;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import java.io.IOException;

/**
 * Created by cesar on 10/03/2016.
 */
public class TokenIntentService extends IntentService {
    
    private static final String KEY_TOKEN = "push.token";
    private static final String KEY_TOKEN_SENT = "push.token_sent";
    
    public TokenIntentService() {
        super("RegistrationIntentService");
    }
    
    @Override
    protected void onHandleIntent(Intent intent) {
        InstanceID instanceID = InstanceID.getInstance(this);
        SharedPreferences prefs = getSharedPreferences(CPApplication.PREFS_TAG, MODE_PRIVATE);

        Log.e("TokenIntentService", "Fetching Token…");
        try {
            Log.e("TokenIntentService", "Fetching Token…");
            String token = instanceID.getToken(getString(R.string.gcm_sender_id),
                                               GoogleCloudMessaging.INSTANCE_ID_SCOPE,
                                               null);
            
            prefs.edit().putString(KEY_TOKEN, token).apply();
            sendToken(token);
            
            Log.d("TokenIntentService", "GCM Token: " + token);
            NotificationCenter.defaultCenter()
                              .postNotification(Notification.TOKEN_REGISTRATION_COMPLETE, null);
        }
        catch(IOException e) {
            // TODO: To stuff
        }
    }
    
    private void sendToken(String token) {
        // TODO: Send to API endpoint
    }
    
    private void subscribe(String token) throws IOException {
        GcmPubSub.getInstance(this).subscribe(token, "/topics/global", null);
    }
    
}
