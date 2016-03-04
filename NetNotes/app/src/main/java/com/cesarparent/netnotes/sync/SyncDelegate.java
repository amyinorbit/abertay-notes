package com.cesarparent.netnotes.sync;

/**
 * Created by cesar on 23/02/2016.
 *
 * Defines methods to be implemented to receive data from a SyncController.
 */
public interface SyncDelegate {

    public void didLogin();

    public void didReceiveUpdates();

    public void didReceiveDeletes();

    public void didReceiveErrors();

}
