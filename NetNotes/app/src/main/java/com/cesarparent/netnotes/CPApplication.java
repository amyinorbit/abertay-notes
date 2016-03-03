package com.cesarparent.netnotes;

import android.app.Application;
import android.content.Context;

/**
 * Created by cesar on 01/03/2016.
 *
 * Used to provide static, universal access to the application's context
 */
public class CPApplication extends Application {
    private static Context _context;

    public void onCreate() {
        super.onCreate();
        CPApplication._context = getApplicationContext();
    }

    public static Context getContext() {
        return CPApplication._context;
    }
}
