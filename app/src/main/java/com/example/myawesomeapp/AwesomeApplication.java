package com.example.myawesomeapp;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.util.Log;

/**
 * @author Moneytree KK
 */
public class AwesomeApplication extends Application {

    static final String TAG = "AwesomeApplication";

    @Override
    public void onCreate() {
        super.onCreate();

        // Strongly recommend to initialize MoneytreeLink client here if you don't want to use
        // both 'Implicit' and 'Code' at the same time or you don't want to set different scopes
        // dynamically.
        //
        // This AwesomeApp is a show case app to demonstrate what the SDK provides,
        // so it gives capability to change configuration after the app initializes once.
        // That's why there are no initialization phase here.
        this.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                Log.i(TAG, String.format("onActivityCreated %s", activity));
            }

            @Override
            public void onActivityStarted(Activity activity) {
                Log.i(TAG, String.format("onActivityStarted %s", activity));
            }

            @Override
            public void onActivityResumed(Activity activity) {
                Log.i(TAG, String.format("onActivityResumed %s", activity));
            }

            @Override
            public void onActivityPaused(Activity activity) {
                Log.i(TAG, String.format("onActivityPaused %s", activity));
            }

            @Override
            public void onActivityStopped(Activity activity) {
                Log.i(TAG, String.format("onActivityStopped %s", activity));
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
                Log.i(TAG, String.format("onActivitySaveInstanceState %s", activity));
            }

            @Override
            public void onActivityDestroyed(Activity activity) {
                Log.i(TAG, String.format("onActivityDestroyed %s", activity));
            }
        });
    }
}
