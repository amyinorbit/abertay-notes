package com.cesarparent.netnotes.model;

import android.provider.BaseColumns;

/**
 * Created by cesar on 03/03/2016.
 * 
 * Contract to facilitate transaction between the model layer and the Database helper
 */
public class DatabaseContract {
    
    public DatabaseContract() {}
    
    public static abstract class DBNote implements BaseColumns {
        public static final String TABLE_NAME = "note";
        public static final String COLUMN_NAME_ENTRY_ID = "uniqueID";
        public static final String COLUMN_NAME_TEXT = "text";
        public static final String COLUMN_NAME_CREATEDATE = "createDate";
        public static final String COLUMN_NAME_SORTDATE = "sortDate";
    }
}
