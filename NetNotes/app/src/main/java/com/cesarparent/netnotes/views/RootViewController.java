package com.cesarparent.netnotes.views;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
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
import com.cesarparent.netnotes.sync.SyncController;
import com.cesarparent.utils.Notification;
import com.cesarparent.utils.NotificationCenter;

public class RootViewController extends AppCompatActivity {
    
    private NotesAdapter _adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_root);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Model and list view
        Model.refresh();
        ListView noteListView = (ListView) findViewById(R.id.notesListView);
        _adapter = new NotesAdapter(this);
        noteListView.setAdapter(_adapter);
        
        noteListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                openNote(position);
            }
        });
        
        noteListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(RootViewController.this);
                builder.setMessage("Delete Note?")
                       .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                           @Override
                           public void onClick(DialogInterface dialog, int which) {
                               deleteNote(position);
                           }
                       })
                       .setNegativeButton("No", null).show();
                return true;
            }
        });
    }
    
    @Override
    public void onResume() {
        super.onResume();
        NotificationCenter.defaultCenter().addObserver(Notification.MODEL_UPDATE,
                                                       this,
                                                       "onModelChange");
        _adapter.notifyDataSetChanged();
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
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent i = new Intent(this, LoginViewController.class);
                startActivity(i);
                break;
            
            case R.id.action_sync:
                SyncController.sharedInstance().triggerSync();
                break;

            default:
                break;
        }

        return super.onOptionsItemSelected(item);
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
    
    public void onModelChange(Object notification) {
        _adapter.notifyDataSetChanged();
    }

}
