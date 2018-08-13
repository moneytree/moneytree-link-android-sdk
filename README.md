# Moneytree Link SDK (Android) Set Up Instructions

- [Moneytree Link SDK (Android) Set Up Instructions](#moneytree-link-sdk-android-set-up-instructions)
    - [Requirements](#requirements)
        - [Note for KitKat device](#note-for-kitkat-device)
    - [Set up dependencies and manifest](#set-up-dependencies-and-manifest)
    - [Implement](#implement)
        - [[1a] MoneytreeLink Library (PKCE)](#1a-moneytreelink-library-pkce)
        - [[1b] MoneytreeLink Library (Authorization code grant type)](#1b-moneytreelink-library-authorization-code-grant-type)
        - [[2] Issho Tsucho Library](#2-issho-tsucho-library)
    - [Register device token for push notification](#register-device-token-for-push-notification)
    - [Breaking Changes](#breaking-changes)
        - [v3](#v3)
        - [v3.0.8](#v308)

## Requirements

- `minSdkVersion` of your app should be >= 19 (Android 4.4, `KitKat`)
- Add [`Chrome Custom Tabs`](https://developer.chrome.com/multidevice/android/customtabs) to your project

```groovy
compile "com.android.support:customtabs:<LATEST_VERSION>"
```

### Note for KitKat device

We have to protect guests as much as possible even though they use old devices so we're going to update our server-side configuration to only allow `TLSv1.2` (ask our representatives about schedule). Then Android 4.4, `KitKat`, will be required to make sure it has the latest security patches from `Google Play Services`. So, you have to run this code snippet in your app if their device is KitKat.

```java
ProviderInstaller.installIfNeeded()
```

See also [an official document](https://developer.android.com/training/articles/security-gms-provider?hl=en) about this.

## Set up dependencies and manifest

1. Get the latest [`MoneytreeLinkCore-<version>.aar`](https://github.com/moneytree/mt-link-android-sdk-example/releases) and [`MoneytreeLinkIsshoTsucho-<version>.aar`](https://github.com/moneytree/mt-link-android-sdk-example/releases).

2. Add libraries (the aar file above) to your project
    - See also [Integration steps](https://developer.android.com/studio/projects/android-library.html?#AddDependency).
    - Or this example app might be helpful.

3. In `app/build.gradle`, you have to add some dependencies that the SDK requires.
    ```groovy
    compile "org.jetbrains.kotlin:kotlin-stdlib:1.2.51"
    compile "com.google.code.gson:gson:2.8.5"
    compile "com.squareup.okhttp3:okhttp:3.11.0"
    compile "com.squareup.retrofit2:retrofit:2.4.0"
    compile "com.squareup.retrofit2:converter-gson:2.4.0"
    ```

    Note: You can exclude `kotlin-stdlib` if your app uses Kotlin already.

4. Configure `AndroidManifest.xml` to receive a callback response from the server.

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

## Implement

First of all, you have to choose an implementation type for your app. You can't have multiple implementation type in your app. Ask our representatives if you're unclear which one meets your demands.

|#| Library | Authorization flow | Misc |
|-|------- | ------------------ |-|
|1a| `MoneytreeLink` | PKCE; Saves a token into the SDK             ||
|1b| (Same as above) | Authorization code grant type; Saves a token into your external server | New in v4.0.0|
|2| `IsshoTsucho`   | PKCE; Saves a token into the SDK             ||

Then you can follow the implementation guide base on the type.

### [1a] MoneytreeLink Library (PKCE)

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
           .build();
    }
    ```

    TIPS: Ideally, a `Boolean` value for `isProduction` and a `String` value for `clientId` can be managed easily using [`resource`](https://developer.android.com/guide/topics/resources/more-resources.html#Bool) and [`Build Variants`](https://developer.android.com/studio/build/build-variants.html) in Android. You don't have to havem them as a static `String` or `Boolean` in code.

2. And then, initialize `MoneytreeLink` using the configuration file.
    ```java
    // Application class
    MoneytreeLink.init(this, configuration);
    ```

3. Update your activity class to enable to use `MoneytreeLink`.  All you have to do first is making a path to `authorize` for the guests. Every operations except `authorize` require the access token where the server provides when the guest agrees to authorize. Example is follows.

    ```java
     // Activity class
     findViewById(R.id.open_link_button).setOnClickListener(
        new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Authorize
                MoneytreeLink.client().authorizeFrom(
                    YourActivity.this,
                    new Authorization.OnCompletionListener() {
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
                    }
                );
            }
        }
    );
    ```

    `Authorization.OnCompletionListener` will be used when you want to handle callback in a result of authorization request.

### [1b] MoneytreeLink Library (Authorization code grant type)

Simply, it delegates token exchange stuff to your server in order to save an access token into your own database. Therefore, SDK has limitations under this option. For instance, `getToken` method never works or it can't register guest's device token via SDK. Because SDK doesn't have an access token. Your app has to communicate with your server to register/unregister a device token. Your server also have a responsibility refresh/revoke an access token based on guest activity.

1. Initialize `MoneytreeLinkConfiguration` at your `Application` class. It's almost same as the [MoneytreeLink section](#1a-moneytreelink-library-pkce) so you can read that instead. But don't forget to add `redirectUri` to the `MoneytreeLinkConfiguration`. It's like
   ```java
    final MoneytreeLinkConfiguration conf = new MoneytreeLinkConfiguration.Builder()
        .isProduction(false)
        .clientId("1234567890abcde...")
        .scopes(MoneyTreeLinkClient.GuestRead, ...)
        // Set redirectUri where your server endpoint to accept an auth code
        .redirectUri("https://your.server.com/token-exchange-endpoint")
        .build();
   ```
2. And then, initialize `MoneytreeLink` using the configuration file. See [the above section](#1a-moneytreelink-library-pkce) since it's same.

3. Update your activity class to make a path to start authorization. Don't forget giving a `state` value to a request parameter for security. Your server will identify guests from this value. It should be unique per request. See also [the guideline](https://www.oauth.com/oauth2-servers/server-side-apps/authorization-code/).

    ```java
     // Activity class
     findViewById(R.id.open_link_button).setOnClickListener(
        new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Authorize
                MoneytreeLink.client().authorizeFrom(
                    YourActivity.this,
                    // Note that it's different type in the above section
                    // since SDK never gets an access token.
                    new Action.OnCompletionListener() {
                        @Override
                        public void onSuccess() {
                            // It runs when your server makes a proper callback event.
                        }

                        @Override
                        public void onError(@NonNull final MoneytreeLinkException exception) {
                            // It runs when the SDK gets error during authorization.
                            // See the JavaDoc regarding MoneytreeLinkException
                        }
                    },
                    // state value
                    "unique-value-per-request"
                );
            }
        }
    );
    ```

4. Note that at least the following methods don't work under this option.
    - getToken
    - registerDeviceToken
    - deregisterDeviceTokend

### [2] Issho Tsucho Library

1. Initialize `MoneytreeLinkConfiguration` at your `Application` class. It's same as the [MoneytreeLink section](#1a-moneytreelink-library-pkce) so you can read that instead.

2. And then, initialize `IsshoTsucho` using the configuration file.
    ```java
    // at your Application class
    IsshoTsucho.init(this, configuration);
    ```

3. Update your activity class to start `IsshoTsucho` whenever you want.

    ```java
    IsshoTsucho.client().startIsshoTsucho(
      new IsshoTsucho.OnCompletionListener() { /* implement */ }
    );
    ```

    `IsshoTsucho.OnCompletionListener` is an optional instance, so you may set `null` if you don't need. It will describe sample usage in the later section.

4. `MoneytreeLink` instance is initialized when you initialize `IsshoTsucho`, so you can get it like

    ```java
    final MoneytreeLink linkClient = IsshoTsucho.linkClient();
    ```

    And you can see what `MoneytreeLink` (and `IsshoTsucho`) can by reading Javadoc.

## Register device token for push notification

If you want to register a device token for push notification, it should be done after guests give permission to access their data from your app. In this section, it proposes when the best timing to register device token is.

- If you choose [1a] option (`MoneytreeLink` with PKCE)

    After authorization flow would be the best timing. Example is follows.

    ```java
     // Authorize
     MoneytreeLink.client().authorizeFrom(
         YourActivity.this,
         new Authorization.OnCompletionListener() {
             @Override
             public void onSuccess(@NonNull final String accessToken) {
                 final String deviceToken = ... // You should provide the device token
                 // Registration method
                 MoneytreeLink.client().registerDeviceToken(
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
        }
     );
    ```

    `MoneytreeLink.clint().unregisterDeviceToken(...)` is used to unregister device token.

- If you choose [2] option (`Issho Tsucho`)

    `IsshoTsucho.OnCompletionListener` would be the best. See above regarding the sample code.

## Breaking Changes

### v3

MoneytreeLink SDK v3 brings some breaking changes regarding class name. The following is just an example, see also MainActivity.java in the Awesome app code and JavaDoc.

| Old Class Name                     | New Class Name                     | Note                                   |
| ---------------------------------- | ---------------------------------- | -------------------------------------- |
| CompletionHandler                  | Authorization.OnCompletionListener | Methods for authorization              |
| (same as above)                    | Action.OnCompletionListener        | Used for openSettings, openInstitution |
| MoneytreeLink.ApiCompletionHandler | Api.OnCompletionListener           | Renamed                                |
| IsshoTsucho.CompletionHandler      | IsshoTsucho.OnCompletionListener   | Renamed                                |

### v3.0.8

We **don't** support a browser that doesn't implement `Custom Chrome Tabs` anymore. The SDK returns `MoneytreeLinkException.Error.BROWSER_NOT_SUPPORTED` at `onError` of `OnCompletionHandler` from every possible methods to require `Custom Chrome Tabs` if a device doesn't have it. So, you can handle the error like this.

```java
// Any completionHandler code
@Override
public void onError(@NonNull MoneytreeLinkException exception) {
    if (exception.getError() == MoneytreeLinkException.Error.BROWSER_NOT_SUPPORTED) {
        // You can ask user to [install Android System WebView] or [change default browser to Google Chrome] here
    }
}
```
