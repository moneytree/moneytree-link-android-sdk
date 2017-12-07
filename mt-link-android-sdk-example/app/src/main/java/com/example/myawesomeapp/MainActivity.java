package com.example.myawesomeapp;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.example.myawesomeapp.fcm.TokenRegistrar;
import com.getmoneytree.MoneytreeLink;
import com.getmoneytree.MoneytreeLinkException;
import com.getmoneytree.auth.CompletionHandler;
import com.getmoneytree.it.IsshoTsucho;
import com.google.firebase.iid.FirebaseInstanceId;

/**
 * A showcase app that introduces what the SDK can do.
 *
 * @author Moneyteee KK
 */
public class MainActivity extends AppCompatActivity implements TokenRegistrar {

  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    findViewById(R.id.issho_tsucho_button).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        startIsshoTsucho();
      }
    });

    ////// Set up VaaS (If you use Issho Tsucho, you don't have to implement the below code) //////

    findViewById(R.id.token_button).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(final View view) {
        // Need to pass activity when you get a token.
        MoneytreeLink.client(MainActivity.this).getToken(new CompletionHandler() {
          @Override
          public void onSuccess(@NonNull final String accessToken) {
            getStatusTextView().setText("Token: " + accessToken);
          }

          @Override
          public void onError(@NonNull final MoneytreeLinkException exception) {
            if (exception.getError() == MoneytreeLinkException.Error.UNAUTHORIZED) {
              getStatusTextView().setText("No token in the SDK. Need to authorize first.");
            } else {
              getStatusTextView().setText(exception.getMessage());
            }
          }
        });
      }
    });

    findViewById(R.id.vault_button).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        MoneytreeLink.client().openVaultFrom(MainActivity.this, new CompletionHandler() {
          @Override
          public void onSuccess(@NonNull final String accessToken) {
            // Nothing
          }

          @Override
          public void onError(@NonNull final MoneytreeLinkException exception) {
            if (exception.getError() == MoneytreeLinkException.Error.UNAUTHORIZED) {
              getStatusTextView().setText("No token in the SDK. Need to authorize first.");
            } else {
              getStatusTextView().setText(exception.getMessage());
            }
          }
        });
      }
    });

    findViewById(R.id.auth_button).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        MoneytreeLink.client().authorizeFrom(MainActivity.this, new CompletionHandler() {
          @Override
          public void onSuccess(@NonNull final String accessToken) {
            getStatusTextView().setText("Authorized and got token: " + accessToken);
          }

          @Override
          public void onError(@NonNull final MoneytreeLinkException exception) {
            getStatusTextView().setText(exception.getMessage());
          }
        });
      }
    });

    findViewById(R.id.settings_button).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        MoneytreeLink.client().openSettingsFrom(MainActivity.this, new CompletionHandler() {
          @Override
          public void onSuccess(@NonNull final String accessToken) {
            // Nothing
          }

          @Override
          public void onError(@NonNull final MoneytreeLinkException exception) {
            if (exception.getError() == MoneytreeLinkException.Error.UNAUTHORIZED) {
              getStatusTextView().setText("No token in the SDK. Need to authorize first.");
            } else {
              getStatusTextView().setText(exception.getMessage());
            }
          }
        });
      }
    });

    findViewById(R.id.institution_button).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        MoneytreeLink.client().openInstitutionFrom(
            MainActivity.this,
            "fauxbank_test_bank",
            new CompletionHandler() {
              @Override
              public void onSuccess(@NonNull final String accessToken) {
                // Nothing
              }

              @Override
              public void onError(@NonNull final MoneytreeLinkException exception) {
                if (exception.getError() == MoneytreeLinkException.Error.UNAUTHORIZED) {
                  getStatusTextView().setText("No token in the SDK. Need to authorize first.");
                } else {
                  getStatusTextView().setText(exception.getMessage());
                }
              }
            }
        );
      }
    });

    findViewById(R.id.register_button).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        registerToken();
      }
    });

    findViewById(R.id.deregister_button).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        deregisterToken();
      }
    });

    getStatusTextView().setText(
        MoneytreeLink.client().isLoggedIn() ? "Logged In" : "Unauthorized"
    );

    findViewById(R.id.reset_button).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(final View view) {
        MoneytreeLink.client().deleteCredentials();
        getStatusTextView().setText("Deleted token in the SDK.");
      }
    });
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
      getStatusTextView().setText("Can't get a device token from the device.");
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
      getStatusTextView().setText("Can't get a device token from the device.");
      return;
    }

    deregisterToken(token);
  }

  private TextView getStatusTextView() {
    return findViewById(R.id.result_text);
  }

  @Override
  public void registerToken(@NonNull String token) {
    getStatusTextView().setText(token);
    if (!MoneytreeLink.client().isLoggedIn()) {
      getStatusTextView().setText("Need to authorize first.");
      return;
    }

    MoneytreeLink
        .client()
        .registerDeviceToken(
            token,
            new MoneytreeLink.ApiCompletionHandler() {
              @Override
              public void onSuccess() {
                getStatusTextView().setText("Finished registration successfully.");
              }

              @Override
              public void onError(@NonNull Throwable throwable) {
                getStatusTextView().setText(throwable.getMessage());
              }
            }
        );
  }

  @Override
  public void deregisterToken(@NonNull String token) {
    getStatusTextView().setText(token);
    if (!MoneytreeLink.client().isLoggedIn()) {
      getStatusTextView().setText("Need to authorize first.");
      return;
    }

    MoneytreeLink
        .client()
        .unregisterDeviceToken(
            token,
            new MoneytreeLink.ApiCompletionHandler() {
              @Override
              public void onSuccess() {
                getStatusTextView().setText("Finished de-registration successfully.");
              }

              @Override
              public void onError(@NonNull Throwable throwable) {
                getStatusTextView().setText(throwable.getMessage());
              }
            }
        );
  }
}
