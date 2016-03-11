package com.cesarparent.netnotes.push;

import android.content.Intent;

import com.google.android.gms.iid.InstanceIDListenerService;

import java.io.IOException;

/**
 * Created by cesar on 10/03/2016.
 * 
 * 
 */
public class PushTokenListener extends InstanceIDListenerService {
    
    @Override
    public void onTokenRefresh() {
        Intent i = new Intent(this, TokenIntentService.class);
        startService(i);
    }
}
