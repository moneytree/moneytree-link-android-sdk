package com.example.myawesomeapp;

import android.app.Application;

import com.getmoneytree.MoneytreeLink;
import com.getmoneytree.MoneytreeLinkConfiguration;
import com.getmoneytree.MoneytreeLinkScope;
import com.getmoneytree.it.IsshoTsucho;

/**
 * @author Moneytree KK
 */
public class AwesomeApplication extends Application {

  private static final MoneytreeLinkConfiguration configuration = new MoneytreeLinkConfiguration.Builder()
    // true: production, false: staging
    .isProduction(false)
    // Awesome App ID
    // DON'T USE IT YOUR PRODUCTION APP!
    // You can update this if you want to test on your account.
    .clientId("af84f08f40970caf17f2e53b31771ceb50d0f32f7d44b826753982e809395290")
    // You can add scopes using String as well.
    //.scopes("customized_scope", "new_scope")
    .scopes(
      MoneytreeLinkScope.GuestRead,
      MoneytreeLinkScope.AccountsRead,
      MoneytreeLinkScope.TransactionsRead
    )
    // Redirect URL that used in the Auth code grant type flow of the Awesome App
    // DON'T USE IT YOUR PRODUCTION APP!
    // You can update this if you want to test on your account.
    //.redirectUri("https://wf3kkdzcog.execute-api.ap-northeast-1.amazonaws.com/staging/external_client_server.json")
    // You have to add Intelligence module if you want.
    //.modules(new MoneytreeIntelligenceFactory())
    .build();

  @Override
  public void onCreate() {
    super.onCreate();

    // Initialize MoneytreeLink client.
    MoneytreeLink.init(this, configuration);
    // Initialize Issho Tsucho client. (You can do either one.)
    IsshoTsucho.init(this, configuration);
  }
}
