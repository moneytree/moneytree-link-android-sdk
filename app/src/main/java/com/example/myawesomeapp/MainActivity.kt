package com.example.myawesomeapp

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import com.example.myawesomeapp.fcm.TokenRegistrar
import com.getmoneytree.Region
import com.getmoneytree.MoneytreeAuthOptions
import com.getmoneytree.MoneytreeLink
import com.getmoneytree.MoneytreeLinkException
import com.getmoneytree.VaultOpenServicesOptions
import com.getmoneytree.it.IsshoTsucho
import com.getmoneytree.listener.Action
import com.getmoneytree.listener.Api
import com.getmoneytree.listener.Authorization
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

  @Suppress("ConstantConditionIf")
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    ////// Set up Issho Tsucho //////

    findViewById<Button>(R.id.issho_tsucho_button)
      .setOnClickListener { startIsshoTsucho() }

    ////// Set up VaaS (If you use Issho Tsucho, you don't have to implement the below code) //////

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
      MoneytreeLink.getInstance().openVaultFrom(
        this@MainActivity,
        object : Action.OnCompletionListener {
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
        }
      )
    }

    findViewById<Button>(R.id.customer_support_button).setOnClickListener { view ->
      MoneytreeLink.getInstance().openCustomerSupport(
        this@MainActivity,
        object : Action.OnCompletionListener {
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
        connectServiceButton.isEnabled = connectServiceInput.text.isNotEmpty()
      }

      override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

      override fun afterTextChanged(s: Editable) {}
    })

    findViewById<Button>(R.id.connect_service_button).setOnClickListener { view ->
      val serviceKey = connectServiceInput.text.toString()

      MoneytreeLink.getInstance().connectService(
        this@MainActivity,
        serviceKey,
        object : Action.OnCompletionListener {
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
        serviceSettingButton.isEnabled = serviceSettingIdInput.text.isNotEmpty()
      }

      override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

      override fun afterTextChanged(s: Editable) {}
    })

    findViewById<Button>(R.id.service_settings_button).setOnClickListener { view ->
      val serviceKey = serviceSettingIdInput.text.toString()

      MoneytreeLink.getInstance().serviceSettings(
        this@MainActivity,
        serviceKey,
        object : Action.OnCompletionListener {
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
        }
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

      MoneytreeLink.getInstance().openServices(
        this@MainActivity,
        options,
        object : Action.OnCompletionListener {
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
        }
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
        //.email("you@example.com")
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

    findViewById<Button>(R.id.onboard_button).setOnClickListener { view ->
      val email = findViewById<TextView>(R.id.onboard_input).editableText.toString()
      if (email.isEmpty()) {
        showError(view, "Email is required.")
        return@setOnClickListener
      }

      val options = MoneytreeAuthOptions.Builder()
        .apply {
          if (BuildConfig.authType == AuthType.PKCE) {
            // AuthorizationHandler is required only for PKCE flow.
            authorizationHandler(authHandlerForPKCE)
          } else {
            // You have to invoke this when you choose Code Grant flow.
            codeGrantTypeOptions(codeGrantOption)
          }
        }
        // Email and region are required for onboard.
        .email(email)
        .region(Region.JAPAN)
        .build(MoneytreeLink.getInstance().configuration)

      MoneytreeLink.getInstance().onboardFrom(
        this@MainActivity,
        options
      )
    }

    val spinner = findViewById<Spinner>(R.id.magiclink_destination)
    // Populate data for spinner.
    // https://developer.android.com/guide/topics/ui/controls/spinner#Populate
    KeyValueAdapter(this, android.R.layout.simple_spinner_item)
      .apply {
        addAll(
          listOf(
            Pair("/settings", "Settings Page"),
            Pair("/settings/change-language", "Change Moneytree Service Language"),
            Pair("/settings/authorized-applications", "Authorized Apps")
          )
        )
      }
      .also { adapter ->
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
      }

    findViewById<Button>(R.id.magiclink_button).setOnClickListener { view ->
      val email = findViewById<EditText>(R.id.magiclink_email).text.toString()
      if (email.isEmpty()) {
        showError(view, "Email is required.")
        return@setOnClickListener
      }

      val selectedItem = spinner.selectedItem as Pair<String, String>
      MoneytreeLink.getInstance().requestMagicLink(
        email,
        selectedItem.first,
        object : Action.OnCompletionListener {
          override fun onSuccess() {
            showMessage(view, "Requested magic link. Check your inbox.")
          }

          override fun onError(exception: MoneytreeLinkException) {
            showError(view, exception.message)
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

  override fun onNewIntent(intent: Intent?) {
    super.onNewIntent(intent)
    // Handle magic link action
    MoneytreeLink.getInstance().consumeMagicLink(
      this,
      intent, null
    )
    { error ->
      showError(rootView, error.message)
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
  private fun startIsshoTsucho() {
    showMessage(rootView, getString(R.string.it_launching))
    IsshoTsucho.client().startIsshoTsucho(object : IsshoTsucho.OnCompletionListener {
      override fun onLaunchedIsshoTsucho() {
        showMessage(rootView, getString(R.string.it_success))
      }

      override fun onFailedToLaunch(e: MoneytreeLinkException) {
        showError(rootView, e.message)
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
}
