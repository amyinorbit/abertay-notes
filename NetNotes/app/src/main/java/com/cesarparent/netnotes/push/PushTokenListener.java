package com.cesarparent.netnotes.push;

import android.content.Intent;

import com.google.android.gms.iid.InstanceIDListenerService;

/**
 * Created by cesar on 10/03/2016.
 * 
 * Service that listens to changes of the Google API registration ID.
 */
public class PushTokenListener extends InstanceIDListenerService {

    /**
     * Called when the Google API registration ID changes. Starts the Push Token intent, that will
     * send the new token to the server.
     */
    @Override
    public void onTokenRefresh() {
        Intent i = new Intent(this, PushTokenService.class);
        startService(i);
    }
}
