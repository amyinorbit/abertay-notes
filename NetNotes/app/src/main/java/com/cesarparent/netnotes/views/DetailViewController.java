package com.cesarparent.netnotes.views;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import com.cesarparent.netnotes.R;
import com.cesarparent.netnotes.model.Note;

public class DetailViewController extends AppCompatActivity {
    
    private EditText _noteTextView;
    
    Note _currentNote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
        _noteTextView = (EditText) findViewById(R.id.noteTextView);
        
        processIntent(getIntent());
        
    }
    
    private void processIntent(Intent i) {
        if(i == null) { return; }
        if(i.getType() == null) { return; }
        if(!i.getType().contains("text/plain")) { return; }
        String data = i.getStringExtra(Intent.EXTRA_TEXT);
        if(data == null) { return; }
        _noteTextView.setText(data);
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
