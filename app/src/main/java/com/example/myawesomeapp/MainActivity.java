package com.example.myawesomeapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.getmoneytree.MoneytreeLinkClient;

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
                MoneytreeLinkClient.authorize();
            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        // Check an intent has a token
        if (MoneytreeLinkClient.hasToken(intent)) {
            final String token = MoneytreeLinkClient.findToken(intent);
            saveToken(token);
        }

        // Need to call on singleTask activity to work `getIntent()` in other methods
        setIntent(intent);
    }

    private void saveToken(@NonNull String token) {
        // You should save a token to DB in secure way.

        // FIXME: Remove in your production code
        final TextView view = (TextView) findViewById(R.id.token_string);
        view.setText(token);
    }
}