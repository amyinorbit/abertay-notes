package com.cesarparent.netnotes.views;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.cesarparent.netnotes.R;
import com.cesarparent.netnotes.model.Model;
import com.cesarparent.netnotes.push.PushTokenService;
import com.cesarparent.netnotes.sync.Authenticator;
import com.cesarparent.netnotes.sync.Sync;
import com.cesarparent.utils.Utils;

/**
 * Created by Amy Parent on 04/03/2016.
 *
 * View Controller for the login/create account activity.
 */
public class LoginViewController extends AppCompatActivity implements Sync.ResultCallback {
    
    private ProgressDialog  _progress;          // The spinner dialog shown on login.
    private TextView        _emailTextField;    // The email input field.
    private EditText        _passwordTextField; // The password input field.
    private Button          _button;            // The subbmit/logout button.

    /**
     * Creates the activity.
     * @param savedInstanceState    The saved state if there's any.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if(Authenticator.isLoggedIn()) {
            showLoggedIn();
        } else {
            showLogIn();
        }
    }

    /**
     * Show the logged-in layout.
     */
    private void showLoggedIn() {
        setContentView(R.layout.view_logged_in);
        
        _emailTextField = (TextView)findViewById(R.id.emailTextField);
        _button = (Button)findViewById(R.id.logInOutButton);
        _passwordTextField = null;
        _emailTextField.setText(Authenticator.getEmail());
        
        setUpToolbar();
    }

    /**
     * Show the log-in layout.
     */
    private void showLogIn() {
        setContentView(R.layout.view_login);

        _button = (Button)findViewById(R.id.logInOutButton);
        _emailTextField = (TextView)findViewById(R.id.emailTextField);
        _passwordTextField = (EditText)findViewById(R.id.passwordTextField);
        
        setUpToolbar();
    }

    /**
     * Set-up the Material toolbar to show the back button.
     */
    private void setUpToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
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

            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Triggers a sync log-out.
     * @param sender    The button that triggered the action.
     */
    public void logOut(View sender) {
        Sync.logOut();
        showLogIn();
    }

    /**
     * Starts the log-in process.
     * @param sender    The button that triggered the action.
     */
    public void logIn(View sender) {
        doRequest(false);
    }

    /**
     * Starts the sign-up/create accoutn process.
     * @param sender    The button that triggered the action.
     */
    public void signUp(View sender) {
        doRequest(true);
    }

    /**
     * Starts a log-in or sign-up request, using the data input by the user.
     * @param signup    Whether the user should be signed up or logged in.
     */
    public void doRequest(boolean signup) {
        Utils.hideSoftKeyboard(this);
        _button.setEnabled(false);
        _progress = ProgressDialog.show(this, "Logging In", "Please wait", true);
        Sync.logIn(signup,
                   _emailTextField.getText().toString(),
                   _passwordTextField.getText().toString(),
                   this);
    }

    /**
     * Called when the login/sign-up request returns. If the request is successful, the view is
     * changed to the logged-in layout.
     * @param status    The response status.
     */
    @Override
    public void onSyncResult(Sync.Status status) {
        _progress.dismiss();
        _button.setEnabled(true);
        Model.flushDeleted();
        if(status == Sync.Status.SUCCESS) {
            showLoggedIn();
            Sync.refresh();
            Intent i = new Intent(this, PushTokenService.class);
            startService(i);
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Error")
                   .setMessage(status.toString())
                   .setPositiveButton(android.R.string.ok, null).show();
        }
    }
}
