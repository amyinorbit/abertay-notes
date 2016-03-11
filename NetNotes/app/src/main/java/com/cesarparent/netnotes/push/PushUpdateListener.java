package com.cesarparent.netnotes.push;

import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;

/**
 * Created by cesar on 10/03/2016.
 * 
 * 
 */
public class PushUpdateListener extends GcmListenerService {
    
    
    @Override
    public void onMessageReceived(String from, Bundle data) {
        Log.d("PushNotifications", "Received message from "+from);
        Log.d("PushNotifications", "Data: "+data);
        // TODO: Handle 
    }
}
