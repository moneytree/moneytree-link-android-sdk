package com.example.myawesomeapp;

import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.getmoneytree.MoneytreeLink;
import com.getmoneytree.MoneytreeLinkConfiguration;
import com.getmoneytree.MoneytreeLinkScope;
import com.getmoneytree.auth.OAuthAccessToken;
import com.getmoneytree.auth.OAuthCode;
import com.getmoneytree.auth.OAuthHandler;
import com.getmoneytree.auth.OAuthPayload;
import com.getmoneytree.auth.OAuthResponseType;
import com.getmoneytree.it.IsshoTsucho;

import static com.getmoneytree.auth.OAuthResponseType.Code;
import static com.getmoneytree.auth.OAuthResponseType.Token;

/**
 * @author Moneyteee KK
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up a click listener for Issho Tsucho
        final Button isshoTsuchoButton = (Button) findViewById(R.id.issho_tsucho_button);
        isshoTsuchoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IsshoTsucho.init(getApplicationContext(), getConfiguration(R.id.radio_code));
                IsshoTsucho.client().startIsshoTsucho();
            }
        });

        // Set Implicit as default
        @IdRes final int defaultGrantType = R.id.radio_token;
        final RadioGroup group = (RadioGroup) findViewById(R.id.response_radio_group);
        group.check(defaultGrantType);

        // Strongly recommend to initialize MoneytreeLink client at Application class if you don't want to use both 'Implicit' and 'Code' at the same time or you don't want to set different scopes dynamically. This AwesomeApp is a show case app to demonstrate what the SDK provides, so it gives capability to change configuration after the app initializes once.

        // Set up MoneytreeLink client once, but it might be overridden when you change the response type.
        MoneytreeLink.init(getApplicationContext(), getConfiguration(defaultGrantType));

        // Make sure to set the default OAuth handler (Implicit).
        MoneytreeLink.client().setOAuthHandler(getHandler(defaultGrantType));

        // Set up click listeners
        group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                // Initialize MoneytreeLink client again
                MoneytreeLink.init(getApplicationContext(), getConfiguration(checkedId));
                MoneytreeLink.client().setOAuthHandler(getHandler(checkedId));

                final RadioButton selectedButton = (RadioButton) findViewById(checkedId);
                Toast
                        .makeText(
                                MainActivity.this,
                                "Changed grantType to " + selectedButton.getText(),
                                Toast.LENGTH_SHORT
                        )
                        .show();
            }
        });

        final Button signUpButton = (Button) findViewById(R.id.signup_button);
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MoneytreeLink.client().signup();
            }
        });

        final Button loginButton = (Button) findViewById(R.id.login_button);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MoneytreeLink.client().login();
            }
        });

        final Button authButton = (Button) findViewById(R.id.auth_button);
        authButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MoneytreeLink.client().authorize();
            }
        });

        final Button vaultButton = (Button) findViewById(R.id.vault_button);
        vaultButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MoneytreeLink.client().openVault();
            }
        });

        final Button settingButton = (Button) findViewById(R.id.settings_button);
        settingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MoneytreeLink.client().openSettings();
            }
        });
    }

    /**
     * Provide {@link MoneytreeLinkConfiguration} instance.
     *
     * @return {@link MoneytreeLinkConfiguration}
     */
    private MoneytreeLinkConfiguration getConfiguration(@IdRes int checkedId) {
        final OAuthResponseType grantType = checkedId == R.id.radio_token ? Token : Code;
        return new MoneytreeLinkConfiguration.Builder()
                .isProduction(false)                            // true: production, false: staging
                .clientId(getString(R.string.link_client_id))   // set your ClientId
                .scopes(MoneytreeLinkScope.GuestRead)           // set scopes
                //.scopes("customized_scope", "new_scope")      // You can add scopes using String as well.
                .responseType(grantType)                  // Token(token) or Code
                .build();
    }

    /**
     * Provide appropriate handler class that consumes response.
     *
     * @return handler instance
     */
    private OAuthHandler<? extends OAuthPayload> getHandler(@IdRes int checkedId) {
        if (checkedId == R.id.radio_token) {
            return new OAuthHandler<OAuthAccessToken>() {
                @Override
                public void onSuccess(OAuthAccessToken payload) {
                    displayResult("token: " + payload.getAccessToken());
                }

                @Override
                public void onFailure(Throwable throwable) {
                    throwable.printStackTrace();
                    displayResult(throwable.getMessage());
                }
            };
        } else {
            return new OAuthHandler<OAuthCode>() {
                @Override
                public void onSuccess(OAuthCode payload) {
                    displayResult("code: " + payload.getCode());
                }

                @Override
                public void onFailure(Throwable throwable) {
                    throwable.printStackTrace();
                    displayResult(throwable.getMessage());
                }
            };
        }
    }

    /**
     * Display response value on the view
     *
     * @param value response
     */
    private void displayResult(@NonNull String value) {
        final TextView textView = (TextView) findViewById(R.id.result_text);
        textView.setText(value);
    }
}
