package com.cesarparent.netnotes.sync;

/**
 * Created by cesar on 04/03/2016.
 * 
 * Basic Delegate that will be notified of an API's task changes
 */
public interface APITaskDelegate {
    
    void taskDidReceiveResponse(APIResponse response);
    
    
    void taskWasCancelled();
}
