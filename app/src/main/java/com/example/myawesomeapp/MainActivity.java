package com.example.myawesomeapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import jp.moneytree.mtlinksdk.MTLinkClient;

/**
 * @author Moneyteee KK, 2017
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Button button = (Button) findViewById(R.id.login_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MTLinkClient.authorize();
            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        // Need to call on singleTask activity to work `getIntent()` in other methods
        setIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // TODO: Parse token from intent
    }
}
