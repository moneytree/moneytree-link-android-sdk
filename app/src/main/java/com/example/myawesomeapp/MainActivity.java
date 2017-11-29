package com.example.myawesomeapp;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myawesomeapp.databinding.ActivityMainBinding;
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

  @NonNull
  private ActivityMainBinding binding;

  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

    binding.isshoTsuchoButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        startIsshoTsucho();
      }
    });

    ////// Set up VaaS (If you use Issho Tsucho, you don't have to implement the below code) //////

    // Set this activity as the root view for the SDK.
    MoneytreeLink.client().setRootView(this);
    // Set the default OAuth handler.
    MoneytreeLink.client().setAuthzTokenHandler(getHandler());

    binding.tokenButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(final View view) {
        MoneytreeLink.client().getToken(new CompletionHandler() {
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

    binding.vaultButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        MoneytreeLink.client().openVaultFrom(MainActivity.this);
      }
    });

    binding.authButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        MoneytreeLink.client().authorizeFrom(MainActivity.this);
      }
    });

    binding.settingsButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        MoneytreeLink.client().openSettingsFrom(MainActivity.this);
      }
    });

    binding.institutionButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        MoneytreeLink.client().openInstitutionFrom(
            MainActivity.this,
            "fauxbank_test_bank"
        );
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

    binding.resetButton.setOnClickListener(new View.OnClickListener() {
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
  private CompletionHandler getHandler() {

    return new CompletionHandler() {
      @Override
      public void onSuccess(@NonNull final String accessToken) {
        getStatusTextView().setText("Token: " + accessToken);
      }

      @Override
      public void onError(@NonNull final MoneytreeLinkException ex) {
        ex.printStackTrace();
        getStatusTextView().setText("Error: " + ex.getMessage());
      }
    };
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
            }
        );
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
            }
        );
  }
}
