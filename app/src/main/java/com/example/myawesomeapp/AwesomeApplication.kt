package com.example.myawesomeapp

import android.app.Application
import android.util.Log
import com.getmoneytree.AuthenticationMethod
import com.getmoneytree.LinkEnvironment
import com.getmoneytree.MoneytreeLink
import com.getmoneytree.MoneytreeLinkConfiguration
import com.getmoneytree.MoneytreeLinkScope
import com.getmoneytree.linkkit.LinkKit

/**
 * @author Moneytree KK
 */
class AwesomeApplication : Application() {

  override fun onCreate() {
    super.onCreate()

    val configuration = MoneytreeLinkConfiguration.Builder()
      .linkEnvironment(
        if (BuildConfig.isProduction) LinkEnvironment.Production
        else LinkEnvironment.Staging
      )
      .clientId(BuildConfig.clientId)
      // TODO: Place the Client ID you received here
      // You can add scopes using String as well.
      // .scopes("customized_scope", "new_scope")
      .scopes(
        MoneytreeLinkScope.GuestRead,
        MoneytreeLinkScope.AccountsRead,
        MoneytreeLinkScope.TransactionsRead
      )
      .authenticationMethod(AuthenticationMethod.Credentials)
      .build()

    if (configuration.clientId == "[clientId]") {
      Log.w(
        TAG,
        """\n
          ***************** Detected placeholder strings. Need your edit! *************************
          You should set your clientId to the SDK configuration. See also AwesomeApplication.kt.
          And just in case, please check if you updated clientIdShort variable in build.gradle.kts.
          Read README.md for the setup step.
          *****************************************************************************************
        """.trimIndent()
      )
    }

    // Initialize MoneytreeLink client.
    MoneytreeLink.init(this, configuration) { result ->
      Log.d(TAG, "Result: $result")
    }
    // Initialize LINK Kit client. (You can do either one.)
    LinkKit.init(this, configuration)
  }

  companion object {
    private const val TAG = "AwesomeApplication"
  }
}
