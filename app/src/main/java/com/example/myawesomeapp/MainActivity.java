package com.example.myawesomeapp;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.TextView;

import com.example.myawesomeapp.fcm.TokenRegistrar;
import com.getmoneytree.MoneytreeAuthOptions;
import com.getmoneytree.MoneytreeLink;
import com.getmoneytree.MoneytreeLinkException;
import com.getmoneytree.VaultOpenServicesOptions;
import com.getmoneytree.it.IsshoTsucho;
import com.getmoneytree.listener.Action;
import com.getmoneytree.listener.Api;
import com.getmoneytree.listener.Authorization;
import com.google.firebase.iid.FirebaseInstanceId;

/**
 * A reference app that introduces what the SDK can do.
 *
 * @author Moneyteee KK
 */
public class MainActivity extends FragmentActivity implements TokenRegistrar {

  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    ////// Set up Issho Tsucho //////

    findViewById(R.id.issho_tsucho_button).setOnClickListener(v -> startIsshoTsucho());

    ////// Set up VaaS (If you use Issho Tsucho, you don't have to implement the below code) //////

    findViewById(R.id.token_button).setOnClickListener(view -> {
      // Need to pass activity when you get a token.
      MoneytreeLink.getInstance().getToken(new Authorization.OnCompletionListener() {

        @Override
        public void onSuccess(@NonNull final String accessToken) {
          // Runs when the SDK can offer a stored token to your app.
          getStatusTextView().setText(getString(R.string.token_message, accessToken));
        }

        @Override
        public void onError(@NonNull final MoneytreeLinkException exception) {
          // Runs in cases other than the situation described in `onSuccess`.
          if (exception.getError() == MoneytreeLinkException.Error.UNAUTHORIZED) {
            getStatusTextView().setText(R.string.error_no_token);
          } else {
            getStatusTextView().setText(exception.getMessage());
          }
        }
      });
    });

    findViewById(R.id.vault_button).setOnClickListener(
      v -> MoneytreeLink.getInstance().openVaultFrom(
        MainActivity.this,
        new Action.OnCompletionListener() {
          @Override
          public void onSuccess() {
            // Runs when the browser opens.
            getStatusTextView().setText(R.string.open_vault_success);
          }

          @Override
          public void onError(@NonNull final MoneytreeLinkException exception) {
            // Runs in cases other than the situation described in `onSuccess`.
            if (exception.getError() == MoneytreeLinkException.Error.UNAUTHORIZED) {
              getStatusTextView().setText(R.string.error_no_token);
            } else {
              getStatusTextView().setText(exception.getMessage());
            }
          }
        }
      )
    );

    findViewById(R.id.customer_support_button).setOnClickListener(
      v -> MoneytreeLink.getInstance().openCustomerSupport(
        MainActivity.this,
        new Action.OnCompletionListener() {
          @Override
          public void onSuccess() {
            // Runs when the browser opens.
            getStatusTextView().setText(R.string.open_customer_support_success);
          }

          @Override
          public void onError(@NonNull final MoneytreeLinkException exception) {
            // Runs in cases other than the situation described in `onSuccess`.
            if (exception.getError() == MoneytreeLinkException.Error.UNAUTHORIZED) {
              getStatusTextView().setText(R.string.error_no_token);
            } else {
              getStatusTextView().setText(exception.getMessage());
            }
          }
        }
      )
    );


    // connect service input and button
    final TextView connectServiceInput = findViewById(R.id.connect_service_key_input);
    final Button connectServiceButton = findViewById(R.id.connect_service_button);
    connectServiceButton.setEnabled(false);

    // disable connect service button if connect service key input has no value and vise-versa
    connectServiceInput.addTextChangedListener(new TextWatcher() {
      public void onTextChanged(CharSequence s, int start, int before, int count) {
        connectServiceButton.setEnabled(connectServiceInput.getText().length() != 0);
      }

      public void beforeTextChanged(CharSequence s, int start, int count, int after) {
      }

      public void afterTextChanged(Editable s) {
      }
    });

    connectServiceButton.setOnClickListener(v -> {
      final String serviceKey = connectServiceInput.getText().toString();

      MoneytreeLink.getInstance().connectService(
        MainActivity.this,
        serviceKey,
        new Action.OnCompletionListener() {
         @Override
         public void onSuccess() {
           // Runs when the browser opens.
           getStatusTextView().setText(R.string.connect_service_success);
         }

         @Override
         public void onError(@NonNull final MoneytreeLinkException exception) {
           // Runs in cases other than the situation described in `onSuccess`.
           if (exception.getError() == MoneytreeLinkException.Error.UNAUTHORIZED) {
             getStatusTextView().setText(R.string.error_no_token);
           } else {
             getStatusTextView().setText(exception.getMessage());
           }
         }
        }
      );
    });


    // open service setting input and button
    final TextView serviceSettingIdInput = findViewById(R.id.service_settings_id_input);
    final Button serviceSettingButton = findViewById(R.id.service_settings_button);
    serviceSettingButton.setEnabled(false);

    // disable connect service button if connect service key input has no value and vise-versa
    serviceSettingIdInput.addTextChangedListener(new TextWatcher() {
      public void onTextChanged(CharSequence s, int start, int before, int count) {
        serviceSettingButton.setEnabled(serviceSettingIdInput.getText().length() != 0);
      }

      public void beforeTextChanged(CharSequence s, int start, int count, int after) {
      }

      public void afterTextChanged(Editable s) {
      }
    });

    serviceSettingButton.setOnClickListener(v -> {
      final String serviceId = serviceSettingIdInput.getText().toString();

      MoneytreeLink.getInstance().serviceSettings(
        MainActivity.this,
        serviceId,
        new Action.OnCompletionListener() {
          @Override
          public void onSuccess() {
            // Runs when the browser opens.
            getStatusTextView().setText(R.string.open_service_setting_success);
          }

          @Override
          public void onError(@NonNull final MoneytreeLinkException exception) {
            // Runs in cases other than the situation described in `onSuccess`.
            if (exception.getError() == MoneytreeLinkException.Error.UNAUTHORIZED) {
              getStatusTextView().setText(R.string.error_no_token);
            } else {
              getStatusTextView().setText(exception.getMessage());
            }
          }
        }
      );
    });


    // open service setting input and button
    final TextView openServicesTypeInput = findViewById(R.id.open_services_type_input);
    final TextView openServicesGroupInput = findViewById(R.id.open_services_group_input);
    final TextView openServicesSearchInput = findViewById(R.id.open_services_search_input);

    findViewById(R.id.open_services_button).setOnClickListener(v -> {
      final VaultOpenServicesOptions options = new VaultOpenServicesOptions
        .Builder()
        .type(openServicesTypeInput.getText().toString())
        .group(openServicesGroupInput.getText().toString())
        .search(openServicesSearchInput.getText().toString())
        .build();

      MoneytreeLink.getInstance().openServices(
        MainActivity.this,
        options,
        new Action.OnCompletionListener() {
          @Override
          public void onSuccess() {
            // Runs when the browser opens.
            getStatusTextView().setText(R.string.open_services_success);
          }

          @Override
          public void onError(@NonNull final MoneytreeLinkException exception) {
            // Runs in cases other than the situation described in `onSuccess`.
            if (exception.getError() == MoneytreeLinkException.Error.UNAUTHORIZED) {
              getStatusTextView().setText(R.string.error_no_token);
            } else {
              getStatusTextView().setText(exception.getMessage());
            }
          }
        }
      );
    });


    findViewById(R.id.auth_button).setOnClickListener(v -> {
      final MoneytreeAuthOptions options = new MoneytreeAuthOptions.Builder()
        // If you want to show the Login page (not Signup), set false or skip it
        .presentSignUp(true)
        // AuthorizationHandler is required only for PKCE flow.
        .authorizationHandler(
          new Authorization.OnCompletionListener() {
            @Override
            public void onSuccess(@NonNull final String accessToken) {
              // Runs after an user completes authorization flow (only in PKCE).
              getStatusTextView().setText(getString(R.string.token_message, accessToken));
            }

            @Override
            public void onError(@NonNull final MoneytreeLinkException exception) {
              // Runs in cases other than the situation described in `onSuccess`.
              getStatusTextView().setText(exception.getMessage());
            }
          }
        )
        // You have to call this handler instead of the handler above when you don't use PKCE flow
        //.codeGrantTypeOptions(...)
        // You can set default email address for the Signup/Login form
        //.email("guest@email.com")
        .build(MoneytreeLink.getInstance().getConfiguration());

      MoneytreeLink.getInstance().authorizeFrom(MainActivity.this, options);
    });

    findViewById(R.id.settings_button).setOnClickListener(
      v -> MoneytreeLink.getInstance().openSettingsFrom(
        MainActivity.this,
        new Action.OnCompletionListener() {
          @Override
          public void onSuccess() {
            // Runs when the browser opens.
            getStatusTextView().setText(R.string.open_settings_success);
          }

          @Override
          public void onError(@NonNull final MoneytreeLinkException exception) {
            // Runs in cases other than the situation described in `onSuccess`.
            if (exception.getError() == MoneytreeLinkException.Error.UNAUTHORIZED) {
              getStatusTextView().setText(R.string.error_no_token);
            } else {
              getStatusTextView().setText(exception.getMessage());
            }
          }
        }
      )
    );

    findViewById(R.id.register_button).setOnClickListener(v -> registerToken());

    findViewById(R.id.deregister_button).setOnClickListener(v -> deregisterToken());

    getStatusTextView().setText(
      MoneytreeLink.getInstance().isLoggedIn() ? "Logged In" : "Unauthorized"
    );

    findViewById(R.id.reset_button).setOnClickListener(view -> {
      MoneytreeLink.getInstance().deleteCredentials();
      getStatusTextView().setText(R.string.deleted_token);
    });

    // Set logout handler.
    MoneytreeLink.getInstance().setLogoutHandler(
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

    findViewById(R.id.logout_button).setOnClickListener(
      v -> MoneytreeLink.getInstance().logoutFrom(MainActivity.this)
    );
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
        instanceIdResult -> {
          final String deviceToken = instanceIdResult.getToken();
          registerToken(deviceToken);
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
        instanceIdResult -> {
          final String deviceToken = instanceIdResult.getToken();
          deregisterToken(deviceToken);
        }
      );
  }

  private TextView getStatusTextView() {
    return findViewById(R.id.result_text);
  }

  @Override
  public void registerToken(@NonNull String token) {
    getStatusTextView().setText(token);
    if (!MoneytreeLink.getInstance().isLoggedIn()) {
      getStatusTextView().setText(R.string.error_no_token);
      return;
    }

    MoneytreeLink
      .getInstance()
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
    if (!MoneytreeLink.getInstance().isLoggedIn()) {
      getStatusTextView().setText(R.string.error_no_token);
      return;
    }

    MoneytreeLink
      .getInstance()
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
