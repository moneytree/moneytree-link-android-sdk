package com.example.myawesomeapp;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.getmoneytree.MoneytreeLink;
import com.getmoneytree.auth.OAuthAccessToken;
import com.getmoneytree.auth.OAuthCode;
import com.getmoneytree.auth.OAuthHandler;

/**
 * @author Moneyteee KK, 2017
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Button implicitButton = (Button) findViewById(R.id.implicit_button);
        implicitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MoneytreeLink.client().authorize(new OAuthHandler<OAuthAccessToken>() {

                    @Override
                    public void onSuccess(OAuthAccessToken payload) {
                        displayResult("token: " + payload.getAccessToken());
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        throwable.printStackTrace();
                        displayResult(throwable.getMessage());
                    }
                });
            }
        });

        final Button codeButton = (Button) findViewById(R.id.code_button);
        codeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MoneytreeLink.client().authorize(new OAuthHandler<OAuthCode>() {

                    @Override
                    public void onSuccess(OAuthCode payload) {
                        displayResult("code: " + payload.getCode());
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        throwable.printStackTrace();
                        displayResult(throwable.getMessage());
                    }
                });
            }
        });
    }

    private void displayResult(@NonNull String value) {
        final TextView textView = (TextView) findViewById(R.id.result_text);
        textView.setText(value);
    }
}