package com.example.myawesomeapp

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.myawesomeapp.databinding.ActivityMainBinding
import com.getmoneytree.LinkAuthFlow
import com.getmoneytree.LinkAuthOptions
import com.getmoneytree.LinkError
import com.getmoneytree.LinkEvent
import com.getmoneytree.LinkRequestContext
import com.getmoneytree.MoneytreeLink
import com.getmoneytree.MoneytreeLinkException
import com.getmoneytree.MoneytreeLinkExtensions.onAuthorized
import com.getmoneytree.MoneytreeLinkExtensions.onError
import com.getmoneytree.MoneytreeLinkExtensions.onEvent
import com.getmoneytree.MoneytreeLinkExtensions.onLoggedOut
import com.getmoneytree.VaultOpenServicesOptions
import com.getmoneytree.linkkit.LinkKit
import com.getmoneytree.listener.Action
import com.google.android.material.snackbar.Snackbar
import java.util.UUID

/**
 * A reference app that introduces what the SDK can do.
 *
 * @author Moneytree KK
 */
class MainActivity : AppCompatActivity() {

  private val rootView: View
    get() = binding.scrollView

  private val baseAuthOptions: LinkAuthOptions.Builder
    get() = LinkAuthOptions
      .builder()
      .forceLogout(binding.forceLogout.isChecked)
      .auth(
        if (BuildConfig.authType == AuthType.PKCE) LinkAuthFlow.Pkce.create()
        else LinkAuthFlow.CodeGrant(state = UUID.randomUUID().toString().replace("_", "-"))
      )

  private lateinit var binding: ActivityMainBinding

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityMainBinding.inflate(layoutInflater)
    setContentView(binding.root)

    // Subscribe to this centralized result listener to get the LINK SDK results
    val context = this
    with(MoneytreeLink.instance) {
      onAuthorized(context) { token ->
        if (token != null) {
          // If the token exists, it means you chose PKCE as the auth flow
          showMessage(rootView, getString(R.string.token_message, token.accessToken))
        } else {
          // Otherwise, it should be Code Grant
          showMessage(rootView, getString(R.string.code_grant_completion_message))
        }
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
            showMessage(
              findViewById(android.R.id.content),
              "Request was cancelled. User closed the browser."
            )
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
          LinkEvent.WebAppLogout -> {}
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

    binding.linkKitButton.setOnClickListener { startLinkKit() }

    binding.tokenButton.setOnClickListener {
      // Need to pass activity when you get a token.
      MoneytreeLink.instance.getToken(this)
    }

    binding.vaultButton.setOnClickListener {
      MoneytreeLink.instance.openVault(
        this@MainActivity,
        LinkRequestContext
          .Builder()
          .userEmail(binding.authEmailEdit.text.toString())
          .build()
      )
    }

    binding.customerSupportButton.setOnClickListener {
      MoneytreeLink.instance.openVault(
        this@MainActivity,
        LinkRequestContext.Builder()
          .path(MoneytreeLink.VAULT_SUPPORT)
          .build()
      )
    }

    val connectServiceInput = binding.connectServiceKeyInput
    val connectServiceButton = binding.connectServiceButton
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

    binding.connectServiceButton.setOnClickListener {
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

    val serviceSettingIdInput = binding.serviceSettingsIdInput
    val serviceSettingButton = binding.serviceSettingsButton
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

    binding.serviceSettingsButton.setOnClickListener {
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

    val openServicesTypeInput = binding.openServicesTypeInput
    val openServicesGroupInput = binding.openServicesGroupInput
    val openServicesSearchInput = binding.openServicesSearchInput

    binding.openServicesButton.setOnClickListener {
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

    binding.authButton.setOnClickListener {
      MoneytreeLink.instance.authorize(
        this@MainActivity,
        baseAuthOptions
          .presentSignup(true)
          .buildAuthorize(email = binding.authEmailEdit.text?.toString() ?: "")
      )
    }

    binding.settingsButton.setOnClickListener {
      MoneytreeLink.instance.openSettings(
        this@MainActivity,
        null
      )
    }

    binding.onboardButton.setOnClickListener { view ->
      val email = binding.onboardInput.editableText.toString()
      if (email.isEmpty()) {
        showError(view, "Email is required.")
        return@setOnClickListener
      }
      MoneytreeLink.instance.onboard(
        this@MainActivity,
        baseAuthOptions.buildOnboarding(email)
      )
    }

    val spinner = binding.loginlinkDestination
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

    binding.loginlinkButton.setOnClickListener { view ->
      val email = binding.loginlinkEmail.text.toString()
      if (email.isEmpty()) {
        showError(view, "Email is required.")
        return@setOnClickListener
      }

      @Suppress("UNCHECKED_CAST")
      val selectedItem: Pair<String, String> = spinner.selectedItem as Pair<String, String>
      MoneytreeLink.instance.requestLoginLink(
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

    binding.registerButton.setOnClickListener { registerToken() }
    binding.unregisterButton.setOnClickListener { unregisterToken() }

    binding.resetButton.setOnClickListener { view ->
      MoneytreeLink.instance.deleteCredentials()
      showMessage(view, getString(R.string.deleted_token))
    }

    binding.logoutButton.setOnClickListener {
      MoneytreeLink.instance.logout(this@MainActivity)
    }

    binding.resultText.text = getString(
      R.string.welcome,
      if (BuildConfig.authType == AuthType.PKCE) "PKCE" else "Code Grant",
      if (BuildConfig.isProduction) "Production" else "Staging"
    )

    if (savedInstanceState == null) {
      intent.data?.also { uri ->
        MoneytreeLink.instance.consumeLoginLink(this, uri)
      }
    }
  }

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    // Handle login link action
    intent.data?.also { uri ->
      MoneytreeLink.instance.consumeLoginLink(this, uri)
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
      .also { binding.resultText.text = message }
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
      .also { binding.resultText.text = message }
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
   * You have to determine how your app gets the device token from users' device.
   */
  private fun getDeviceToken(): String = "__device_token__"

  /**
   * Register a token to the Moneytree notifications server
   */
  private fun registerToken() {
    if (!MoneytreeLink.instance.isLoggedIn) {
      showError(rootView, getString(R.string.error_no_token))
    } else {
      MoneytreeLink
        .instance
        .registerRemoteToken(
          getDeviceToken(),
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

  /**
   * Remove a token from the Moneytree notifications server
   */
  private fun unregisterToken() {
    if (!MoneytreeLink.instance.isLoggedIn) {
      showError(rootView, getString(R.string.error_no_token))
    } else {
      MoneytreeLink
        .instance
        .unregisterRemoteToken(
          getDeviceToken(),
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
