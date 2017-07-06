package com.example.myawesomeapp;

import android.app.Application;

import com.getmoneytree.MoneytreeLink;
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
                .clientId(getString(R.string.link_client_id))  // set your ClientId
                .scopes(MoneytreeLinkScope.GuestRead) // set scopes
                .build();
        // Set application instance and configuration that created above.
        MoneytreeLink.init(this, configuration);
    }
}
