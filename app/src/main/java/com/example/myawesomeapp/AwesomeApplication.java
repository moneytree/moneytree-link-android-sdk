package com.example.myawesomeapp;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.util.Log;

import com.getmoneytree.MoneytreeLink;
import com.getmoneytree.MoneytreeLinkConfiguration;
import com.getmoneytree.MoneytreeLinkScope;
import com.getmoneytree.it.IsshoTsucho;

/**
 * @author Moneytree KK
 */
public class AwesomeApplication extends Application {

  static final String TAG = "AwesomeApplication";

  static final MoneytreeLinkConfiguration configuration = new MoneytreeLinkConfiguration.Builder()
      // true: production, false: staging
      .isProduction(false)
      // It's for the example app. DON'T USE FOR YOUR APP!
      .clientId("af84f08f40970caf17f2e53b31771ceb50d0f32f7d44b826753982e809395290")
      // You can add scopes using String as well.
      //.scopes("customized_scope", "new_scope")
      .scopes(
          MoneytreeLinkScope.GuestRead,
          MoneytreeLinkScope.AccountsRead,
          MoneytreeLinkScope.TransactionsRead
      )
      .build();

  @Override
  public void onCreate() {
    super.onCreate();

    // Initialize MoneytreeLink client.
    MoneytreeLink.init(this, configuration);
    // Initialize Issho Tsucho client. (You can do either one.)
    IsshoTsucho.init(this, configuration);

    // Just for logging. Don't have to implement :)
    registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
      @Override
      public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        Log.i(
            TAG,
            String.format(
                "onActivityCreated %s@%s",
                activity.getClass().getSimpleName(),
                Integer.toHexString(activity.hashCode())
            )
        );
      }

      @Override
      public void onActivityStarted(Activity activity) {
        Log.i(
            TAG,
            String.format(
                "onActivityStarted %s@%s",
                activity.getClass().getSimpleName(),
                Integer.toHexString(activity.hashCode())
            )
        );
      }

      @Override
      public void onActivityResumed(Activity activity) {
        Log.i(
            TAG,
            String.format(
                "onActivityResumed %s@%s",
                activity.getClass().getSimpleName(),
                Integer.toHexString(activity.hashCode())
            )
        );
      }

      @Override
      public void onActivityPaused(Activity activity) {
        Log.i(
            TAG,
            String.format(
                "onActivityPaused %s@%s",
                activity.getClass().getSimpleName(),
                Integer.toHexString(activity.hashCode())
            )
        );
      }

      @Override
      public void onActivityStopped(Activity activity) {
        Log.i(
            TAG,
            String.format(
                "onActivityStopped %s@%s",
                activity.getClass().getSimpleName(),
                Integer.toHexString(activity.hashCode())
            )
        );
      }

      @Override
      public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        Log.i(
            TAG,
            String.format(
                "onActivitySaveInstanceState %s@%s",
                activity.getClass().getSimpleName(),
                Integer.toHexString(activity.hashCode())
            )
        );
      }

      @Override
      public void onActivityDestroyed(Activity activity) {
        Log.i(
            TAG,
            String.format(
                "onActivityDestroyed %s@%s",
                activity.getClass().getSimpleName(),
                Integer.toHexString(activity.hashCode())
            )
        );
      }
    });
  }
}
