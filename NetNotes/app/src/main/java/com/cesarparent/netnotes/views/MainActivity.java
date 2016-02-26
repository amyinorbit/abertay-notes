package com.cesarparent.netnotes.views;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.cesarparent.netnotes.R;
import com.cesarparent.utils.NotificationCenter;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        NotificationCenter.defaultCenter().addObserver("kTestNotification", this, "test");

    }

    public void test(Object notification) {
        Toast.makeText(this, "Received Notification!", Toast.LENGTH_LONG).show();
    }


    public void doSomething(View sender) {
        NotificationCenter.defaultCenter().postNotification("kTestNotification", sender);
    }
}
