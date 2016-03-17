package com.cesarparent.utils;

/**
 * Created by cesar on 06/03/2016.
 * 
 * Valid notifications that can be broadcast accross the app.
 */
public abstract class Notification {
    
    public static final String MODEL_UPDATE = "notificationModelChanged";
    public static final String LOGIN_SUCCESS = "notificationLoginSuccess";
    public static final String LOGIN_FAIL = "notificationLoginFail";
    public static final String TOKEN_REGISTRATION_COMPLETE = "tokenRegistrationComplete";
}
