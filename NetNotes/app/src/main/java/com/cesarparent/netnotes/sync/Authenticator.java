package com.cesarparent.netnotes.sync;

import android.content.SharedPreferences;

import com.cesarparent.netnotes.CPApplication;
import com.cesarparent.utils.Utils;

/**
 * Created by cesar on 04/03/2016.
 * 
 */
public class Authenticator {

    private static final String API_KEY = "C162E35C-638C-478A-8A57-F89FA72B9AA6";
    
    private static final String KEY_EMAIL = "user.email";
    private static final String KEY_TOKEN = "user.token";
    private static final String KEY_SEQDELETE = "sync.transaction.delete";
    private static final String KEY_SEQUPDATE = "sync.transaction.update";
    private static final String KEY_PUSH_TOKEN = "push.token";
    private static final String KEY_PUSH_TOKEN_SENT = "push.token_sent";
    
    private static final Object _sequenceIDLock = new Object();
    
    public static String getAuthToken() {
        if(!isLoggedIn()) { return null; }
        return Utils.HMACAuth(getEmail(), getToken(), API_KEY);
    }
    
    public static String getToken() {
        
        return CPApplication.getSharedPreferences().getString(KEY_TOKEN, null);
    }
    
    public static String getEmail() {
        return CPApplication.getSharedPreferences().getString(KEY_EMAIL, null);
    }
    
    public static boolean isLoggedIn() {
        return (getEmail() != null && getToken() != null);
    }
    
    public static boolean isTokenSent() {
        return CPApplication.getSharedPreferences().getBoolean(KEY_PUSH_TOKEN_SENT, false);
    }
    
    public static void setCredentials(String email, String token) {
        SharedPreferences.Editor editor = CPApplication.getSharedPreferences().edit();
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_TOKEN, token);
        editor.apply();
    }
    
    public static String getDeleteTransactionID() {
        return CPApplication.getSharedPreferences().getString(KEY_SEQDELETE, "0");
    }
    
    public static String getUpdateTransactionID() {
        return CPApplication.getSharedPreferences().getString(KEY_SEQUPDATE, "0");
    }
    
    public static void setDeleteTransactionID(String time) {
        synchronized (_sequenceIDLock) {
            put(KEY_SEQDELETE, time);
        }
    }
    
    public static void setUpdateTransactionID(String time) {
        synchronized (_sequenceIDLock) {
            put(KEY_SEQUPDATE, time);
        }
    }
    
    private static void put(String key, String value) {
        SharedPreferences.Editor editor = CPApplication.getSharedPreferences().edit();
        editor.putString(key, value);
        editor.apply();
    }
    
    public static void invalidateCredentials() {
        SharedPreferences.Editor editor = CPApplication.getSharedPreferences().edit();
        editor.remove(KEY_EMAIL);
        editor.remove(KEY_TOKEN);
        editor.apply();
    }
    
    public static void invalidateSyncDates() {
        SharedPreferences.Editor editor = CPApplication.getSharedPreferences().edit();
        editor.remove(KEY_SEQUPDATE);
        editor.remove(KEY_SEQDELETE);
        editor.apply();
    }
    
}
