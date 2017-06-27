package com.example.myawesomeapp;

import android.app.Application;

import jp.moneytree.mtlinksdk.MTLinkClient;
import jp.moneytree.mtlinksdk.MTLinkConfiguration;
import jp.moneytree.mtlinksdk.MTLinkScope;

/**
 * @author Moneytree KK
 */
public class AwesomeApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize MTLinkClient
        final MTLinkConfiguration configuration = new MTLinkConfiguration.Builder()
                .isProduction(false) // true: production, false: staging
                .appId("__SET_YOUR_MoneytreeLinkAppID__")  // set your MoneytreeLinkAppId
                .scopes(MTLinkScope.GuestRead) // set scopes
                .build();
        // Set application instance and configuration that created above.
        MTLinkClient.init(this, configuration);
    }
}