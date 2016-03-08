package com.cesarparent.netnotes.sync;

import android.content.SharedPreferences;
import android.util.Log;

import com.cesarparent.netnotes.CPApplication;
import com.cesarparent.utils.Utils;

/**
 * Created by cesar on 04/03/2016.
 */
public class Authenticator {

    private static final String API_KEY = "C162E35C-638C-478A-8A57-F89FA72B9AA6";
    
    private static final String DEFAULT_DATE = "1970-01-01 00:00:01+000";
    
    private static final String KEY_EMAIL = "user.email";
    private static final String KEY_TOKEN = "user.token";
    private static final String KEY_TIMEDELETE = "sync.time.delete";
    private static final String KEY_TIMEUPDATE = "sync.time.update";
    
    private SharedPreferences _prefs;
    
    final private Object _syncDateLock;
    
    public Authenticator() {
        _prefs = CPApplication.getSharedPreferences();
        _syncDateLock = new Object();
    }
    
    public String getAuthToken() {
        if(!isLoggedIn()) { return null; }
        return Utils.HMACAuth(getEmail(), getToken(), API_KEY);
    }
    
    public String getToken() {
        return _prefs.getString(KEY_TOKEN, null);
    }
    
    public String getEmail() {
        return _prefs.getString(KEY_EMAIL, null);
    }
    
    public boolean isLoggedIn() {
        return (getEmail() != null && getToken() != null);
    }
    
    public void setCredentials(String email, String token) {
        SharedPreferences.Editor editor = _prefs.edit();
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_TOKEN, token);
        editor.apply();
    }
    
    public String getDeleteSyncTime() {
        return _prefs.getString(KEY_TIMEDELETE, DEFAULT_DATE);
    }
    
    public String getUpdateSyncTime() {
        return _prefs.getString(KEY_TIMEUPDATE, DEFAULT_DATE);
    }
    
    public void setDeleteSyncTime(String time) {
        synchronized (_syncDateLock) {
            put(KEY_TIMEDELETE, time);
        }
    }
    
    public void setUpdateSyncTime(String time) {
        synchronized (_syncDateLock) {
            put(KEY_TIMEUPDATE, time);
        }
    }
    
    private void put(String key, String value) {
        SharedPreferences.Editor editor = _prefs.edit();
        editor.putString(key, value);
        editor.apply();
    }
    
    public void invalidateCredentials() {
        SharedPreferences.Editor editor = _prefs.edit();
        editor.remove(KEY_EMAIL);
        editor.remove(KEY_TOKEN);
        editor.apply();
    }
    
    public void invalidateSyncDates() {
        SharedPreferences.Editor editor = _prefs.edit();
        editor.remove(KEY_TIMEUPDATE);
        editor.remove(KEY_TIMEDELETE);
        editor.apply();
    }
    
}
