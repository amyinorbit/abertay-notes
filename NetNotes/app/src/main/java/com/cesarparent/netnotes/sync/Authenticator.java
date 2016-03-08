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
    
    private SharedPreferences _prefs;
    private String _token;
    private String _email;
    
    public Authenticator() {
        _prefs = CPApplication.getSharedPreferences();
        _email = _prefs.getString("user.email", null);
        _token = _prefs.getString("user.token", null);
    }
    
    public String getToken() {
        if(_token == null) { return null; }
        String mac = Utils.HMACAuth(_email, _token, API_KEY);
        return mac;
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
