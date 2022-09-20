package com.example.myawesomeapp

import android.app.Application
import android.util.Log
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

    /**
     * Kindly guideline for all who are going to try out AwesomeApp;
     *
     * The default settings is to work in the `PKCE` flow, so,
     * if you want to try out the `Code Grant` flow, you have to complete the following tasks.
     *
     * - Edit `awesomeAuthType` in `gradle.properties`.
     * - Edit `redirectUri` URL below to utilise your backend service.
     * - Rebuild the project and see if the header text says the current configuration is Code Grant
     *
     * Logcat will give descriptive message when the app gets crashed.
     */
    @Suppress("ConstantConditionIf")
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
      .apply {
        if (BuildConfig.authType == AuthType.CODE_GRANT) {
          // Redirect URL that used in the `Code Grant` type flow of the Awesome App
          // DON'T USE THIS URL YOUR PRODUCTION APP!
          // You can update this if you want to test on your account.
          /* ktlint-disable max-line-length */
          redirectUri(
            "https://wf3kkdzcog.execute-api.ap-northeast-1.amazonaws.com/staging/external_client_server.json"
          )
          /* ktlint-enable max-line-length */
        }
      }
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
