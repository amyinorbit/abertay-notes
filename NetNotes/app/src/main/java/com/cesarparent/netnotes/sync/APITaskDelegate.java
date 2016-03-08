package com.cesarparent.netnotes.sync;

/**
 * Created by cesar on 04/03/2016.
 * 
 * Basic Delegate that will be notified of an API's task changes
 */
public abstract class APITaskDelegate {
    
    public abstract void taskDidReceiveResponse(APIResponse response);
    
    public void taskWasCancelled() {};
}
