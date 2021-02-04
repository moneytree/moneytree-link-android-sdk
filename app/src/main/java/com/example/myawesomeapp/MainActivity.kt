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
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.getmoneytree.LinkAuthFlow
import com.getmoneytree.LinkAuthOptions
import com.getmoneytree.LinkError
import com.getmoneytree.LinkEvent
import com.getmoneytree.LinkRequestContext
import com.getmoneytree.MoneytreeLink
import com.getmoneytree.MoneytreeLinkException
import com.getmoneytree.MoneytreeLinkExtensions.onCodeGrantAuthorized
import com.getmoneytree.MoneytreeLinkExtensions.onError
import com.getmoneytree.MoneytreeLinkExtensions.onEvent
import com.getmoneytree.MoneytreeLinkExtensions.onLoggedOut
import com.getmoneytree.MoneytreeLinkExtensions.onPkceAuthorized
import com.getmoneytree.VaultOpenServicesOptions
import com.getmoneytree.linkkit.LinkKit
import com.getmoneytree.listener.Action
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.iid.FirebaseInstanceId
import java.util.UUID

/**
 * A reference app that introduces what the SDK can do.
 *
 * @author Moneyteee KK
 */
class MainActivity : AppCompatActivity() {

  private val statusTextView: TextView
    get() = findViewById(R.id.result_text)

  private val rootView: View
    get() = findViewById(R.id.scroll_view)

  private val baseAuthOptions: LinkAuthOptions.Builder
    get() = LinkAuthOptions
      .builder()
      .forceLogout(findViewById<CheckBox>(R.id.force_logout).isChecked)
      .auth(
        run {
          if (BuildConfig.authType == AuthType.PKCE) LinkAuthFlow.Pkce.create()
          else LinkAuthFlow.CodeGrant(UUID.randomUUID().toString().replace("_", "-"))
        }
      )

  @Suppress("ConstantConditionIf")
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    // Subscribe to this centralized result listener to get the LINK SDK results
    val context = this
    with(MoneytreeLink.instance) {
      onCodeGrantAuthorized(context) {
        showMessage(rootView, getString(R.string.code_grant_completion_message))
      }
      onPkceAuthorized(context) { token ->
        showMessage(rootView, getString(R.string.token_message, token.value))
      }
      onLoggedOut(context) {
        showMessage(rootView, getString(R.string.logout))
      }
      onEvent(context) { event ->
        when (event) {
          LinkEvent.ExternalOAuthAdded -> {
            showMessage(rootView, "External OAuth complete!")
          }
          LinkEvent.LinkWebSessionStarted -> {
            // You can do here whatever needs to happen before the in app browser opens
          }
          LinkEvent.LinkWebSessionFinished -> {
            // Whatever need to happens after the browser closes.
            // This has no specific meaning. It simply indicates that the browser has closed
            // For example VaultClosed should be used if you need to refresh data
          }
          LinkEvent.RequestCancelled -> {
            // This event is transmitted when the in app browser closes without a schema triggering
            // a flow. This means that the browser close button or system back button were used.
            showMessage(findViewById(android.R.id.content), "User closed the browser...")
          }
          LinkEvent.VaultOpened -> {
            // Specific event to indicate that the browser is opening the vault.
            Toast.makeText(this@MainActivity, "Vault opened!", Toast.LENGTH_SHORT).show()
          }
          LinkEvent.VaultClosed -> {
            Toast.makeText(this@MainActivity, "Vault closed!", Toast.LENGTH_SHORT).show()
          }
          LinkEvent.LoggedOut -> {
            // This is the event for a successful logout.
            // The extension function onLoggedOut is a shortcut of this.
          }
        }
      }
      onError(context) { e ->
        if (e.error == LinkError.UNAUTHORIZED) {
          showError(rootView, getString(R.string.error_no_token))
        } else {
          showError(rootView, e.error.message)
        }
      }
    }

    // //// Set up Issho Tsucho //////

    findViewById<Button>(R.id.link_kit_button)
      .setOnClickListener { startLinkKit() }

    findViewById<Button>(R.id.token_button).setOnClickListener {
      // Need to pass activity when you get a token.
      MoneytreeLink.instance.getToken(this)
    }

    findViewById<Button>(R.id.vault_button).setOnClickListener { view ->
      MoneytreeLink.instance.openVault(
        this@MainActivity,
        LinkRequestContext
          .Builder()
          .userEmail(findViewById<EditText>(R.id.auth_email_edit).text.toString())
          .build()
      )
    }

    findViewById<Button>(R.id.customer_support_button).setOnClickListener { view ->
      MoneytreeLink.instance.openVault(
        this@MainActivity,
        LinkRequestContext.Builder()
          .path(MoneytreeLink.VAULT_SUPPORT)
          .build()
      )
    }

    // connect service input and button
    val connectServiceInput = findViewById<TextView>(R.id.connect_service_key_input)
    val connectServiceButton = findViewById<Button>(R.id.connect_service_button)
    connectServiceButton.isEnabled = false

    // disable connect service button if connect service key input has no value and vise-versa
    connectServiceInput.addTextChangedListener(
      object : TextWatcher {
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
          connectServiceButton.isEnabled = connectServiceInput.text.isNotEmpty()
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun afterTextChanged(s: Editable) {}
      }
    )

    findViewById<Button>(R.id.connect_service_button).setOnClickListener { view ->
      val serviceKey = connectServiceInput.text.toString()

      MoneytreeLink.instance.openVault(
        this@MainActivity,
        LinkRequestContext
          .Builder()
          .path(MoneytreeLink.VAULT_SERVICE)
          .pathSuffix(serviceKey)
          .build()
      )
    }

    // connect service input and button
    val serviceSettingIdInput = findViewById<TextView>(R.id.service_settings_id_input)
    val serviceSettingButton = findViewById<Button>(R.id.service_settings_button)
    serviceSettingButton.isEnabled = false

    // disable connect service button if connect service key input has no value and vise-versa
    serviceSettingIdInput.addTextChangedListener(
      object : TextWatcher {
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
          serviceSettingButton.isEnabled = serviceSettingIdInput.text.isNotEmpty()
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun afterTextChanged(s: Editable) {}
      }
    )

    findViewById<Button>(R.id.service_settings_button).setOnClickListener { view ->
      val serviceKey = serviceSettingIdInput.text.toString()

      MoneytreeLink.instance.openVault(
        this@MainActivity,
        LinkRequestContext
          .Builder()
          .path(MoneytreeLink.VAULT_SERVICE_SETTINGS)
          .pathSuffix(serviceKey)
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

      MoneytreeLink.instance.openVault(
        this@MainActivity,
        LinkRequestContext
          .Builder()
          .vaultOpenServicesOptions(options)
          .build()
      )
    }

    findViewById<Button>(R.id.auth_button).setOnClickListener {
      MoneytreeLink.instance.authorize(
        this@MainActivity,
        baseAuthOptions
          .presentSignup(true)
          .buildAuthorize(
            findViewById<EditText>(R.id.auth_email_edit).text?.toString() ?: ""
          )
      )
    }

    findViewById<Button>(R.id.settings_button).setOnClickListener { view ->
      MoneytreeLink.instance.openSettings(
        this@MainActivity,
        null
      )
    }

    findViewById<Button>(R.id.onboard_button).setOnClickListener { view ->
      val email = findViewById<TextView>(R.id.onboard_input).editableText.toString()
      if (email.isEmpty()) {
        showError(view, "Email is required.")
        return@setOnClickListener
      }
      MoneytreeLink.instance.onboard(
        this@MainActivity,
        baseAuthOptions.buildOnboarding(email)
      )
    }

    val spinner = findViewById<Spinner>(R.id.magiclink_destination)
    // Populate data for spinner.
    // https://developer.android.com/guide/topics/ui/controls/spinner#Populate
    KeyValueAdapter(this, android.R.layout.simple_spinner_item)
      .apply {
        addAll(
          listOf(
            Pair(MoneytreeLink.ML_DESTINATION_SETTINGS, "Settings Page"),
            Pair(MoneytreeLink.ML_DESTINATION_LANGUAGE, "Change Moneytree Service Language"),
            Pair(MoneytreeLink.ML_DESTINATION_AUTHORIZED_APPS, "Authorized Apps"),
            Pair(MoneytreeLink.ML_DESTINATION_DELETE_ACCOUNT, "Delete Account"),
            Pair(MoneytreeLink.ML_DESTINATION_EMAIL_PREFERENCES, "Email Preferences"),
            Pair(MoneytreeLink.ML_DESTINATION_UPDATE_EMAIL, "Update Email"),
            Pair(MoneytreeLink.ML_DESTINATION_UPDATE_PASSWORD, "Update Password"),
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

      @Suppress("UNCHECKED_CAST")
      val selectedItem: Pair<String, String> = spinner.selectedItem as Pair<String, String>
      MoneytreeLink.instance.requestMagicLink(
        email,
        selectedItem.first,
        object : Action {
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

    findViewById<Button>(R.id.unregister_button)
      .setOnClickListener { unregisterToken() }

    statusTextView.text = getString(
      R.string.welcome,
      if (BuildConfig.authType == AuthType.PKCE) "PKCE" else "Code Grant",
      if (BuildConfig.isProduction) "Production" else "Staging"
    )

    findViewById<Button>(R.id.reset_button).setOnClickListener { view ->
      MoneytreeLink.instance.deleteCredentials()
      showMessage(view, getString(R.string.deleted_token))
    }

    findViewById<View>(R.id.logout_button).setOnClickListener {
      MoneytreeLink.instance.logout(this@MainActivity)
    }
  }

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    // Handle magic link action
    intent.data?.also { uri ->
      MoneytreeLink.instance.consumeMagicLink(this, uri)
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
   * Start LINK Kit
   */
  private fun startLinkKit() {
    showMessage(rootView, getString(R.string.it_launching))
    LinkKit.getInstance().launch(
      this,
      object : LinkKit.LinkKitListener {
        override fun onLaunched() {
          showMessage(rootView, getString(R.string.it_success))
        }

        override fun onError(exception: MoneytreeLinkException) {
          showError(rootView, exception.message)
        }
      }
    )
  }

  /**
   * Register the FCM token to the Moneytree notifications server
   */
  private fun registerToken() {
    FirebaseInstanceId
      .getInstance()
      .instanceId
      .addOnSuccessListener { instanceIdResult ->
        if (!MoneytreeLink.instance.isLoggedIn) {
          showError(rootView, getString(R.string.error_no_token))
        } else {
          MoneytreeLink
            .instance
            .registerFcmToken(
              instanceIdResult.token,
              object : Action {
                override fun onSuccess() {
                  showMessage(rootView, getString(R.string.register_token_ok))
                }

                override fun onError(exception: MoneytreeLinkException) {
                  showError(rootView, exception.message)
                }
              }
            )
        }
      }
  }

  /**
   * Remove the FCM token from the Moneytree notifications server
   */
  private fun unregisterToken() {
    FirebaseInstanceId
      .getInstance()
      .instanceId
      .addOnSuccessListener { instanceIdResult ->
        if (!MoneytreeLink.instance.isLoggedIn) {
          showError(rootView, getString(R.string.error_no_token))
        } else {
          MoneytreeLink
            .instance
            .unregisterFcmToken(
              instanceIdResult.token,
              object : Action {
                override fun onSuccess() {
                  showMessage(rootView, getString(R.string.unregister_token_ok))
                }

                override fun onError(exception: MoneytreeLinkException) {
                  showError(rootView, exception.message)
                }
              }
            )
        }
      }
  }
}
