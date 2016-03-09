package com.cesarparent.utils;

/**
 * Created by cesar on 09/03/2016.
 * 
 * Objects that implement SQLObject provide an easy interface to output their columns and values.
 */
public interface SQLObject {
    
    String getTableName();
    
    String getTypeTransactionID();
    
    String getDatabaseColumns();
    
    String getDatabasePlaceholders();
    
    Object[] getDatabaseValues();
}
