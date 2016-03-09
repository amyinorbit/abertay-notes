package com.cesarparent.netnotes.views;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.cesarparent.netnotes.R;
import com.cesarparent.netnotes.model.Model;
import com.cesarparent.netnotes.sync.Authenticator;
import com.cesarparent.netnotes.sync.Sync;
import com.cesarparent.utils.Utils;

public class LoginViewController extends AppCompatActivity implements Sync.ResultCallback {
    
    private ProgressDialog _progress;
    private TextView    _emailTextField;
    private EditText    _passwordTextField;
    private Button      _button;
    

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if(Authenticator.isLoggedIn()) {
            showLoggedIn(false);
        } else {
            showLogIn(false);
        }
    }
    
    private void showLoggedIn(boolean fade) {
        smoothTransition(R.layout.view_logged_in, fade);
        
        _emailTextField = (TextView)findViewById(R.id.emailTextField);
        _button = (Button)findViewById(R.id.logInOutButton);
        _passwordTextField = null;
        _emailTextField.setText(Authenticator.getEmail());
        
        setUpToolbar();
    }
    
    private void showLogIn(boolean fade) {
        smoothTransition(R.layout.view_login, fade);

        _button = (Button)findViewById(R.id.logInOutButton);
        _emailTextField = (TextView)findViewById(R.id.emailTextField);
        _passwordTextField = (EditText)findViewById(R.id.passwordTextField);
        
        setUpToolbar();
    }
    
    private void setUpToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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
    
    public void smoothTransition(int id, boolean smooth) {
        if(smooth) {
            LayoutInflater inflator = getLayoutInflater();
            View view = inflator.inflate(id, null);
            view.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in));
            setContentView(view);
        } else {
            setContentView(id);
        }
    }
    
    public void logOut(View sender) {
        Sync.logOut();
        showLogIn(true);
    }
    
    public void logIn(View sender) {
        Utils.hideSoftKeyboard(this);
        _button.setEnabled(false);
        _progress = ProgressDialog.show(this, "Login In", "Please wait", true);
        Sync.logIn(_emailTextField.getText().toString(),
                   _passwordTextField.getText().toString(),
                   this);
    }

    @Override
    public void run(Sync.Status status) {
        _progress.dismiss();
        _button.setEnabled(true);
        Model.flushDeleted();
        if(status == Sync.Status.SUCCESS) {
            showLoggedIn(true);
        } else {
            Snackbar.make(_button, R.string.loginmsg_failed, Snackbar.LENGTH_SHORT).show();
        }
    }
}
