package com.example.myawesomeapp;

import android.app.Application;

import com.getmoneytree.MoneytreeLinkClient;
import com.getmoneytree.MoneytreeLinkConfiguration;
import com.getmoneytree.MoneytreeLinkScope;

/**
 * @author Moneytree KK
 */
public class AwesomeApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize MTLinkClient
        final MoneytreeLinkConfiguration configuration = new MoneytreeLinkConfiguration.Builder()
                .isProduction(false) // true: production, false: staging
                .appId("__SET_YOUR_MoneytreeLinkAppID__")  // set your MoneytreeLinkAppId
                .scopes(MoneytreeLinkScope.GuestRead) // set scopes
                .build();
        // Set application instance and configuration that created above.
        MoneytreeLinkClient.init(this, configuration);
    }
}