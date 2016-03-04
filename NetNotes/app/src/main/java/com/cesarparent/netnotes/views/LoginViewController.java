package com.cesarparent.netnotes.views;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.cesarparent.netnotes.R;
import com.cesarparent.netnotes.sync.SyncController;
import com.cesarparent.utils.NotificationCenter;

public class LoginViewController extends AppCompatActivity {
    
    private ProgressBar _loginProgressIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_login);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        _loginProgressIndicator = (ProgressBar)findViewById(R.id.progressBar);
        _loginProgressIndicator.setVisibility(ProgressBar.INVISIBLE);

        NotificationCenter.defaultCenter().addObserver(SyncController.kLoggedInNotification,
                                                       this,
                                                       "didLogin");
        NotificationCenter.defaultCenter().addObserver(SyncController.kLoginFailNotification,
                                                       this,
                                                       "failedLogin");
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                finish();
                break;

            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    
    
    public void validateLoginInput(View sender) {
        _loginProgressIndicator.setVisibility(ProgressBar.VISIBLE);
    }
    
    public void didLogin(Object notification) {
        Toast.makeText(this, "Log In Successful", Toast.LENGTH_LONG).show();
        _loginProgressIndicator.setVisibility(ProgressBar.INVISIBLE);
    }
    
    public void failedLogin(Object notification) {
        Toast.makeText(this, "Log In Failed", Toast.LENGTH_LONG).show();
        _loginProgressIndicator.setVisibility(ProgressBar.INVISIBLE);
    }
}
