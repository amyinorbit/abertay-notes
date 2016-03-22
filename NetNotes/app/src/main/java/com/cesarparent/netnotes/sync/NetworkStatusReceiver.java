package com.cesarparent.netnotes.sync;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.cesarparent.netnotes.CPApplication;

/**
 * Created by cesar on 17/03/2016.
 * 
 * Receives network connectivity changes and triggers a sync when the network becomes available.
 */
public class NetworkStatusReceiver extends BroadcastReceiver {
    
    private static boolean connected = false;
    
    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager cm = (ConnectivityManager) CPApplication
                .getContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        if(info == null || !info.isConnectedOrConnecting()) {
            connected = false;
            return;
        }
        Log.d("NetworkStatusReceiver", "Network Available. Triggering refresh");
        if(connected) { return; }
        Sync.refresh();
        connected = true;
    }
}
