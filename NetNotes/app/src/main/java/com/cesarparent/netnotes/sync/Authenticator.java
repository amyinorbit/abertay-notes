package com.cesarparent.netnotes.sync;

import android.content.SharedPreferences;

import com.cesarparent.netnotes.CPApplication;

/**
 * Created by cesar on 04/03/2016.
 */
public class Authenticator {
    
    private SharedPreferences _prefs;
    private String _token;
    private String _email;
    
    public Authenticator() {
        _prefs = CPApplication.getSharedPreferences();
        _email = _prefs.getString("user.email", null);
        _token = _prefs.getString("user.token", null);
    }
    
    public String getToken() {
        return (_token != null) ? _token : "";
    }
    
    public String getEmail() {
        return (_email != null) ? _email : "";
    }
    
    public boolean isLoggedIn() {
        return (_email != null && _token != null);
    }
    
    public void setCredentials(String email, String token) {
        _email = email;
        _token = token;
        _writePrefs();
    }
    
    public void setEmail(String email) {
        _email = email;
        _writePrefs();
    }
    
    public void setToken(String token) {
        _token = token;
        _writePrefs();
    }
    
    public void invalidateCredentials() {
        _email = _token = null;
        SharedPreferences.Editor editor = _prefs.edit();
        editor.remove("user.email");
        editor.remove("user.token");
        editor.apply();
    }
    
    private void _writePrefs() {
        SharedPreferences.Editor editor = _prefs.edit();
        editor.putString("user.email", _email);
        editor.putString("user.token", _token);
        editor.apply();
    }
}
