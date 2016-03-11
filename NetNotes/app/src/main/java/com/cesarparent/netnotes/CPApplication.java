package com.cesarparent.netnotes;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;

import java.util.Locale;

/**
 * Created by cesar on 01/03/2016.
 *
 * Used to provide static, universal access to the application's context
 */
public class CPApplication extends Application {
    private static Context _context;
    
    public static final String PREFS_TAG = "com.cesarparent.NetNotes";

    public void onCreate() {
        super.onCreate();
        CPApplication._context = getApplicationContext();
    }

    public static Context getContext() {
        return CPApplication._context;
    }
    
    public static String string(int id) {
        return _context.getString(id);
    }
    
    public static String getDeviceID() {
        String android_id = Settings.Secure.getString(getContext().getContentResolver(),
                                                      Settings.Secure.ANDROID_ID);
        return android_id+android_id;
    }
    
    
    public static Locale locale() {
        return _context.getResources().getConfiguration().locale;
    }
    
    public static SharedPreferences getSharedPreferences() {
        return _context.getSharedPreferences(PREFS_TAG, Context.MODE_PRIVATE);
    }
}
