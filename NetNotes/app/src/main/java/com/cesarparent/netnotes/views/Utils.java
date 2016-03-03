package com.cesarparent.netnotes.views;

import com.cesarparent.netnotes.CPApplication;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by cesar on 03/03/2016.
 * 
 * General Utility class
 */
public class Utils {
    
    public static String JSONDate(Date date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZZZ",
                                                       CPApplication.locale());
        return format.format(date);
    }
    
    public static Date dateFromJSON(String date) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZZZ",
                                                       CPApplication.locale());
        return format.parse(date);
    }
    
}
