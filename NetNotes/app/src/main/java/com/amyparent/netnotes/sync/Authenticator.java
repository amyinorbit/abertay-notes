package com.cesarparent.netnotes.sync;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.cesarparent.netnotes.CPApplication;
import com.cesarparent.utils.Utils;

import java.util.Date;

/**
 * Created by cesar on 04/03/2016.
 * 
 * Utility functions needed for synchronisation.
 */
public class Authenticator {

    private static final String API_KEY = "C162E35C-638C-478A-8A57-F89FA72B9AA6";
    
    public static final String KEY_EMAIL = "user.email";
    public static final String KEY_TOKEN = "user.token";
    public static final String KEY_SEQDELETE = "sync.transaction.delete";
    public static final String KEY_SEQUPDATE = "sync.transaction.update";
    public static final String KEY_SERVER_TIME_OFFSET = "sync.timeoffset";
    public static final String KEY_PUSH_TOKEN = "push.token";
    public static final String KEY_PUSH_TOKEN_SENT = "push.token_sent";
    
    private static final Object _sequenceIDLock = new Object();

    /**
     * Returns the authentication token for the current user.
     * @return  The authentication token for the current user.
     */
    @Nullable
    public static String getAuthToken() {
        if(!isLoggedIn()) { return null; }
        return Utils.HMACAuth(getEmail(), getToken(), API_KEY);
    }

    /**
     * Returns the user's login token.
     * @return  The user's login token.
     */
    @Nullable
    public static String getToken() {
        return CPApplication.getSharedPreferences().getString(KEY_TOKEN, null);
    }

    /**
     * Returns the user's email address if logged in.
     * @return  The user's email address, or null.
     */
    @Nullable
    public static String getEmail() {
        return CPApplication.getSharedPreferences().getString(KEY_EMAIL, null);
    }

    /**
     * Returns whether the user has a valid token and email address registered.
     * @return  Whether the user has a valid token and email address registered.
     */
    public static boolean isLoggedIn() {
        return (getEmail() != null && getToken() != null);
    }

    /**
     * Returns whether the device's Push Token has been sent to the server.
     * @return  Whether the device's Push Token has been sent to the server.
     */
    public static boolean isPushTokenSent() {
        return CPApplication.getSharedPreferences().getBoolean(KEY_PUSH_TOKEN_SENT, false);
    }

    /**
     * Sets the user's credentials once they have been validated by the server.
     * @param email     The user's email address.
     * @param token     The user's login token.
     */
    public static void setCredentials(String email, String token) {
        SharedPreferences.Editor editor = CPApplication.getSharedPreferences().edit();
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_TOKEN, token);
        editor.apply();
    }

    /**
     * Returns the ID of the last successful Delete transaction.
     * @return  The ID of the last successful Delete transaction.
     */
    @NonNull
    public static String getDeleteTransactionID() {
        return CPApplication.getSharedPreferences().getString(KEY_SEQDELETE, "0");
    }

    /**
     * Returns the ID of the last successful Update transaction.
     * @return  The ID of the last successful Update transaction.
     */
    @NonNull
    public static String getUpdateTransactionID() {
        return CPApplication.getSharedPreferences().getString(KEY_SEQUPDATE, "0");
    }

    /**
     * Sets the Update transaction ID.
     * @param transaction   The TUdate transaction ID.
     */
    public static void setDeleteTransactionID(@NonNull String transaction) {
        synchronized (_sequenceIDLock) {
            put(KEY_SEQDELETE, transaction);
        }
    }

    /**
     * Sets the Update transaction ID.
     * @param transaction   The TUdate transaction ID.
     */
    public static void setUpdateTransactionID(@NonNull String transaction) {
        synchronized (_sequenceIDLock) {
            put(KEY_SEQUPDATE, transaction);
        }
    }

    /**
     * Sets the time offset between the phone and the server's clocks.
     * @param offset    The time offset between the phone and the server's clocks.
     */
    public static void setTimeOffset(long offset) {
        SharedPreferences.Editor editor = CPApplication.getSharedPreferences().edit();
        editor.putLong(KEY_SERVER_TIME_OFFSET, offset);
        editor.apply();
    }

    /**
     * Returns the time offset between the phone and the server's clocks.
     * @return  The time offset between the phone and the server's clocks.
     */
    public static long getTimeOffset() {
        return CPApplication.getSharedPreferences().getLong(KEY_SERVER_TIME_OFFSET, 0);
    }

    /**
     * Returns a Date object corrected with the server-client offset.
     * @return  A Date object corrected with the server-client offset.
     */
    @NonNull
    public static Date now() {
        return new Date(System.currentTimeMillis() + getTimeOffset());
    }

    /**
     * Puts a string in the application's shared preferences.
     * @param key       The preferences key.
     * @param value     The value.
     */
    private static void put(@NonNull String key, @NonNull String value) {
        SharedPreferences.Editor editor = CPApplication.getSharedPreferences().edit();
        editor.putString(key, value);
        editor.apply();
    }

    /**
     * Deletes saved credentials from the application's preferences.
     */
    public static void invalidateCredentials() {
        SharedPreferences.Editor editor = CPApplication.getSharedPreferences().edit();
        editor.remove(KEY_EMAIL);
        editor.remove(KEY_TOKEN);
        editor.apply();
    }

    /**
     * Deletes saved transaction IDs from the application's preferences.
     */
    public static void invalidateSyncDates() {
        SharedPreferences.Editor editor = CPApplication.getSharedPreferences().edit();
        editor.remove(KEY_SEQUPDATE);
        editor.remove(KEY_SEQDELETE);
        editor.apply();
    }
    
}
