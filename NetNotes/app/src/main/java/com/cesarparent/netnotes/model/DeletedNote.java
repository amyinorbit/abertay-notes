package com.cesarparent.netnotes.model;

import com.cesarparent.netnotes.sync.Authenticator;
import com.cesarparent.utils.SQLObject;

/**
 * Created by cesar on 09/03/2016.
 * 
 */
public class DeletedNote implements SQLObject {
    
    private static final String _COLUMNS = "uniqueID, seqID";
    
    private String _uniqueID;
    
    @Override
    public String getDatabaseColumns() {
        return _COLUMNS;
    }

    @Override
    public String getDatabasePlaceholders() {
        return "?, ?";
    }
    
    @Override
    public String getTableName() {
        return "deleted";
    }
    
    @Override
    public String getTypeTransactionID() {
        return Authenticator.getDeleteTransactionID();
    }

    @Override
    public Object[] getDatabaseValues() {
        return new Object[] {
                _uniqueID,
                getTypeTransactionID()
        };
    }
    
    public DeletedNote(String uniqueID) {
        _uniqueID = uniqueID;
    }
}
