# Moneytree Link SDK Example App

## Requirements

- `minSdkVersion` of your app should be >= 19 (Android 4.4, `KitKat`)

## Recommendations

- Add [`Chrome Custom Tabs`](https://developer.chrome.com/multidevice/android/customtabs) to your project. It provides the better UX, no side effects.

## Setup

1. Download `MoneytreeLinkCore-<version>.aar`.

2. Add the library (the aar file above) to your project
    - You don't know how? Read [description](https://developer.android.com/studio/projects/android-library.html?#AddDependency).
    - Or this example app might be helpful.

3. Add your `ClientId` string that is provided by Moneytree
```xml
<string name="link_client_id">YOUR_MONEYTREE_LINK_CLIENT_ID</string>
```

    - You might have 2 `ClientId`s - for staging and production. If so, we recommend to use `strings.xml` to control them seamlessly.
    - No naming convention for `string name`. You can name as you want.

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

    3. (*Not for all users*) If you don't use [`Chrome Custom Tabs`](https://developer.chrome.com/multidevice/android/customtabs) in your app, you have to add `INTERNET` permission to the `manifest` file properly.
    ```xml
    <uses-permission android:name="android.permission.INTERNET" />
    ```

5. Initialize `MoneyTreeLinkClient` at your `Application` class like this
   ```java
   @Override
   public void onCreate() {
       super.onCreate();

       final MoneytreeLinkConfiguration conf = new MoneytreeLinkConfiguration.Builder()
           .isProduction(false) // or true if production
           .clientId("__YOUR_MONEYTREE_LINK_CLIENT_ID__")
           .scopes(MoneyTreeLinkClient.GuestRead, ...)
           .build();
       MoneyTreeLinkClient.init(this, conf);
   }
   ```

6. Edit your activity to enable to call `MoneytreeLink` and get a token from `MoneytreeLink`

    1. You can put a button to open `MoneytreeLink` like this.
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
    2. You have to edit *onNewIntent* method that runs when the WebView returns a token.
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
