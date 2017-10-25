package com.example.myawesomeapp;

import android.databinding.DataBindingUtil;
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

import com.example.myawesomeapp.databinding.ActivityMainBinding;
import com.example.myawesomeapp.fcm.TokenRegistrar;
import com.getmoneytree.MoneytreeLink;
import com.getmoneytree.MoneytreeLinkConfiguration;
import com.getmoneytree.MoneytreeLinkException;
import com.getmoneytree.MoneytreeLinkScope;
import com.getmoneytree.auth.OAuthCode;
import com.getmoneytree.auth.OAuthHandler;
import com.getmoneytree.auth.OAuthPayload;
import com.getmoneytree.auth.OAuthResponseType;
import com.getmoneytree.auth.OAuthToken;
import com.getmoneytree.it.IsshoTsucho;
import com.google.firebase.iid.FirebaseInstanceId;

import static com.getmoneytree.auth.OAuthResponseType.Code;
import static com.getmoneytree.auth.OAuthResponseType.Token;

/**
 * A showcase app that introduces what the SDK can do.
 *
 * @author Moneyteee KK
 */
public class MainActivity extends AppCompatActivity implements TokenRegistrar {

    @NonNull
    ActivityMainBinding binding;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        ////// Set up Issho Tsucho //////

        // Initialize Issho Tsucho (NOTE: WE RECOMMEND TO INITIALIZE AT 'Application' CLASS)
        IsshoTsucho.init(getApplicationContext(), getConfiguration(R.id.radio_code));

        binding.isshoTsuchoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startIsshoTsucho();
            }
        });

        ////// Set up VaaS (If you use Issho Tsucho, you don't have to implement the below code) //////

        // Set Implicit as default
        @IdRes final int defaultGrantType = R.id.radio_token;
        final RadioGroup group = binding.responseRadioGroup;
        binding.responseRadioGroup.check(defaultGrantType);

        // Strongly recommend to initialize MoneytreeLink client at Application class
        // if you don't want to use both 'Implicit' and 'Code' at the same time or you don't want
        // to set different scopes dynamically. This AwesomeApp is a show case app to demonstrate
        // what the SDK provides, so it gives capability to change configuration after the app
        // initializes once.

        // Set up MoneytreeLink client once, but it might be overridden
        // when you change the response type (in this app).
        MoneytreeLink
                .init(getApplicationContext(), getConfiguration(defaultGrantType))
                .setRootView(this);

        // Set the default OAuth handler (Implicit).
        MoneytreeLink.client().setOAuthHandler(getHandler(defaultGrantType));

        // Set up click listeners
        group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                // Initialize MoneytreeLink client again
                MoneytreeLink
                        .init(getApplicationContext(), getConfiguration(checkedId))
                        .setRootView(MainActivity.this);
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

        binding.vaultButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MoneytreeLink.client().openVaultFrom(MainActivity.this);
            }
        });

        binding.authButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MoneytreeLink.client().authorize();
            }
        });

        binding.settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MoneytreeLink.client().openSettings();
            }
        });

        binding.registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerToken();
            }
        });

        binding.deregisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deregisterToken();
            }
        });

        getStatusTextView().setText(
                MoneytreeLink.client().isLoggedIn() ? "Logged In" : "Unauthorized"
        );
    }

    /**
     * Provide {@link MoneytreeLinkConfiguration} instance.
     *
     * @return {@link MoneytreeLinkConfiguration}
     */
    private MoneytreeLinkConfiguration getConfiguration(@IdRes int checkedId) {
        final OAuthResponseType grantType = checkedId == R.id.radio_token ? Token : Code;
        return new MoneytreeLinkConfiguration.Builder()
                // true: production, false: staging
                .isProduction(false)
                // It's for the example app. DON'T USE FOR YOUR APP!
                .clientId("af84f08f40970caf17f2e53b31771ceb50d0f32f7d44b826753982e809395290")
                // You can add scopes using String as well.
                //.scopes("customized_scope", "new_scope")
                .scopes(
                        MoneytreeLinkScope.GuestRead,
                        MoneytreeLinkScope.AccountsRead,
                        MoneytreeLinkScope.TransactionsRead
                )
                // Token(token) or Code
                .responseType(grantType)
                .build();
    }

    /**
     * Start the Issho Tsucho
     */
    private void startIsshoTsucho() {
        getStatusTextView().setText("Launching...");
        IsshoTsucho.client().startIsshoTsucho(new IsshoTsucho.CompletionHandler() {
            @Override
            public void onLaunchedIsshoTsucho() {
                getStatusTextView().setText("Launched Issho Tsucho successfully!");
            }

            @Override
            public void onFailedToLaunch(MoneytreeLinkException e) {
                getStatusTextView().setText(e.getLocalizedMessage());
                e.printStackTrace();

                // FIXME: Identify why launch process failed
            }
        });
    }

    /**
     * Register the current token (from MT Server)
     */
    private void registerToken() {
        final String token = FirebaseInstanceId.getInstance().getToken();
        if (token == null) {
            Toast.makeText(this, "No Token", Toast.LENGTH_LONG).show();
            return;
        }

        registerToken(token);
    }

    /**
     * Remove the current token (from MT Server)
     */
    private void deregisterToken() {
        final String token = FirebaseInstanceId.getInstance().getToken();
        if (token == null) {
            Toast.makeText(this, "No Token", Toast.LENGTH_LONG).show();
            return;
        }

        deregisterToken(token);
    }

    private TextView getStatusTextView() {
        return binding.resultText;
    }

    /**
     * Provide appropriate handler class that consumes response.
     *
     * @return handler instance
     */
    private OAuthHandler<? extends OAuthPayload> getHandler(@IdRes int checkedId) {
        if (checkedId == R.id.radio_token) {
            return new OAuthHandler<OAuthToken>() {
                @Override
                public void onSuccess(@NonNull OAuthToken payload) {
                    getStatusTextView().setText("Token: " + payload.accessToken);
                }

                @Override
                public void onError(@NonNull Throwable error) {
                    error.printStackTrace();
                    getStatusTextView().setText("Error: " + error.getMessage());
                }
            };
        } else {
            return new OAuthHandler<OAuthCode>() {
                @Override
                public void onSuccess(@NonNull OAuthCode payload) {
                    getStatusTextView().setText("Code: " + payload.getCode());

                }

                @Override
                public void onError(@NonNull Throwable error) {
                    error.printStackTrace();
                    getStatusTextView().setText("Error: " + error.getMessage());
                }
            };
        }
    }

    @Override
    public void registerToken(@NonNull String token) {
        getStatusTextView().setText(token);
        if (!MoneytreeLink.client().isLoggedIn()) {
            Toast.makeText(this, "Unauthorized Yet", Toast.LENGTH_LONG).show();
            return;
        }

        // It runs when Issho Tsucho launches successfully.
        // So the server side can accept device token if the guest already has.
        MoneytreeLink
                .client()
                .registerDeviceToken(
                        token,
                        new MoneytreeLink.ApiCompletionHandler() {
                            @Override
                            public void onSuccess() {
                                Toast
                                        .makeText(MainActivity.this, "Success!", Toast.LENGTH_LONG)
                                        .show();
                            }

                            @Override
                            public void onError(@NonNull Throwable throwable) {
                                Toast
                                        .makeText(MainActivity.this, "Failed!", Toast.LENGTH_LONG)
                                        .show();

                                MainActivity
                                        .this
                                        .getStatusTextView()
                                        .setText(throwable.getMessage());
                            }
                        });
    }

    @Override
    public void deregisterToken(@NonNull String token) {
        getStatusTextView().setText(token);
        if (!MoneytreeLink.client().isLoggedIn()) {
            Toast.makeText(this, "Unauthorized Yet", Toast.LENGTH_LONG).show();
            return;
        }

        MoneytreeLink
                .client()
                .unregisterDeviceToken(
                        token,
                        new MoneytreeLink.ApiCompletionHandler() {
                            @Override
                            public void onSuccess() {
                                Toast
                                        .makeText(MainActivity.this, "Success!", Toast.LENGTH_LONG)
                                        .show();
                            }

                            @Override
                            public void onError(@NonNull Throwable throwable) {
                                Toast
                                        .makeText(MainActivity.this, "Failed!", Toast.LENGTH_LONG)
                                        .show();

                                MainActivity
                                        .this
                                        .getStatusTextView()
                                        .setText(throwable.getMessage());
                            }
                        });
    }

}
