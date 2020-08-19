package com.example.myawesomeapp

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.myawesomeapp.fcm.TokenRegistrar
import com.getmoneytree.LinkRequestContext
import com.getmoneytree.MoneytreeAuthOptions
import com.getmoneytree.MoneytreeLink
import com.getmoneytree.MoneytreeLinkException
import com.getmoneytree.VaultOpenServicesOptions
import com.getmoneytree.it.IsshoTsucho
import com.getmoneytree.listener.Action
import com.getmoneytree.listener.Api
import com.getmoneytree.listener.Authorization
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.iid.FirebaseInstanceId
import java.util.UUID

/**
 * A reference app that introduces what the SDK can do.
 *
 * @author Moneyteee KK
 */
class MainActivity : AppCompatActivity(), TokenRegistrar {

  private val statusTextView: TextView
    get() = findViewById(R.id.result_text)

  private val rootView: View
    get() = findViewById(R.id.scroll_view)

  private val vaultReqCode = 10

  @Suppress("ConstantConditionIf")
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    // //// Set up Issho Tsucho //////

    findViewById<Button>(R.id.link_kit_button)
      .setOnClickListener { startLinkKit() }

    // //// Set up VaaS (If you use Issho Tsucho, you don't have to implement the below code) //////

    findViewById<Button>(R.id.token_button).setOnClickListener { view ->
      // Need to pass activity when you get a token.
      MoneytreeLink.getInstance().getToken(
        object : Authorization.OnCompletionListener {

          override fun onSuccess(accessToken: String) {
            // Runs when the SDK can offer a stored token to your app.
            showMessage(view, getString(R.string.token_message, accessToken))
          }

          override fun onError(exception: MoneytreeLinkException) {
            // Runs in cases other than the situation described in `onSuccess`.
            if (exception.error == MoneytreeLinkException.Error.UNAUTHORIZED) {
              showError(view, getString(R.string.error_no_token))
            } else {
              showError(view, exception.message)
            }
          }
        }
      )
    }

    findViewById<Button>(R.id.vault_button).setOnClickListener { view ->
      MoneytreeLink.getInstance().openVault(
        vaultReqCode,
        LinkRequestContext.Builder.from(this@MainActivity)
          .listener(object : Action.OnCompletionListener {
            override fun onSuccess() {
              // Runs when the browser opens.
              showMessage(view, getString(R.string.open_vault_success))
            }

            override fun onError(exception: MoneytreeLinkException) {
              // Runs in cases other than the situation described in `onSuccess`.
              if (exception.error == MoneytreeLinkException.Error.UNAUTHORIZED) {
                showError(view, getString(R.string.error_no_token))
              } else {
                showError(view, exception.message)
              }
            }
          })
          .guestEmail(findViewById<EditText>(R.id.auth_email_edit).text.toString())
          .build()
      )
    }

    findViewById<Button>(R.id.customer_support_button).setOnClickListener { view ->
      MoneytreeLink.getInstance().openVault(
        vaultReqCode,
        LinkRequestContext.Builder.from(this@MainActivity)
          .listener(object : Action.OnCompletionListener {
            override fun onSuccess() {
              // Runs when the browser opens.
              showMessage(view, getString(R.string.open_customer_support_success))
            }

            override fun onError(exception: MoneytreeLinkException) {
              // Runs in cases other than the situation described in `onSuccess`.
              if (exception.error == MoneytreeLinkException.Error.UNAUTHORIZED) {
                showError(view, getString(R.string.error_no_token))
              } else {
                showError(view, exception.message)
              }
            }
          })
          .path(MoneytreeLink.VAULT_SUPPORT)
          .build()
      )
    }

    // connect service input and button
    val connectServiceInput = findViewById<TextView>(R.id.connect_service_key_input)
    val connectServiceButton = findViewById<Button>(R.id.connect_service_button)
    connectServiceButton.isEnabled = false

    // disable connect service button if connect service key input has no value and vise-versa
    connectServiceInput.addTextChangedListener(object : TextWatcher {
      override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        connectServiceButton.isEnabled = connectServiceInput.text.isNotEmpty()
      }

      override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

      override fun afterTextChanged(s: Editable) {}
    })

    findViewById<Button>(R.id.connect_service_button).setOnClickListener { view ->
      val serviceKey = connectServiceInput.text.toString()

      MoneytreeLink.getInstance().openVault(
        vaultReqCode,
        LinkRequestContext.Builder.from(this@MainActivity)
          .path(MoneytreeLink.VAULT_SERVICE)
          .pathSuffix(serviceKey)
          .listener(object : Action.OnCompletionListener {
            override fun onSuccess() {
              // Runs when the browser opens.
              showMessage(view, getString(R.string.connect_service_success))
            }

            override fun onError(exception: MoneytreeLinkException) {
              // Runs in cases other than the situation described in `onSuccess`.
              if (exception.error == MoneytreeLinkException.Error.UNAUTHORIZED) {
                showError(view, getString(R.string.error_no_token))
              } else {
                showError(view, exception.message)
              }
            }
          })
          .build()
      )
    }

    // connect service input and button
    val serviceSettingIdInput = findViewById<TextView>(R.id.service_settings_id_input)
    val serviceSettingButton = findViewById<Button>(R.id.service_settings_button)
    serviceSettingButton.isEnabled = false

    // disable connect service button if connect service key input has no value and vise-versa
    serviceSettingIdInput.addTextChangedListener(object : TextWatcher {
      override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        serviceSettingButton.isEnabled = serviceSettingIdInput.text.isNotEmpty()
      }

      override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

      override fun afterTextChanged(s: Editable) {}
    })

    findViewById<Button>(R.id.service_settings_button).setOnClickListener { view ->
      val serviceKey = serviceSettingIdInput.text.toString()

      MoneytreeLink.getInstance().openVault(
        vaultReqCode,
        LinkRequestContext.Builder.from(this@MainActivity)
          .path(MoneytreeLink.VAULT_SERVICE_SETTINGS)
          .pathSuffix(serviceKey)
          .listener(object : Action.OnCompletionListener {
            override fun onSuccess() {
              // Runs when the browser opens.
              showMessage(view, getString(R.string.open_service_setting_success))
            }

            override fun onError(exception: MoneytreeLinkException) {
              // Runs in cases other than the situation described in `onSuccess`.
              if (exception.error == MoneytreeLinkException.Error.UNAUTHORIZED) {
                showError(view, getString(R.string.error_no_token))
              } else {
                showError(view, exception.message)
              }
            }
          })
          .build()
      )
    }

    // open service setting input and button
    val openServicesTypeInput = findViewById<TextView>(R.id.open_services_type_input)
    val openServicesGroupInput = findViewById<TextView>(R.id.open_services_group_input)
    val openServicesSearchInput = findViewById<TextView>(R.id.open_services_search_input)

    findViewById<View>(R.id.open_services_button).setOnClickListener { view ->
      val options = VaultOpenServicesOptions.Builder()
        .type(openServicesTypeInput.text.toString())
        .group(openServicesGroupInput.text.toString())
        .search(openServicesSearchInput.text.toString())
        .build()

      MoneytreeLink.getInstance().openVault(
        vaultReqCode,
        LinkRequestContext.Builder.from(this@MainActivity)
          .vaultOpenServicesOptions(options)
          .listener(object : Action.OnCompletionListener {
            override fun onSuccess() {
              // Runs when the browser opens.
              showMessage(view, getString(R.string.open_services_success))
            }

            override fun onError(exception: MoneytreeLinkException) {
              // Runs in cases other than the situation described in `onSuccess`.
              if (exception.error == MoneytreeLinkException.Error.UNAUTHORIZED) {
                showError(view, getString(R.string.error_no_token))
              } else {
                showError(view, exception.message)
              }
            }
          })
          .build()
      )
    }

    val authHandlerForPKCE = object : Authorization.OnCompletionListener {
      // Runs after an user completes authorization flow.
      override fun onSuccess(accessToken: String) {
        showMessage(rootView, getString(R.string.token_message, accessToken))
      }

      // Runs in cases other than the situation described in `onSuccess`.
      override fun onError(exception: MoneytreeLinkException) {
        showError(rootView, exception.message)
      }
    }

    val codeGrantOption =
      MoneytreeAuthOptions.CodeGrantTypeOptions.Builder()
        .completionHandler(object : Action.OnCompletionListener {
          override fun onSuccess() {
            showMessage(rootView, "Success!")
          }

          override fun onError(exception: MoneytreeLinkException) {
            showError(rootView, exception.message)
          }
        })
        .setState(UUID.randomUUID().toString())
        .build()

    findViewById<Button>(R.id.auth_button).setOnClickListener {
      val options = MoneytreeAuthOptions.Builder()
        // If you want to show the Login page (not Signup), set false or skip it
        .presentSignUp(true)
        .forceLogout(findViewById<CheckBox>(R.id.force_logout).isChecked)
        .apply {
          if (BuildConfig.authType == AuthType.PKCE) {
            // AuthorizationHandler is required only for PKCE flow.
            authorizationHandler(authHandlerForPKCE)
          } else {
            // You have to invoke this when you choose Code Grant flow.
            codeGrantTypeOptions(codeGrantOption)
          }
        }
        // You can set default email address for the Signup/Login form
        // .email("you@example.com")
        .email(findViewById<EditText>(R.id.auth_email_edit).text?.toString())
        .build(MoneytreeLink.getInstance().configuration)

      MoneytreeLink.getInstance().authorizeFrom(
        this@MainActivity,
        options
      )
    }

    findViewById<Button>(R.id.settings_button).setOnClickListener { view ->
      MoneytreeLink.getInstance().openSettingsFrom(
        this@MainActivity,
        object : Action.OnCompletionListener {
          override fun onSuccess() {
            // Runs when the browser opens.
            showMessage(view, getString(R.string.open_settings_success))
          }

          override fun onError(exception: MoneytreeLinkException) {
            // Runs in cases other than the situation described in `onSuccess`.
            if (exception.error == MoneytreeLinkException.Error.UNAUTHORIZED) {
              showError(view, getString(R.string.error_no_token))
            } else {
              showError(view, exception.message)
            }
          }
        }
      )
    }

    findViewById<Button>(R.id.register_button)
      .setOnClickListener { registerToken() }

    findViewById<Button>(R.id.deregister_button)
      .setOnClickListener { deregisterToken() }

    statusTextView.text = getString(
      R.string.welcome,
      if (BuildConfig.authType == AuthType.PKCE) "PKCE" else "Code Grant",
      if (BuildConfig.isProduction) "Production" else "Staging"
    )

    findViewById<Button>(R.id.reset_button).setOnClickListener { view ->
      MoneytreeLink.getInstance().deleteCredentials()
      showMessage(view, getString(R.string.deleted_token))
    }

    // Set logout handler.
    MoneytreeLink.getInstance().setLogoutHandler(
      this,
      object : Action.OnCompletionListener {
        override fun onSuccess() {
          // Logout success, change status to authorization required.
          showMessage(rootView, getString(R.string.logout))
        }

        override fun onError(exception: MoneytreeLinkException) {
          if (exception.error == MoneytreeLinkException.Error.UNAUTHORIZED) {
            showError(rootView, getString(R.string.error_general))
          } else {
            showError(rootView, exception.message)
          }
        }
      }
    )

    findViewById<View>(R.id.logout_button).setOnClickListener {
      MoneytreeLink.getInstance().logoutFrom(this@MainActivity)
    }
  }

  private fun showMessage(view: View, message: String?) {
    message ?: return
    Snackbar
      .make(view, message, Snackbar.LENGTH_SHORT)
      .apply {
        getView().backgroundTintList = ColorStateList.valueOf(
          ContextCompat.getColor(
            this@MainActivity,
            android.R.color.holo_green_dark
          )
        )
      }
      .run { show() }
      .also { statusTextView.text = message }
  }

  private fun showError(view: View, message: String?) {
    message ?: return
    Snackbar
      .make(view, message, Snackbar.LENGTH_SHORT)
      .apply {
        getView().backgroundTintList = ColorStateList.valueOf(
          ContextCompat.getColor(
            this@MainActivity,
            android.R.color.holo_red_light
          )
        )
      }
      .run { show() }
      .also { statusTextView.text = message }
  }

  /**
   * Start the Issho Tsucho
   */
  private fun startLinkKit() {
    showMessage(rootView, getString(R.string.it_launching))
    IsshoTsucho.client().startIsshoTsucho(
      this,
      object : IsshoTsucho.OnCompletionListener {
        override fun onLaunchedIsshoTsucho() {
          showMessage(rootView, getString(R.string.it_success))
        }

        override fun onFailedToLaunch(e: MoneytreeLinkException) {
          showError(rootView, e.message)
        }
      }
    )
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
    if (!MoneytreeLink.getInstance().isLoggedIn) {
      showError(rootView, getString(R.string.error_no_token))
      return
    }

    MoneytreeLink
      .getInstance()
      .registerDeviceTokenFrom(
        this,
        token,
        object : Api.OnCompletionListener {
          override fun onSuccess() {
            showMessage(rootView, getString(R.string.register_token_ok))
          }

          override fun onError(throwable: MoneytreeLinkException) {
            showError(rootView, throwable.message)
          }
        }
      )
  }

  override fun deregisterToken(token: String) {
    if (!MoneytreeLink.getInstance().isLoggedIn) {
      showError(rootView, getString(R.string.error_no_token))
      return
    }

    MoneytreeLink
      .getInstance()
      .unregisterDeviceTokenFrom(
        this,
        token,
        object : Api.OnCompletionListener {
          override fun onSuccess() {
            showMessage(rootView, getString(R.string.unregister_token_ok))
          }

          override fun onError(throwable: MoneytreeLinkException) {
            showError(rootView, throwable.message)
          }
        }
      )
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    val vaultClosed = requestCode == vaultReqCode &&
      resultCode == MoneytreeLink.RESULT_CODE_VAULT_CLOSED
    Toast.makeText(
      this,
      "Vault closed result: $vaultClosed",
      Toast.LENGTH_SHORT
    ).show()
  }
}
