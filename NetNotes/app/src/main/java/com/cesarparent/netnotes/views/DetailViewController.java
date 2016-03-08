package com.cesarparent.netnotes.views;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.cesarparent.netnotes.R;
import com.cesarparent.netnotes.model.Model;
import com.cesarparent.netnotes.model.Note;

public class DetailViewController extends AppCompatActivity {
    
    public static final String ACTION_CREATE = "com.cesarparent.netnotes.action.CREATE";
    public static final String ACTION_EDIT = "com.cesarparent.netnotes.action.EDIT";
    
    public static final String EXTRA_UUID = "com.cesarparent.netnotes.extra.UUID";
    
    private EditText _noteTextView;
    
    Note _currentNote = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
        _noteTextView = (EditText) findViewById(R.id.noteTextView);
        _noteTextView.setImeActionLabel(getString(R.string.action_save), KeyEvent.KEYCODE_ENTER);
        
        processIntent(getIntent());
        
        if(_currentNote != null) {
            _noteTextView.setText(_currentNote.text());
        }
        
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        if(_currentNote == null) { return; }
        if(_currentNote.text().equals(_noteTextView.getText().toString())) { return; }
        _currentNote.setText(_noteTextView.getText().toString());
        Model.sharedInstance().addNote(_currentNote);
    }
    
    private void processIntent(Intent i) {
        if(_currentNote != null) { 
            Log.d("DetailViewController", "Object already exists");
            return;
        }
        if(i == null) { return; }
        if(i.getAction() == null) { return; }
        switch (i.getAction()) {
            case Intent.ACTION_SEND:
                if(i.getType() == null) { return; }
                if(!i.getType().contains("text/plain")) { return; }
                String data = i.getStringExtra(Intent.EXTRA_TEXT);
                if(data == null) { return; }
                _currentNote = new Note(data);
                break;
            
            case ACTION_CREATE:
                _currentNote = new Note("");
                break;
            
            case ACTION_EDIT:
                String uuid = i.getStringExtra(EXTRA_UUID);
                if(uuid == null) { return; }
                Model.sharedInstance().getNoteWithUniqueID(uuid, new Model.NoteCompletionBlock() {
                    @Override
                    public void run(Note note) {
                        _currentNote = note;
                        _noteTextView.setText(_currentNote.text());
                    }
                });
                break;
            default:
                _currentNote = new Note("");
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.detail_menu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                finish();
                break;
            
            case R.id.action_share:
                shareNote(item);
                break;
            
            case R.id.action_delete:
                Model.sharedInstance().deleteNoteWithUniqueID(_currentNote.uniqueID());
                NavUtils.navigateUpFromSameTask(this);
                _currentNote = null;
                finish();
                break;

            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    
    public void shareNote(MenuItem sender) {
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("text/plain");
        share.putExtra(Intent.EXTRA_TEXT, _noteTextView.getText().toString());
        startActivity(Intent.createChooser(share, "Share Note With…"));
    }
}
