# Moneytree Link SDK (Android) Set Up Instructions

## Table Of Contents
1. [Requirements](#requirements)
2. [Update Configurations](#update-configurations)
3. [Update Code](#update-code)
    1. [How to use MoneytreeLink Library](#how-to-use-moneytreelink-library)
    2. [How to use Issho Tsucho Library](#how-to-use-issho-tsucho-library)
    3. [Set up notification](#set-up-notification)

## Requirements

- `minSdkVersion` of your app should be >= 19 (Android 4.4, `KitKat`)
- Add [`Chrome Custom Tabs`](https://developer.chrome.com/multidevice/android/customtabs) to your project

```groovy
compile "com.android.support:customtabs:<LATEST_VERSION>"
```

## Update Configurations

1. Get the latest [`MoneytreeLinkCore-<version>.aar`](https://github.com/moneytree/mt-link-android-sdk-example/releases) and [`MoneytreeLinkIsshoTsucho-<version>.aar`](https://github.com/moneytree/mt-link-android-sdk-example/releases).

1. Add libraries (the aar file above) to your project
    - See also [Integration steps](https://developer.android.com/studio/projects/android-library.html?#AddDependency).
    - Or this example app might be helpful.

1. In `app/build.gradle`, you have to add some dependencies that the SDK requires.
    ```groovy
    compile "com.google.code.gson:gson:2.8.1"
    compile "com.squareup.okhttp3:okhttp:3.9.0"
    compile "com.squareup.retrofit2:retrofit:2.3.0"
    compile "com.squareup.retrofit2:converter-gson:2.3.0"
    ```

1. Configure `AndroidManifest.xml` to receive an authorized response from the server.

    1. Add `SchemeHandlerActivity` as the following example. It will receive an authorized response when the guest gives permission to access to their data from your app.
    ```xml
    <activity android:name="com.getmoneytree.auth.SchemeHandlerActivity">
        <intent-filter>
            <action android:name="android.intent.action.VIEW" />

            <category android:name="android.intent.category.DEFAULT" />
            <category android:name="android.intent.category.BROWSABLE" />
            <!-- FIXME -->
            <data android:scheme="YOUR_SCHEME_NAME" />
        </intent-filter>
    </activity>
    ```

    1. Edit `YOUR_SCHEME_NAME` in the example above and replace with `mtlink`+ **first 5 chars of your ClientId**
       e.g. If your `ClientId` is `abcde1234567890moneytree`, the scheme would be `mtlinkabcde`.

    ```xml
    <data android:scheme="mtlinkabcde" />
    ```

    1. Make sure `INTERNET` permission is declared.
    ```xml
    <uses-permission android:name="android.permission.INTERNET" />
    ```

## Update Code

### How to use MoneytreeLink Library

**If you're going to use Issho Tsucho Library, you can skip this section**

1. Initialize `MoneytreeLinkConfiguration` at your `Application` class
    ```java
    @Override
    public void onCreate() {
       super.onCreate();

       final MoneytreeLinkConfiguration conf = new MoneytreeLinkConfiguration.Builder()
           .isProduction(false)                         // or true if production
           .clientId("1234567890abcde...")              // your ClientId
           .scopes(MoneyTreeLinkClient.GuestRead, ...)  // scope(s)
           .perferredGrantType(OAuthResponseType.Token)
           .build();
    ```

    TIPS: Ideally, a boolean value for `isProduction` and a string value for `clientId` can be managed easily using [`resource`](https://developer.android.com/guide/topics/resources/more-resources.html#Bool) and [`Build Variants`](https://developer.android.com/studio/build/build-variants.html) in Android. You don't have to define them in code directly.

1. And then, initialize `MoneytreeLink` using the configuration file.
    ```java
    // at your Application class
    MoneytreeLink.init(this, configuration);
    ```

1. Update your activity class to enable to use `MoneytreeLink`. Make sure to call `setRootView` in advance. If you want to call `authorize`, you have to call `setOAuthHandler` as well.

    ```java
    // at your activity class
    final MoneytreLink client = MoneytreeLink.client();
    client.setRootView(this);
    client.setAuthzTokenHandler( /* set handler instance */ ); // see example code
    client.authorizeFrom(this);
    ```

    `OAuthHandler` will be used when you want to handle callback in a result of authorization request.

### How to use Issho Tsucho Library

1. Initialize `MoneytreeLinkConfiguration` at your `Application` class. See the above section to know how to initialize.

1. And then, initialize `IsshoTsucho` using the configuration file.
    ```java
    // at your Application class
    IsshoTsucho.init(this, conf);
    ```

1. Update your activity class to start `IsshoTsucho` whenever you want.

    ```java
    IsshoTsucho.client().startIsshoTsucho(
      new IsshoTsucho.CompletionHandler() {/* implement */}
    );
    ```

    `IsshoTsucho.CompletionHandler` is an optional instance, so you may set `null` if you don't need. It will describe sample usage in the later section.

1. `MoneytreeLink` instance is initialized when you initialize `IsshoTsucho`, so you can get it like

    ```java
    final MoneytreeLink linkClient = IsshoTsucho.linkClient();
    ```

    And you can see what `MoneytreeLink` (and `IsshoTsucho`) can by reading Javadoc.

### Set up notification

If you want to register device token, it should be after the guest gives permission to access their data from your app. In this section, it proposes when the best timing to register device token is.

- If you don't use `Issho Tsucho` library:

    `MoneytreeLink#setAuthzTokenHandler` would be the best timing. See [the above section](#how-to-use-moneytreelink-library).

- If you use `Issho Tsucho` library:

    `IsshoTsucho.CompletionHandler` would be the best. See an example code in this project.
