# Moneytree Link SDK Example App

## Requirements

- `minSdkVersion` of your app should be >= 19 (Android 4.4, `KitKat`)

## Recommendations

- Add [`Chrome Custom Tabs`](https://developer.chrome.com/multidevice/android/customtabs) to your project. It provides the better UX, no side effects.
```groovy
compile "com.android.support:customtabs:<LATEST_VERSION>"
```

## Setup

### setup and configure SDK

1. Download the latest [`MoneytreeLinkCore-<version>.aar`](https://github.com/moneytree/mt-link-android-sdk-example/releases).

2. Add the library (the aar file above) to your project
    - See also [Integration steps](https://developer.android.com/studio/projects/android-library.html?#AddDependency).
    - Or this example app might be helpful.

3. Add your `ClientId` string that is provided by Moneytree. The `ClientId` is different for each environment. *Staging*, and *Production*.
```xml
<string name="moneytree_link_client_id">YOUR_MONEYTREE_LINK_CLIENT_ID</string>
```

4. Configure the `manifest` file to receive an auth token from WebView.

    1. Add `CustomTabActivity` as the following example. It will receive a callback which has an auth token when your `authorize` request for the `MoneytreeLink` class finishes successfully.
    ```xml
    <activity android:name="com.getmoneytree.auth.CustomTabActivity">
        <intent-filter>
            <action android:name="android.intent.action.VIEW" />

            <category android:name="android.intent.category.DEFAULT" />
            <category android:name="android.intent.category.BROWSABLE" />
            <!-- FIXME -->
            <data android:scheme="YOUR_SCHEME_NAME" />
        </intent-filter>
    </activity>
    ```

    2. Replace `YOUR_SCHEME_NAME` with `mtlink`+ **first 5 chars of your ClientId**
       e.g. If the `ClientId` is `abcde1234567890moneytree`, the scheme is `mtlinkabcde`:

    ```xml
    <data android:scheme="mtlinkabcde" />
    ```

    3. (*Not for all users*) If your app can't use [`Chrome Custom Tabs`](https://developer.chrome.com/multidevice/android/customtabs), `INTERNET` permission is required.
    ```xml
    <uses-permission android:name="android.permission.INTERNET" />
    ```

5. Initialize `MoneytreeLink` from the `Application` class
   ```java
   @Override
   public void onCreate() {
       super.onCreate();

       final MoneytreeLinkConfiguration conf = new MoneytreeLinkConfiguration.Builder()
           .isProduction(false) // or true if production
           .clientId(R.string.moneytree_link_client_id) // your ClientId
           .scopes(MoneyTreeLinkClient.GuestRead, ...)  // scope(s)
           .perferredGrantType(OAuthGrantType.Implicit) // or .Scope
           .build();
       MoneyTreeLink.init(this, conf);
   }
   ```

6. Update your activity class to enable to use `MoneytreeLink` and get an access token or code.

    1. Register your **OAuthHandler** instance. The following case is about to `Implicit`. Use **&lt;OAuthAccessCode&gt;** when you expect *code*.
    ```java
    MoneytreeLink.client().setOAuthHandler(
      new OAuthHandler<OAuthAccessToken>() {
        @Override
        public void onSuccess(OAuthAccessToken payload) {
          // Your method
          saveToken(payload.getAccessToken());
        }
                                           
        @Override
        public void onFailure(Throwable throwable) {
          // Your method
          displayErrorMessage(throwable);
        }
    });
    ```
    Note that this *oAuthHandler* is a singleton, so you may override when you call twice. And make sure to call *setOAuthHandler* on the activity where it calles *MoneytreeClient*.
    
    2. Call *authorize* to start an authorization flow
    ```java
    MoneytreeLink.client().authorize();
    ```
    It will open the *chromeTabs* to get an auth token.

    3. An **OAuthHandler** instance that you set in advance will be fired when the guest completes authorization process.
