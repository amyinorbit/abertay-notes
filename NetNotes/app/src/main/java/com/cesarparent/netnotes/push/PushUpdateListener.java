package com.cesarparent.netnotes.push;

import android.os.Bundle;
import android.util.Log;
import com.cesarparent.netnotes.sync.Sync;
import com.google.android.gms.gcm.GcmListenerService;

/**
 * Created by cesar on 10/03/2016.
 * 
 * 
 */
public class PushUpdateListener extends GcmListenerService {
    
    @Override
    public void onMessageReceived(String from, Bundle data) {
        Log.d("PushUpdateListener", "Received message from "+from);
        String action = data.getString("sync_action");
        if(action == null) { return; }
        
        switch (action) {
            case "update":
                Log.d("PushUpdateListener", "Triggering update refresh");
                Sync.refreshUpdate();
                break;
            case "delete":
                Log.d("PushUpdateListener", "Triggering delete refresh");
                Sync.refreshDelete();
                break;
            default:
                Log.w("PushUpdateListener", "Invalid push sync action: " + action);
                break;
        }
    }
}
