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
     * The default settings is to work in the `PKCE` type flow, so,
     * if you want to try out the `Code Grant` type flow, you have to complete the following tasks.
     * - Uncomment `redirectUri` call in the configuration settings (just below!)
     * - At [MainActivity], you have to replace options in the [MoneytreeAuthOptions.Builder]
     *   where it's supplied when it starts authorize process.
     *   - You have to comment out [MoneytreeAuthOptions.Builder.authorizationHandler] and
     *   call [MoneytreeAuthOptions.Builder.codeGrantTypeOptions] instead.
     * - You have to look into Logcat if the app gets crashed when you tap a button.
     *   It will give you descriptive message..
     */
    @Suppress("ConstantConditionIf")
    val configuration = MoneytreeLinkConfiguration.Builder()
      .linkEnvironment(
        if (BuildConfig.isProduction) LinkEnvironment.Production
        else LinkEnvironment.Staging
      )
      // Place the Client ID you received here
      .clientId("[clientId]")
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

    // Initialize MoneytreeLink client.
    MoneytreeLink.init(this, configuration) { result ->
      Log.d(AwesomeApplication::class.simpleName!!, "Result: $result")
    }
    // Initialize Issho Tsucho client. (You can do either one.)
    LinkKit.init(this, configuration)
  }
}
