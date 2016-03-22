package com.cesarparent.netnotes.views;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.cesarparent.utils.NotificationCenter;
import com.cesarparent.netnotes.R;
import com.cesarparent.netnotes.model.Model;
import com.cesarparent.netnotes.model.NotesAdapter;
import com.cesarparent.netnotes.push.PushTokenService;
import com.cesarparent.netnotes.sync.Authenticator;
import com.cesarparent.netnotes.sync.Sync;
import com.cesarparent.utils.Notification;

/**
 * Created by Cesar Parent on 04/03/2016.
 *
 * View Controller for the login/create account activity.
 */
public class RootViewController extends AppCompatActivity implements Sync.ResultCallback {
    
    private NotesAdapter    _adapter = null;            // The adapter used to show the note list.
    SwipeRefreshLayout      _pullToRefresh = null;      // The pull-to-refresh layout coordinator.
    boolean                 _requestPending = false;    // Whether a request is currently running.
    BroadcastReceiver       _observer = null;           // The ModelChange receiver;

    /**
     * Creates the activity.
     * @param savedInstanceState    The saved state if there's any.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        
        // Basic Activity spawning.
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_root);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        
        // Create the broadcast receiver;
        _observer = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                onModelChange();
            }
        };
        
        
        // UI widget grabbing extravaganza.
        _pullToRefresh = (SwipeRefreshLayout) findViewById(R.id.pullRefreshView);
        _pullToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });
        
        _adapter = new NotesAdapter(this);
        ListView noteListView = (ListView) findViewById(R.id.notesListView);
        noteListView.setAdapter(_adapter);
        
        noteListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                openNote(position);
            }
        });

        // Setup the long click to delete action.
        noteListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(RootViewController.this);
                builder.setMessage("Delete Note?")
                       .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                           @Override
                           public void onClick(DialogInterface dialog, int which) {
                               deleteNote(position);
                           }
                       })
                       .setNegativeButton(android.R.string.no, null).show();
                return true;
            }
        });
        
        // Refresh the data and make sure the Push token has been uploaded to the server.
        // If not logged in, let's not bother.
        if(Authenticator.isLoggedIn()) {
            refresh();
            refreshToken();
        }
    }

    /**
     * Resumes the activity and trigger a refresh. Registers the activity to receive ModelChange
     * broadcasts.
     */
    @Override
    public void onResume() {
        super.onResume();
        Model.refresh();
        NotificationCenter.registerObserver(Notification.MODEL_UPDATE, _observer);
    }

    /**
     * Pauses the activity, and un-registers the activity from ModelChange broadcasts.
     */
    @Override
    public void onPause() {
        super.onPause();
        NotificationCenter.removeObserver(_observer);
    }

    /**
     * Called on menu creation.
     * @param menu      The menu to create.
     * @return  Whether the menu was created.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.root_menu, menu);
        return true;
    }

    /**
     * Re-draws the menu. If a request is being ran, disables the refresh button and make it
     * half-transparent.
     * @param menu      The menu to re-draw.
     * @return  Whether the menu was re-drawn.
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem syncMenuItem = menu.findItem(R.id.action_sync);
        syncMenuItem.setEnabled(!_requestPending);
        syncMenuItem.getIcon().setAlpha(_requestPending ? 127 : 255);
        return true;
    }

    /**
     * Dispatches actions based on which item of the menu was tapped.
     * @param item      The item that was tapped.
     * @return  Whether the action was consumed.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                openLogin();
                break;
            
            case R.id.action_sync:
                refresh();
                break;

            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Called when a refresh request has returned. Re-enables the refresh button, stop the spinning
     * ListView indicator. If the request failed, show the error and possible action in a SnackBar.
     * @param status    The response status.
     */
    @Override
    public void onSyncResult(Sync.Status status) {
        _requestPending = false;
        invalidateOptionsMenu();
        _pullToRefresh.post(new Runnable() {
            @Override
            public void run() {
                _pullToRefresh.setRefreshing(false);
            }
        });
        
        if(status == Sync.Status.SUCCESS) { return; }
        final Snackbar message = Snackbar.make(_pullToRefresh, status.toString(), Snackbar.LENGTH_LONG);
        if(status == Sync.Status.FAIL_UNAUTHORIZED || status == Sync.Status.FAIL_LOGGED_OUT) {
            message.setAction(R.string.action_login, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openLogin();
                }
            });
        } else {
            message.setAction(R.string.action_try_again, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    refresh();
                }
            });
        }
        message.show();
    }

    /**
     * Starts the editor activity to create a new note.
     * @param sender    The button that triggered the action.
     */
    public void startCreateNote(View sender) {
        Intent i = new Intent(this, DetailViewController.class);
        i.setAction(DetailViewController.ACTION_CREATE);
        startActivity(i);
    }

    /**
     * Opens the note at a given position in the list in the editor.
     * @param position  The note's position in the root list.
     */
    public void openNote(int position) {
        String uuid = Model.getHandleAtIndex(position).uniqueID;
        Intent i = new Intent(this, DetailViewController.class);
        i.setAction(DetailViewController.ACTION_EDIT);
        i.putExtra(DetailViewController.EXTRA_UUID, uuid);
        startActivity(i);
    }

    /**
     * Deletes the note at a given position in the list.
     * @param position  The note's position in the root list.
     */
    public void deleteNote(int position) {
        String uuid = Model.getHandleAtIndex(position).uniqueID;
        Model.deleteNoteWithUniqueID(uuid);
    }

    /**
     * Manually refreshes the data and shows it in the UI.
     */
    public void refresh() {
        _requestPending = true;
        // Necessary for the loading indicator to show up when called in onCreate
        _pullToRefresh.post(new Runnable() {
            @Override
            public void run() {
                _pullToRefresh.setRefreshing(true);
            }
        });
        _pullToRefresh.setRefreshing(true);
        invalidateOptionsMenu();
        Sync.refresh(this);
    }

    /**
     * Starts the login activity.
     */
    public void openLogin() {
        Intent i = new Intent(this, LoginViewController.class);
        startActivity(i);
    }

    /**
     * Notifies the ListView adapter when the model has changed and the view should show enw data.
     */
    public void onModelChange() {
        _adapter.notifyDataSetChanged();
    }

    /**
     * If the Push Notification Token hasn't been sent to the server yet, attempt to send it.
     */
    public void refreshToken() {
        if(Authenticator.isPushTokenSent()) { return; }
        Intent token = new Intent(this, PushTokenService.class);
        startService(token);
    }
}
