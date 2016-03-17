package com.cesarparent.netnotes.views;

import android.app.AlertDialog;
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
import com.cesarparent.netnotes.R;
import com.cesarparent.netnotes.model.Model;
import com.cesarparent.netnotes.model.NotesAdapter;
import com.cesarparent.netnotes.push.PushTokenService;
import com.cesarparent.netnotes.sync.Authenticator;
import com.cesarparent.netnotes.sync.Sync;
import com.cesarparent.utils.Notification;
import com.cesarparent.utils.NotificationCenter;

public class RootViewController extends AppCompatActivity implements Sync.ResultCallback {
    
    private NotesAdapter    _adapter = null;
    SwipeRefreshLayout      _pullToRefresh = null;
    boolean                 _requestPending = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_root);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        
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
        
        // Model and list view
        refresh();
        refreshToken();
        
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
    }
    
    @Override
    public void onResume() {
        super.onResume();
        Model.refresh();
        NotificationCenter.defaultCenter().addObserver(Notification.MODEL_UPDATE,
                                                       this,
                                                       "onModelChange");
    }
    
    @Override
    public void onPause() {
        super.onPause();
        NotificationCenter.defaultCenter().removeObserver(this);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.root_menu, menu);
        return true;
    }
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem syncMenuItem = menu.findItem(R.id.action_sync);
        syncMenuItem.setEnabled(!_requestPending);
        syncMenuItem.getIcon().setAlpha(_requestPending ? 130 : 255);
        return true;
    }
    
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
    
    public void startCreateNote(View sender) {
        Intent i = new Intent(this, DetailViewController.class);
        i.setAction(DetailViewController.ACTION_CREATE);
        startActivity(i);
    }
    
    public void openNote(int position) {
        String uuid = Model.getHandleAtIndex(position).uniqueID;
        Intent i = new Intent(this, DetailViewController.class);
        i.setAction(DetailViewController.ACTION_EDIT);
        i.putExtra(DetailViewController.EXTRA_UUID, uuid);
        startActivity(i);
    }
    
    public void deleteNote(int position) {
        String uuid = Model.getHandleAtIndex(position).uniqueID;
        Model.deleteNoteWithUniqueID(uuid);
    }
    
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
    
    public void openLogin() {
        Intent i = new Intent(this, LoginViewController.class);
        startActivity(i);
    }
    
    public void onModelChange(Object notification) {
        _adapter.notifyDataSetChanged();
    }
    
    public void refreshToken() {
        if(Authenticator.isPushTokenSent()) { return; }
        Intent token = new Intent(this, PushTokenService.class);
        startService(token);
    }
}
