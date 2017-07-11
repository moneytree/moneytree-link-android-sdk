package com.example.myawesomeapp;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.getmoneytree.MoneytreeLink;
import com.getmoneytree.token.TokenHandler;

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
                MoneytreeLink.client().authorize(new TokenHandler() {
                    @Override
                    public void onSuccess(String token) {
                        saveToken(token);
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        throwable.printStackTrace();
                    }
                });
            }
        });
    }

    private void saveToken(@NonNull String token) {
        // You should save a token to DB in secure way.

        // FIXME: Remove in your production code
        final TextView view = (TextView) findViewById(R.id.token_string);
        view.setText(token);
    }
}