package com.cesarparent.utils;

import android.app.Activity;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.cesarparent.netnotes.CPApplication;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by cesar on 03/03/2016.
 * 
 * General Utility class
 */
public class Utils {
    
    public static String JSONDate(Date date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZZZ",
                                                       CPApplication.locale());
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
        return format.format(date);
    }
    
    public static Date dateFromJSON(String date) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZZZ",
                                                       CPApplication.locale());
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
        return format.parse(date);
    }
    
    public static String HTTPBasicAuth(String username, String password) {
        return "Basic "+ Base64.encodeToString((username + ":" + password).getBytes(),
                                               Base64.NO_WRAP);
    }
    
    public static String safeSubString(String string, int start, int end) {
        start = Math.max(start, 0);
        end = Math.min(end, string.length());
        return string.substring(start, end);
    }
    
    public static String HMACAuth(String username, String token, String key) {
        try {
            Mac crypto = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret = new SecretKeySpec(key.getBytes(), "HmacSHA256");
            crypto.init(secret);
            String hmac = Base64.encodeToString(crypto.doFinal(token.getBytes("UTF-8")),
                                                Base64.NO_WRAP);
            return Base64.encodeToString((username+":"+hmac).getBytes(), Base64.NO_WRAP);
        }
        catch(NoSuchAlgorithmException | InvalidKeyException | UnsupportedEncodingException e) {
            Log.e("HMAC", "Error Creating Token: "+ e.getMessage());
            return "";
        }
    }

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager)activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View focus = activity.getCurrentFocus();
        if(focus != null) {
            inputMethodManager.hideSoftInputFromWindow(focus.getWindowToken(), 0);
        }
        
    }
    
}
