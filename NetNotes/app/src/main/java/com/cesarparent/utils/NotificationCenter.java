package com.cesarparent.utils;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import com.cesarparent.netnotes.CPApplication;

/**
 * Created by cesar on 17/03/2016.
 * 
 * Convenience wrapper around LocalBroadcastManager - Provides easy way of posting synchronous
 * notifications around the app.
 */
public class NotificationCenter {

    /**
     * Broadcasts a notification string through the local broadcast manager.
     * @param notification      The notification to broadcast.
     */
    public static void postNotification(String notification) {
        Intent b = new Intent(notification);
        LocalBroadcastManager.getInstance(CPApplication.getContext()).sendBroadcastSync(b);
    }

    /**
     * Registers a BroadcastReceiver for a certain type of notification.
     * @param notification      The notification type to subscribe to.
     * @param observer          The observer to register.
     */
    public static void registerObserver(String notification, @NonNull BroadcastReceiver observer) {
        LocalBroadcastManager.getInstance(CPApplication.getContext())
                             .registerReceiver(observer, new IntentFilter(notification));
    }

    /**
     * Unregisters a receiver.
     * @param observer          The receiver to unregister.
     */
    public static void removeObserver(@NonNull BroadcastReceiver observer) {
        LocalBroadcastManager.getInstance(CPApplication.getContext()).unregisterReceiver(observer);
    }
    
}
