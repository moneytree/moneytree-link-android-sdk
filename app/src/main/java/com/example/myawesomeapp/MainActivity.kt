package com.example.myawesomeapp

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.TypedValue
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.myawesomeapp.databinding.ActivityMainBinding
import com.getmoneytree.LinkError
import com.getmoneytree.LinkEvent
import com.getmoneytree.MoneytreeLink
import com.getmoneytree.MoneytreeLinkExtensions.onAuthorized
import com.getmoneytree.MoneytreeLinkExtensions.onError
import com.getmoneytree.MoneytreeLinkExtensions.onEvent
import com.getmoneytree.MoneytreeLinkExtensions.onLoggedOut
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MainActivity : AppCompatActivity() {
  private lateinit var binding: ActivityMainBinding


  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)


    binding = ActivityMainBinding.inflate(layoutInflater)
    setContentView(binding.root)

    binding.debugText.movementMethod = ScrollingMovementMethod()

    sdkCallbacksSetup()

    val moneytreeIdFragment = MoneytreeIdFragment()
    val vaultFragment = VaultFragment()
    val linkKitFragment = LinkKitFragment()
    val otherFragment = OtherFragment()

    binding.bottomNav.setOnItemSelectedListener { menuItem ->
      when (menuItem.itemId) {
        R.id.auth -> {
          setTabFragment(moneytreeIdFragment, getString(R.string.moneytree_id_title))
          true
        }
        R.id.vault -> {
          setTabFragment(vaultFragment, getString(R.string.services_title))
          true
        }
        R.id.link_kit -> {
          setTabFragment(linkKitFragment, getString(R.string.link_kit_title))
          true
        }
        R.id.others -> {
          setTabFragment(otherFragment, getString(R.string.other_functions_title))
          true
        }
        else -> {
          false
        }
      }
    }

    binding.bottomNav.selectedItemId = savedInstanceState?.getInt("currentPage") ?: R.id.auth

    setDebug(
      getString(
        R.string.general_output_message_default,
        if (BuildConfig.isProduction) {
          getString(R.string.general_output_production)
        } else getString(R.string.general_output_staging)
      )
    )

    if (savedInstanceState == null) consumeMagicLink(intent)
  }

  @SuppressLint("SetTextI18n")
  fun setDebug(text: String, isError: Boolean = false) {
    val typedValue = TypedValue()
    theme.resolveAttribute(R.attr.colorOnSurfaceVariant, typedValue, true)
    val color = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      Color.valueOf(typedValue.data).toArgb()
    } else {
      getColor(R.color.md_theme_light_onSurfaceVariant)
    }
    binding.debugText.setTextColor(
      if (isError) Color.RED else color
    )

    val dateTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
      .format(Calendar.getInstance().timeInMillis)
    binding.debugText.text = "[$dateTime] $text"
  }

  private fun setTabFragment(
    fragment: Fragment,
    title: String,
  ) {
    binding.toolbar.title = title
    supportFragmentManager.beginTransaction().apply {
      replace(R.id.page_container, fragment)
      commit()
    }
  }

  //region ------------------------------ SDK flows ------------------------------
  private fun sdkCallbacksSetup() {
    val context = this
    with(MoneytreeLink.instance) {
      onAuthorized(context) { token ->
        setDebug(getString(R.string.general_output_token, token?.accessToken))
      }
      onLoggedOut(context) {
        setDebug(getString(R.string.moneytree_id_output_logged_out))
      }
      onEvent(context) { event ->
        when (event) {
          LinkEvent.ExternalOAuthAdded -> {
            setDebug(getString(R.string.debug_message_external_auth_complete))
          }
          LinkEvent.LinkWebSessionStarted -> {
            // Event indicating that the SDK is starting a web session (opening an in-app-browser).
            // Use this event to handle web session lifecycle as needed.
          }
          LinkEvent.LinkWebSessionFinished -> {
            // Event indicating that the web session has closed.
            // This has no specific meaning. It simply indicates that the browser has closed.
            // For example, VaultClosed should be used if you need to refresh data.
          }
          LinkEvent.RequestCancelled -> {
            // This event is transmitted when the in app browser closes without a scheme triggering
            // a flow. This can happen when the browser close button or system back button are used.
            setDebug(getString(R.string.error_user_cancelled))
          }
          LinkEvent.VaultOpened -> {
            // Specific event to indicate that the browser is opening the vault.
            setDebug(getString(R.string.services_output_vault_opened))
          }
          LinkEvent.VaultClosed -> {
            // The Vault has closed.
            // Potentially new data is available. Take any actions you need on that data.
            setDebug(getString(R.string.services_output_vault_closed))
          }
          LinkEvent.LoggedOut -> {
            // This is the event for a successful logout.
            // The extension function onLoggedOut is a shortcut of this.
          }
          LinkEvent.WebAppLogout -> {
            // Specific logout event when you are logging out from he settings page of LINK Kit.
          }
        }
      }
      onError(context) { e ->
        if (e.error == LinkError.UNAUTHORIZED) {
          setDebug(getString(R.string.error_unauthorized))
        } else {
          setDebug(e.error.message)
        }
      }
    }
  }
  //endregion

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    consumeMagicLink(intent)
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    outState.putInt("currentPage", binding.bottomNav.selectedItemId)
  }

  private fun consumeMagicLink(intent: Intent) {
    // Handle login link action
    intent.data?.also { uri ->
      MoneytreeLink.instance.consumeLoginLink(this, uri)
    }
  }
}
