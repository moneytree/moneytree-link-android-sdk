# Moneytree Link SDK Example App

## Requirements

- `minSdkVersion` of your app should be >= 19 (Android 4.4, `KitKat`)

## Recommendations

- Add [`Chrome Custom Tabs`](https://developer.chrome.com/multidevice/android/customtabs) to your project. It provides the better UX, no side effects.
```groovy
compile "com.android.support:customtabs:<LATEST_VERSION>"
```

## Setup

1. Download the latest [`MoneytreeLinkCore-<version>.aar`](https://github.com/moneytree/mt-link-android-sdk-example/releases).

2. Add the library (the aar file above) to your project
    - [Integration steps](https://developer.android.com/studio/projects/android-library.html?#AddDependency).
    - Or this example app might be helpful.

3. Add your `ClientId` string that is provided by Moneytree
```xml
<string name="moneytree_link_client_id">YOUR_MONEYTREE_LINK_CLIENT_ID</string>
```
    - We recommend to use `strings.xml` to differentiate `moneytree_link_client_id` between staging and production. i.e. `debug/strings.xml` and `main/strings.xml`

4. Configure the `manifest` file to receive the token from web view.

    1. Add `CustomTabActivity` as the following example. It receives a callback which has a token when your `authorize` request for the `MoneytreeLink` class finishes successfully.
    ```xml
    <activity android:name="com.getmoneytree.token.CustomTabActivity">
        <intent-filter>
            <action android:name="android.intent.action.VIEW" />

            <category android:name="android.intent.category.DEFAULT" />
            <category android:name="android.intent.category.BROWSABLE" />
            <!-- FIXME -->
            <data android:scheme="YOUR_SCHEME_NAME" />
        </intent-filter>
    </activity>
    ```

    2. Replace `YOUR_SCHEME_NAME` in `android:scheme`. It should be `mtlink` + **first 5 chars of your ClientId**. If your `ClientId` is `abcde1234567890moneytree`, your scheme will be `mtlinkabcde` like as follows.
    ```xml
    <data android:scheme="mtlinkabcde" />
    ```

    3. (*Not for all users*) If your app can't use [`Chrome Custom Tabs`](https://developer.chrome.com/multidevice/android/customtabs), don't forget to define `INTERNET` permission.
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
           .scopes(MoneyTreeLinkClient.GuestRead, ...) // scope(s)
           .build();
       MoneyTreeLink.init(this, conf);
   }
   ```

6. Edit your activity to enable to call `MoneytreeLink` and get a token from `MoneytreeLink`

    1. Call *authorize* to start the authorization flow
    ```java
    button.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {

          MoneytreeLink.client().authorize(new TokenHandler() {
              @Override
              public void onSuccess(String token) {
                  // Your method; Save a token to secure place.
                  saveToken(token);
              }

              @Override
              public void onFailure(Throwable throwable) {
                  // Your method; Let users know there was an error.
                  displayErrorMessage(throwable);
              }
          });
        }
    });
    ```
    The method *authorize* will open the WebView to get a token.

    2. A method of `TokenHandler` instance (*onSuccess* or *onFailure*) will be fired based on the result of requests.
