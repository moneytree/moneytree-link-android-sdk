# Moneytree Link SDK (Android) Set Up Instructions

## Table Of Contents
1. [Requirements](#requirements)
2. [Update Configurations](#update-configurations)
3. [Update Code](#update-code)
    1. [How to use MoneytreeLink Library](#how-to-use-moneytreelink-library)
    2. [How to use Issho Tsucho Library](#how-to-use-issho-tsucho-library)
    3. [Set up notification](#set-up-notification)
4. [Breaking Changes since version 2](#breaking-changes-since-version-2)

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

1. Configure `AndroidManifest.xml` to receive a callback response from the server.

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
    // Application class
    @Override
    public void onCreate() {
       super.onCreate();

       final MoneytreeLinkConfiguration conf = new MoneytreeLinkConfiguration.Builder()
           .isProduction(false)                         // or true if production
           .clientId("1234567890abcde...")              // your ClientId
           .scopes(MoneyTreeLinkClient.GuestRead, ...)  // scope(s)
           .perferredGrantType(OAuthResponseType.Token)
           .build();
    }
    ```

    TIPS: Ideally, a boolean value for `isProduction` and a string value for `clientId` can be managed easily using [`resource`](https://developer.android.com/guide/topics/resources/more-resources.html#Bool) and [`Build Variants`](https://developer.android.com/studio/build/build-variants.html) in Android. You don't have to define them in code directly.

1. And then, initialize `MoneytreeLink` using the configuration file.
    ```java
    // Application class
    MoneytreeLink.init(this, configuration);
    ```

1. Update your activity class to enable to use `MoneytreeLink`.  All you have to do first is making a path to `authorize` for the guests. Every operations except `authorize` require the access token where the server provides when the guest agrees to authorize. Example is follows.

    ```java
     // Activity class
     findViewById(R.id.open_link_button).setOnClickListener(new View.OnClickListener() {
       @Override
       public void onClick(View v) {
         // Authorize
         MoneytreeLink.client().authorizeFrom(YourActivity.this, new Authorization.OnCompletionListener() {
           @Override
           public void onSuccess(@NonNull final String accessToken) {
             // It runs when the SDK gets token. 
             // You can implement as you want (open vault etc.)
           }
    
           @Override
           public void onError(@NonNull final MoneytreeLinkException exception) {
             // It runs when the SDK gets error during authorization.
             // See the JavaDoc regarding MoneytreeLinkException
           }
         });
       }
     });
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
      new IsshoTsucho.OnCompletionListener() { /* implement */ }
    );
    ```

    `IsshoTsucho.OnCompletionListener` is an optional instance, so you may set `null` if you don't need. It will describe sample usage in the later section.

1. `MoneytreeLink` instance is initialized when you initialize `IsshoTsucho`, so you can get it like

    ```java
    final MoneytreeLink linkClient = IsshoTsucho.linkClient();
    ```

    And you can see what `MoneytreeLink` (and `IsshoTsucho`) can by reading Javadoc.

### Set up notification

If you want to register device token, it should be after the guest gives permission to access their data from your app. In this section, it proposes when the best timing to register device token is.

- If you don't use `Issho Tsucho` library:

    After authorization flow would be the best timing. Example is follows.
    
    ```java
     // Authorize
     MoneytreeLink.client().authorizeFrom(YourActivity.this, new Authorization.OnCompletionListener() {
       @Override
       public void onSuccess(@NonNull final String accessToken) {
          final String deviceToken = ... // You should provide the device token
          // Registration method
          MoneytreeLink
            .client()
            .registerDeviceToken(
              deviceToken,
              new Api.OnCompletionListener() {
                @Override
                public void onSuccess() {
                  // It runs registering device token finishes successfully.
                }
      
                @Override
                public void onError(@NonNull MoneytreeLinkException exception) {
                  // It runs registering device token fails.
                }
              }
            );
       }
     
       @Override
       public void onError(@NonNull final MoneytreeLinkException exception) { /* snip*/ }
     });
    ```
    
    `MoneytreeLink.clint().unregisterDeviceToken(...)` is used to unregister device token.

- If you use `Issho Tsucho` library:

    `IsshoTsucho.OnCompletionListener` would be the best. See above regarding the sample code.

## Breaking Changes since version 2

MoneytreeLink SDK v3 brings some breaking changes regarding class name. The following is just an example, see also MainActivity.java in the Awesome app code and JavaDoc.

| Old Class Name  | New Class Name | Note |
| --------------- | -------------- | ---- |
|CompletionHandler| Authorization.OnCompletionListener| Methods for authorization|
| (same as above) | Action.OnCompletionListener| Used for openSettings, openInstitution|
|MoneytreeLink.ApiCompletionHandler|Api.OnCompletionListener| Renamed |
|IsshoTsucho.CompletionHandler|IsshoTsucho.OnCompletionListener| Renamed |