package com.example.myawesomeapp;

import android.app.Application;

/**
 * @author Moneytree KK
 */
public class AwesomeApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Strongly recommend to initialize MoneytreeLink client here if you don't want to use both 'Implicit' and 'Code' at the same time or you don't want to set different scopes dynamically. This AwesomeApp is a show case app to demonstrate what the SDK provides, so it gives capability to change configuration after the app initializes once. That's why there are no initialization phase here.
    }
}
