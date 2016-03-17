package com.cesarparent.netnotes.views;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import com.cesarparent.netnotes.R;
import com.cesarparent.netnotes.model.Model;
import com.cesarparent.netnotes.model.Note;

/**
 * Created by Cesar Parent on 04/03/2016.
 * 
 * View Controller for the note editor/creator.
 */
public class DetailViewController extends AppCompatActivity {
    
    // Intent actions used when launching the activity.
    public static final String ACTION_CREATE = "com.cesarparent.netnotes.action.CREATE";
    public static final String ACTION_EDIT = "com.cesarparent.netnotes.action.EDIT";
    public static final String EXTRA_UUID = "com.cesarparent.netnotes.extra.UUID";
    
    private EditText    _noteTextView;          // The editor text field.
    Note                _currentNote = null;    // The note being edited.

    /**
     * Creates the activity.
     * @param savedInstanceState    The saved state if there's any.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        
        // Basic Activity spawning.
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        
        // Enable the back arrow in the toolbar and catch UI widgets.
        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        _noteTextView = (EditText) findViewById(R.id.noteTextView);
        _noteTextView.setImeActionLabel(getString(R.string.action_save), KeyEvent.KEYCODE_ENTER);
        
        // Get the note from the saved instance state if there is one
        if(savedInstanceState != null) {
            _currentNote = (Note) savedInstanceState.getSerializable("currentNote");
        }
        // process the intent that launched the activity.
        processIntent(getIntent());
    }

    /**
     * Saves the current note in the database when the activity goes out of view.
     */
    @Override
    protected void onPause() {
        super.onPause();
        if(_currentNote == null) { return; }
        // Don't re-save if the text hasn't changed.
        if(_currentNote.text().equals(_noteTextView.getText().toString())) { return; }
        _currentNote.setText(_noteTextView.getText().toString());
        Model.addNote(_currentNote);
    }

    /**
     * Saves the note in the activity's saved state when required.
     * @param state     The state object.
     */
    @Override
    protected void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        state.putSerializable("currentNote", _currentNote);
    }

    /**
     * Processes the intent that launched the activity:
     * - If the intent is a share itent, create a new note and put the shared text into it.
     * - If the intent has ACTION_CREATE, create a new empty note.
     * - If the intent contains a valid UUID, load the note and display it.
     * - any other case, create a new empty note.
     * 
     * If there is already a valid note in the activity (recovered from the saved instance state
     * for example), or if the intent is null, the function is bypassed.
     * 
     * @param i     The intent to process.
     */
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
                _currentNote = new Note();
                _noteTextView.setText(data);
                break;
            
            case ACTION_CREATE:
                _currentNote = new Note("");
                break;
            
            case ACTION_EDIT:
                String uuid = i.getStringExtra(EXTRA_UUID);
                if(uuid == null) { return; }
                Model.getNoteWithUniqueID(uuid, new Model.NoteCompletionBlock() {
                    @Override
                    public void run(Note note) {
                        _currentNote = note;
                        _noteTextView.setText(_currentNote.text());
                    }
                });
                break;
            default:
                _currentNote = new Note();
        }
    }

    /**
     * Called on menu creation.
     * @param menu      The menu to create.
     * @return  Whether the menu was created.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.detail_menu, menu);
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
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                finish();
                break;
            
            case R.id.action_share:
                shareNote(item);
                break;
            
            case R.id.action_delete:
                delete();
                break;

            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Shares the note that is currently displayed in the editor using a SEND intent.
     * @param sender    The Menu Item that triggered the action.
     */
    public void shareNote(MenuItem sender) {
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("text/plain");
        share.putExtra(Intent.EXTRA_TEXT, _noteTextView.getText().toString());
        startActivity(Intent.createChooser(share, "Share Note With…"));
    }

    /**
     * Deletes the note currently displayed in the editor.
     */
    public void delete() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Delete Note?")
               .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialog, int which) {
                       Model.deleteNoteWithUniqueID(_currentNote.uniqueID());
                       NavUtils.navigateUpFromSameTask(DetailViewController.this);
                       _currentNote = null;
                       finish();
                   }
               })
               .setNegativeButton("No", null).show();
    }
}
