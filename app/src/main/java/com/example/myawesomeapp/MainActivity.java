package com.example.myawesomeapp;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.example.myawesomeapp.fcm.TokenRegistrar;
import com.getmoneytree.MoneytreeAuthOptions;
import com.getmoneytree.MoneytreeLink;
import com.getmoneytree.MoneytreeLinkException;
import com.getmoneytree.it.IsshoTsucho;
import com.getmoneytree.listener.Action;
import com.getmoneytree.listener.Api;
import com.getmoneytree.listener.Authorization;
import com.google.android.gms.security.ProviderInstaller;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

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
            getStatusTextView().setText(getString(R.string.token_message, accessToken));
          }

          @Override
          public void onError(@NonNull final MoneytreeLinkException exception) {
            if (exception.getError() == MoneytreeLinkException.Error.UNAUTHORIZED) {
              getStatusTextView().setText(R.string.error_no_token);
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
                getStatusTextView().setText(R.string.error_no_token);
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
        final MoneytreeAuthOptions options = new MoneytreeAuthOptions.Builder()
          // If you want to show the Login page (not Signup), set false or skip it
          .presentSignUp(true)
          // AuthorizationHandler is required for PKCE flow. Otherwise an app will get crashed.
          .authorizationHandler(
            new Authorization.OnCompletionListener() {
              @Override
              public void onSuccess(@NonNull final String accessToken) {
                getStatusTextView().setText(getString(R.string.token_message, accessToken));
              }

              @Override
              public void onError(@NonNull final MoneytreeLinkException exception) {
                getStatusTextView().setText(exception.getMessage());
              }
            }
          )
          // You can set default email address for the Signup/Login form if you know
          // .email("guest@email.com")
          .build(MoneytreeLink.client().getConfiguration());

        MoneytreeLink.client().authorizeFrom(MainActivity.this, options);
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
                getStatusTextView().setText(R.string.error_no_token);
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
        getStatusTextView().setText(R.string.deleted_token);
      }
    });

    // Set logout handler.
    MoneytreeLink.client().setLogoutHandler(
      this,
      new Action.OnCompletionListener() {
        @Override
        public void onSuccess() {
          // Logout success, change status to authorization required.
          getStatusTextView().setText(R.string.logout);
        }

        @Override
        public void onError(@NonNull final MoneytreeLinkException exception) {
          if (exception.getError() == MoneytreeLinkException.Error.UNAUTHORIZED) {
            getStatusTextView().setText(R.string.error_general);
          } else {
            getStatusTextView().setText(exception.getMessage());
          }
        }
      }
    );

    findViewById(R.id.logout_button).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        MoneytreeLink.client().logoutFrom(MainActivity.this);
      }
    });

    findViewById(R.id.patch_button).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(final View v) {
        try {
          ProviderInstaller.installIfNeeded(MainActivity.this);
          findViewById(R.id.patch_result).setVisibility(View.VISIBLE);
          ((TextView) findViewById(R.id.patch_result)).setText(R.string.kitkat_patch_done);
        } catch (Exception e) {
          findViewById(R.id.patch_result).setVisibility(View.VISIBLE);
          ((TextView) findViewById(R.id.patch_result)).setText(R.string.kitkat_patch_error);
        }
      }
    });
  }

  /**
   * Start the Issho Tsucho
   */
  private void startIsshoTsucho() {
    getStatusTextView().setText(R.string.it_launching);
    IsshoTsucho.client().startIsshoTsucho(new IsshoTsucho.OnCompletionListener() {
      @Override
      public void onLaunchedIsshoTsucho() {
        getStatusTextView().setText(R.string.it_success);
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
    FirebaseInstanceId
      .getInstance()
      .getInstanceId()
      .addOnSuccessListener(
        new OnSuccessListener<InstanceIdResult>() {
          @Override
          public void onSuccess(final InstanceIdResult instanceIdResult) {
            final String deviceToken = instanceIdResult.getToken();
            registerToken(deviceToken);
          }
        }
      );
  }

  /**
   * Remove the current token (from MT Server)
   */
  private void deregisterToken() {
    FirebaseInstanceId
      .getInstance()
      .getInstanceId()
      .addOnSuccessListener(
        new OnSuccessListener<InstanceIdResult>() {

          @Override
          public void onSuccess(final InstanceIdResult instanceIdResult) {
            final String deviceToken = instanceIdResult.getToken();
            deregisterToken(deviceToken);
          }
        }
      );
  }

  private TextView getStatusTextView() {
    return findViewById(R.id.result_text);
  }

  @Override
  public void registerToken(@NonNull String token) {
    getStatusTextView().setText(token);
    if (!MoneytreeLink.client().isLoggedIn()) {
      getStatusTextView().setText(R.string.error_no_token);
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
            getStatusTextView().setText(R.string.register_token_ok);
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
      getStatusTextView().setText(R.string.error_no_token);
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
            getStatusTextView().setText(R.string.unregister_token_ok);
          }

          @Override
          public void onError(@NonNull MoneytreeLinkException throwable) {
            getStatusTextView().setText(throwable.getMessage());
          }
        }
      );
  }
}
