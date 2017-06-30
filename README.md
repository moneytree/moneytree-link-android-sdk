# Moneytree Link SDK Example App

## Requirements

- `minSdkVersion` of your app should be >= 19 (Android 4.4, `KitKat`)

## Recommendations

- Add [`Chrome Custom Tabs`](https://developer.chrome.com/multidevice/android/customtabs) to your project. It provides the better UX, no side effects.

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

    1. Set `launchMode` to `singleTask` to an activity that you want to receive the token.
    ```xml
    <activity
         android:name="com.example.myawesomeapp.OpenMoneytreeActivity"
         android:label="@string/app_name"
         android:launchMode="singleTask" <--- THIS
         (snip)
    ```

    2. Add `intent-filter` to the same activity. Note that the vaule of `android:scheme` should be `mtlink` + **first 5 chars of your ClientId**. If your `AppId` is `abcde1234567890moneytree`, it should be `mtlinkabcde` like the following example.
    ```xml
    <intent-filter>
        <action android:name="android.intent.action.VIEW" />
        <category android:name="android.intent.category.DEFAULT" />
        <category android:name="android.intent.category.BROWSABLE" />
        <data android:scheme="mtlinkabcde" /> // Depends on your ClientId
    </intent-filter>
    ```

    3. (*Not for all users*) If you don't use [`Chrome Custom Tabs`](https://developer.chrome.com/multidevice/android/customtabs) in your app, `INTERNET` permission is required.
    ```xml
    <uses-permission android:name="android.permission.INTERNET" />
    ```

5. Initialize `MoneytreeLinkClient` from the `Application` class
   ```java
   @Override
   public void onCreate() {
       super.onCreate();

       final MoneytreeLinkConfiguration conf = new MoneytreeLinkConfiguration.Builder()
           .isProduction(false) // or true if production
           .clientId(R.string.moneytree_link_client_id) // your MoneytreeLinkClientId
           .scopes(MoneyTreeLinkClient.GuestRead, ...) // scope(s)
           .build();
       MoneyTreeLinkClient.init(this, conf);
   }
   ```

6. Edit your activity to enable to call `MoneytreeLink` and get a token from `MoneytreeLink`

    1. Call *authorize* to start the authorization flow
    ```java
    final Button button = (Button) findViewById(R.id.login_button);
    button.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            MoneytreeLinkClient.authorize(MainActivity.this);
        }
    });
    ```
    The method *authorize* will open the WebView to get a token.

    2. Override *onNewIntent* to get a token
    ```java
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        // Check whether an intent has a token
        if (MoneytreeLinkClient.hasToken(intent)) {
            final String token = MoneytreeLinkClient.findToken(intent);
            // You can save the token here.
            saveToken(token);
        }

        // Need to call on singleTask activity to work `getIntent()` in other methods
        setIntent(intent);
    }
    ```
    Make sure to check given `intent` using *hasToken* first, and then get using *findToken*.
