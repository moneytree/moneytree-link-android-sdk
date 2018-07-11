package com.example.myawesomeapp;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.example.myawesomeapp.fcm.TokenRegistrar;
import com.getmoneytree.MoneytreeLink;
import com.getmoneytree.MoneytreeLinkException;
import com.getmoneytree.it.IsshoTsucho;
import com.getmoneytree.listener.Action;
import com.getmoneytree.listener.Api;
import com.getmoneytree.listener.Authorization;
import com.google.android.gms.security.ProviderInstaller;
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

    ////// Identify Android version and show some UI if KitKat //////

    final int securityColumnVisibility;
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
      securityColumnVisibility = View.VISIBLE;
    } else {
      securityColumnVisibility = View.GONE;
    }
    findViewById(R.id.patch_text).setVisibility(securityColumnVisibility);
    findViewById(R.id.patch_button).setVisibility(securityColumnVisibility);
    findViewById(R.id.patch_border).setVisibility(securityColumnVisibility);

    ////// Set up Issho Tsucho //////

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
        MoneytreeLink.client().getToken(new Authorization.OnCompletionListener() {

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
        MoneytreeLink.client().openVaultFrom(
          MainActivity.this,
          new Authorization.OnCompletionListener() {
            @Override
            public void onSuccess(@NonNull final String accessToken) {
              // Nothing
            }

            @Override
            public void onError(@NonNull final MoneytreeLinkException exception) {
              if (exception.getError() ==
                  MoneytreeLinkException.Error.UNAUTHORIZED) {
                getStatusTextView().setText("No token in the SDK. Need to authorize first.");
              } else {
                getStatusTextView().setText(exception.getMessage());
              }
            }
          }
        );
      }
    });

    findViewById(R.id.auth_button).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        MoneytreeLink.client().authorizeFrom(
          MainActivity.this,
          true,
          new Authorization.OnCompletionListener() {
            @Override
            public void onSuccess(@NonNull final String accessToken) {
              getStatusTextView().setText("Authorized and got token: " + accessToken);
            }

            @Override
            public void onError(@NonNull final MoneytreeLinkException exception) {
              getStatusTextView().setText(exception.getMessage());
            }
          }
        );
      }
    });

    findViewById(R.id.settings_button).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        MoneytreeLink.client().openSettingsFrom(
          MainActivity.this,
          new Action.OnCompletionListener() {
            @Override
            public void onSuccess() {
              // Nothing
            }

            @Override
            public void onError(@NonNull final MoneytreeLinkException exception) {
              if (exception.getError() ==
                  MoneytreeLinkException.Error.UNAUTHORIZED) {
                getStatusTextView().setText("No token in the SDK. Need to authorize first.");
              } else {
                getStatusTextView().setText(exception.getMessage());
              }
            }
          }
        );
      }
    });

    findViewById(R.id.institution_button).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        MoneytreeLink.client().openInstitutionFrom(
          MainActivity.this,
          "fauxbank_test_bank",
          new Action.OnCompletionListener() {
            @Override
            public void onSuccess() {
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

    findViewById(R.id.logout_button).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        MoneytreeLink.client().logoutFrom(
          MainActivity.this,
          new Action.OnCompletionListener() {
            @Override
            public void onSuccess() {
              // Logout success, change status to authorization required.
              getStatusTextView().setText("Logged out");
            }

            @Override
            public void onError(@NonNull final MoneytreeLinkException exception) {
              if (exception.getError() == MoneytreeLinkException.Error.UNAUTHORIZED) {
                getStatusTextView().setText("Error in Logout");
              } else {
                getStatusTextView().setText(exception.getMessage());
              }
            }
          }
        );
      }
    });

    findViewById(R.id.patch_button).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(final View v) {
        try {
          ProviderInstaller.installIfNeeded(MainActivity.this);
          findViewById(R.id.patch_result).setVisibility(View.VISIBLE);
          ((TextView) findViewById(R.id.patch_result)).setText("Done applying security patch.");
        } catch (Exception e) {
          findViewById(R.id.patch_result).setVisibility(View.VISIBLE);
          ((TextView) findViewById(R.id.patch_result)).setText("Can't use Google Play Service.");
        }
      }
    });
  }

  /**
   * Start the Issho Tsucho
   */
  private void startIsshoTsucho() {
    getStatusTextView().setText("Launching...");
    IsshoTsucho.client().startIsshoTsucho(new IsshoTsucho.OnCompletionListener() {
      @Override
      public void onLaunchedIsshoTsucho() {
        getStatusTextView().setText("Launched Issho Tsucho successfully!");
      }

      @Override
      public void onFailedToLaunch(MoneytreeLinkException e) {
        getStatusTextView().setText(e.getLocalizedMessage());
        e.printStackTrace();
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
      .registerDeviceTokenFrom(
        this,
        token,
        new Api.OnCompletionListener() {
          @Override
          public void onSuccess() {
            getStatusTextView().setText("Finished registration successfully.");
          }

          @Override
          public void onError(@NonNull MoneytreeLinkException throwable) {
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
      .unregisterDeviceTokenFrom(
        this,
        token,
        new Api.OnCompletionListener() {
          @Override
          public void onSuccess() {
            getStatusTextView().setText("Finished de-registration successfully.");
          }

          @Override
          public void onError(@NonNull MoneytreeLinkException throwable) {
            getStatusTextView().setText(throwable.getMessage());
          }
        }
      );
  }
}
