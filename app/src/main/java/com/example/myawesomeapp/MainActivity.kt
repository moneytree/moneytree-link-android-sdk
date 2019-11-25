package com.example.myawesomeapp

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.TextView

import com.example.myawesomeapp.fcm.TokenRegistrar
import com.getmoneytree.MoneytreeAuthOptions
import com.getmoneytree.MoneytreeLink
import com.getmoneytree.MoneytreeLinkException
import com.getmoneytree.VaultOpenServicesOptions
import com.getmoneytree.it.IsshoTsucho
import com.getmoneytree.listener.Action
import com.getmoneytree.listener.Api
import com.getmoneytree.listener.Authorization
import com.google.firebase.iid.FirebaseInstanceId

/**
 * A reference app that introduces what the SDK can do.
 *
 * @author Moneyteee KK
 */
class MainActivity : AppCompatActivity(), TokenRegistrar {

  private val statusTextView: TextView
    get() = findViewById(R.id.result_text)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    ////// Set up Issho Tsucho //////

    findViewById<Button>(R.id.issho_tsucho_button)
      .setOnClickListener { startIsshoTsucho() }

    ////// Set up VaaS (If you use Issho Tsucho, you don't have to implement the below code) //////

    findViewById<Button>(R.id.token_button).setOnClickListener {
      // Need to pass activity when you get a token.
      MoneytreeLink.getInstance().getToken(
          object : Authorization.OnCompletionListener {

            override fun onSuccess(
                accessToken: String
            ) {
              // Runs when the SDK can offer a stored token to your app.
              statusTextView.text = getString(
                  R.string.token_message,
                  accessToken
              )
            }

            override fun onError(exception: MoneytreeLinkException) {
              // Runs in cases other than the situation described in `onSuccess`.
              if (exception.error == MoneytreeLinkException.Error.UNAUTHORIZED) {
                statusTextView.setText(R.string.error_no_token)
              } else {
                statusTextView.text = exception.message
              }
            }
          }
      )
    }

    findViewById<Button>(R.id.vault_button).setOnClickListener {
      MoneytreeLink.getInstance().openVaultFrom(
          this@MainActivity,
          object : Action.OnCompletionListener {
            override fun onSuccess() {
              // Runs when the browser opens.
              statusTextView.setText(R.string.open_vault_success)
            }

            override fun onError(exception: MoneytreeLinkException) {
              // Runs in cases other than the situation described in `onSuccess`.
              if (exception.error == MoneytreeLinkException.Error.UNAUTHORIZED) {
                statusTextView.setText(R.string.error_no_token)
              } else {
                statusTextView.text = exception.message
              }
            }
          }
      )
    }

    findViewById<Button>(R.id.customer_support_button).setOnClickListener {
      MoneytreeLink.getInstance().openCustomerSupport(
        this@MainActivity,
        object : Action.OnCompletionListener {
          override fun onSuccess() {
            // Runs when the browser opens.
            statusTextView.setText(R.string.open_customer_support_success)
          }

          override fun onError(exception: MoneytreeLinkException) {
            // Runs in cases other than the situation described in `onSuccess`.
            if (exception.error == MoneytreeLinkException.Error.UNAUTHORIZED) {
              statusTextView.setText(R.string.error_no_token)
            } else {
              statusTextView.text = exception.message
            }
          }
        }
      )
    }

    // connect service input and button
    val connectServiceInput = findViewById<TextView>(R.id.connect_service_key_input)
    val connectServiceButton = findViewById<Button>(R.id.connect_service_button)
    connectServiceButton.isEnabled = false

    // disable connect service button if connect service key input has no value and vise-versa
    connectServiceInput.addTextChangedListener(object : TextWatcher {
      override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        connectServiceButton.isEnabled = connectServiceInput.text.length != 0
      }

      override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

      override fun afterTextChanged(s: Editable) {}
    })

    findViewById<Button>(R.id.connect_service_button).setOnClickListener {
      val serviceKey = connectServiceInput.text.toString()

      MoneytreeLink.getInstance().connectService(
          this@MainActivity,
          serviceKey,
          object : Action.OnCompletionListener {
            override fun onSuccess() {
              // Runs when the browser opens.
              statusTextView.setText(R.string.connect_service_success)
            }

            override fun onError(exception: MoneytreeLinkException) {
              // Runs in cases other than the situation described in `onSuccess`.
              if (exception.error == MoneytreeLinkException.Error.UNAUTHORIZED) {
                statusTextView.setText(R.string.error_no_token)
              } else {
                statusTextView.text = exception.message
              }
            }
          }
      )
    }


    // connect service input and button
    val serviceSettingIdInput = findViewById<TextView>(R.id.service_settings_id_input)
    val serviceSettingButton = findViewById<Button>(R.id.service_settings_button)
    serviceSettingButton.isEnabled = false

    // disable connect service button if connect service key input has no value and vise-versa
    serviceSettingIdInput.addTextChangedListener(object : TextWatcher {
      override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        serviceSettingButton.isEnabled = serviceSettingIdInput.text.length != 0
      }

      override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

      override fun afterTextChanged(s: Editable) {}
    })

    findViewById<Button>(R.id.service_settings_button).setOnClickListener {
      val serviceKey = serviceSettingIdInput.text.toString()

      MoneytreeLink.getInstance().serviceSettings(
          this@MainActivity,
          serviceKey,
          object: Action.OnCompletionListener {
            override fun onSuccess() {
              // Runs when the browser opens.
              statusTextView.setText(R.string.open_service_setting_success)
            }

            override fun onError(exception: MoneytreeLinkException) {
              // Runs in cases other than the situation described in `onSuccess`.
              if (exception.error == MoneytreeLinkException.Error.UNAUTHORIZED) {
                statusTextView.setText(R.string.error_no_token)
              } else {
                statusTextView.text = exception.message
              }
            }
          }
      )
    }


    // open service setting input and button
    val openServicesTypeInput = findViewById<TextView>(R.id.open_services_type_input)
    val openServicesGroupInput = findViewById<TextView>(R.id.open_services_group_input)
    val openServicesSearchInput = findViewById<TextView>(R.id.open_services_search_input)

    findViewById<View>(R.id.open_services_button).setOnClickListener { v ->
      val options = VaultOpenServicesOptions.Builder()
        .type(openServicesTypeInput.text.toString())
        .group(openServicesGroupInput.text.toString())
        .search(openServicesSearchInput.text.toString())
        .build()

      MoneytreeLink.getInstance().openServices(
          this@MainActivity,
          options,
          object: Action.OnCompletionListener {
            override fun onSuccess() {
              // Runs when the browser opens.
              statusTextView.setText(R.string.open_services_success)
            }

            override fun onError(exception: MoneytreeLinkException) {
              // Runs in cases other than the situation described in `onSuccess`.
              if (exception.error == MoneytreeLinkException.Error.UNAUTHORIZED) {
                statusTextView.setText(R.string.error_no_token)
              } else {
                statusTextView.text = exception.message
              }
            }
          }
      )
    }


    findViewById<Button>(R.id.auth_button).setOnClickListener {
      val options = MoneytreeAuthOptions.Builder()
        // If you want to show the Login page (not Signup), set false or skip it
        .presentSignUp(true)
        // AuthorizationHandler is required only for PKCE flow.
        .authorizationHandler(
            object : Authorization.OnCompletionListener {
              override fun onSuccess(
                  accessToken: String
              ) {
                // Runs after an user completes authorization flow (only in PKCE).
                statusTextView.text = getString(
                    R.string.token_message,
                    accessToken
                )
              }

              override fun onError(exception: MoneytreeLinkException) {
                // Runs in cases other than the situation described in `onSuccess`.
                statusTextView.text = exception.message
              }
            }
        )
        // You have to call this handler instead of the handler above when you don't use PKCE flow
        //.codeGrantTypeOptions(...)
        // You can set default email address for the Signup/Login form
        //.email("guest@email.com")
        .build(MoneytreeLink.getInstance().configuration)

      MoneytreeLink.getInstance().authorizeFrom(
          this@MainActivity,
          options
      )
    }

    findViewById<Button>(R.id.settings_button).setOnClickListener {
      MoneytreeLink.getInstance().openSettingsFrom(
          this@MainActivity,
          object : Action.OnCompletionListener {
            override fun onSuccess() {
              // Runs when the browser opens.
              statusTextView.setText(R.string.open_settings_success)
            }

            override fun onError(exception: MoneytreeLinkException) {
              // Runs in cases other than the situation described in `onSuccess`.
              if (exception.error == MoneytreeLinkException.Error.UNAUTHORIZED) {
                statusTextView.setText(R.string.error_no_token)
              } else {
                statusTextView.text = exception.message
              }
            }
          }
      )
    }

    findViewById<Button>(R.id.register_button)
      .setOnClickListener { registerToken() }

    findViewById<Button>(R.id.deregister_button)
      .setOnClickListener { deregisterToken() }

    statusTextView.text = if (MoneytreeLink.getInstance().isLoggedIn) "Logged In" else "Unauthorized"

    findViewById<Button>(R.id.reset_button).setOnClickListener {
      MoneytreeLink.getInstance().deleteCredentials()
      statusTextView.setText(R.string.deleted_token)
    }

    // Set logout handler.
    MoneytreeLink.getInstance().setLogoutHandler(
        this,
        object : Action.OnCompletionListener {
          override fun onSuccess() {
            // Logout success, change status to authorization required.
            statusTextView.setText(R.string.logout)
          }

          override fun onError(exception: MoneytreeLinkException) {
            if (exception.error == MoneytreeLinkException.Error.UNAUTHORIZED) {
              statusTextView.setText(R.string.error_general)
            } else {
              statusTextView.text = exception.message
            }
          }
        }
    )

    findViewById<View>(R.id.logout_button).setOnClickListener {
      MoneytreeLink.getInstance().logoutFrom(this@MainActivity)
    }
  }

  /**
   * Start the Issho Tsucho
   */
  private fun startIsshoTsucho() {
    statusTextView.setText(R.string.it_launching)
    IsshoTsucho.client().startIsshoTsucho(object : IsshoTsucho.OnCompletionListener {
      override fun onLaunchedIsshoTsucho() {
        statusTextView.setText(R.string.it_success)
      }

      override fun onFailedToLaunch(e: MoneytreeLinkException) {
        statusTextView.text = e.localizedMessage
        e.printStackTrace()
      }
    })
  }

  /**
   * Register the current token (from MT Server)
   */
  private fun registerToken() {
    FirebaseInstanceId
      .getInstance()
      .instanceId
      .addOnSuccessListener { instanceIdResult ->
        val deviceToken = instanceIdResult.token
        registerToken(deviceToken)
      }
  }

  /**
   * Remove the current token (from MT Server)
   */
  private fun deregisterToken() {
    FirebaseInstanceId
      .getInstance()
      .instanceId
      .addOnSuccessListener { instanceIdResult ->
        val deviceToken = instanceIdResult.token
        deregisterToken(deviceToken)
      }
  }

  override fun registerToken(token: String) {
    statusTextView.text = token
    if (!MoneytreeLink.getInstance().isLoggedIn) {
      statusTextView.setText(R.string.error_no_token)
      return
    }

    MoneytreeLink
      .getInstance()
      .registerDeviceTokenFrom(
          this,
          token,
          object : Api.OnCompletionListener {
            override fun onSuccess() {
              statusTextView.setText(R.string.register_token_ok)
            }

            override fun onError(throwable: MoneytreeLinkException) {
              statusTextView.text = throwable.message
            }
          }
      )
  }

  override fun deregisterToken(token: String) {
    statusTextView.text = token
    if (!MoneytreeLink.getInstance().isLoggedIn) {
      statusTextView.setText(R.string.error_no_token)
      return
    }

    MoneytreeLink
      .getInstance()
      .unregisterDeviceTokenFrom(
          this,
          token,
          object : Api.OnCompletionListener {
            override fun onSuccess() {
              statusTextView.setText(R.string.unregister_token_ok)
            }

            override fun onError(throwable: MoneytreeLinkException) {
              statusTextView.text = throwable.message
            }
          }
      )
  }
}
