# Moneytree Link SDK (Android) Set Up Instructions

- [Moneytree Link SDK (Android) Set Up Instructions](#Moneytree-Link-SDK-Android-Set-Up-Instructions)
  - [Requirements](#Requirements)
  - [Set up dependencies and manifest](#Set-up-dependencies-and-manifest)
  - [Implementation](#Implementation)
    - [[1a] MoneytreeLink Library (PKCE)](#1a-MoneytreeLink-Library-PKCE)
    - [[1b] MoneytreeLink Library (Authorization code grant type)](#1b-MoneytreeLink-Library-Authorization-code-grant-type)
    - [[2] Issho Tsucho Library](#2-Issho-Tsucho-Library)
    - [Moneytree Intelligence](#Moneytree-Intelligence)
  - [Register device token for push notification](#Register-device-token-for-push-notification)
  - [Breaking Changes](#Breaking-Changes)
    - [v3](#v3)
    - [v3.0.8](#v308)
    - [v4.1.0](#v410)
    - [v4.1.1](#v411)
    - [v5.0.0](#v500)
    - [v5.1.0](#v510)

## Requirements

- `minSdkVersion` of your app should be >= 21 (Android 5, `Lollipop`)
- Add the Moneytree repository to your project configuration

```groovy
repositories {
    jcenter()
    google()

    // The moneytree repository
    maven {
        url "https://dl.bintray.com/moneytree/app.moneytree"
    }
}
```

## Set up dependencies and manifest

1. Define the Moneytree Link SDK core in your `dependencies` section

    ```groovy
    implementation("app.moneytree.link:core:__version__@aar") {
        transitive = true
    }
    ```

2. (Optional) Add libraries based on your contract with Moneytree.

    ```groovy
    // Issho tsucho
    implementation("app.moneytree.link:it:__version__@aar") {
        transitive = true
    }
    // Moneytree Intelligence
    implementation("app.moneytree.link:intelligence:__version__@aar") {
        transitive = true
    }
    ```

    - Note that libraries won't work unless you have a certain contract with Moneytree even if you add them.
    - The configuration of `AwesomeApp`, which is a reference app that we're offering, might be also helpful.

3. Configure `AndroidManifest.xml` to receive a callback response from the Moneytree server.

    1. Add `SchemeHandlerActivity` as the following example. It will receive a callback response when a user authorizes access to your app against Moneytree.

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

## Implementation

First of all, you have to choose an implementation type for your app. You can't have multiple implementation type in your app. Ask our representatives if you're unclear which one meets your demands.

| #   | Library         | Authorization flow                                                     | Misc          |
| --- | --------------- | ---------------------------------------------------------------------- | ------------- |
| 1a  | `MoneytreeLink` | PKCE; Saves a token into the SDK                                       |               |
| 1b  | (Same as above) | Authorization code grant type; Saves a token into your external server | New in v4.0.0 |
| 2   | `IsshoTsucho`   | PKCE; Saves a token into the SDK                                       |               |

Then you can follow the implementation guide base on the type.

### [1a] MoneytreeLink Library (PKCE)

1. Initialize `MoneytreeLinkConfiguration` at your `Application` class

    ```kotlin
    // Application class
    override fun onCreate() {
        super.onCreate()

        val configuration = MoneytreeLinkConfiguration.Builder()
            .isProduction(false)                         // or true if production
            .clientId("1234567890abcde...")              // your ClientId
            .scopes(MoneyTreeLinkClient.GuestRead, ...)  // scope(s)
            .build();
    ```

    TIPS: Ideally, a `Boolean` value for `isProduction` and a `String` value for `clientId` can be managed easily using [`resource`](https://developer.android.com/guide/topics/resources/more-resources.html#Bool) and [`Build Variants`](https://developer.android.com/studio/build/build-variants.html) in Android. You don't have to havem them as a static `String` or `Boolean` in code.

2. And then, initialize `MoneytreeLink` using the configuration file.

    ```kotlin
    // Application class
    MoneytreeLink.init(this, configuration)
    ```

3. Update your activity class to work with `MoneytreeLink`.  All you have to do first is making a path to `authorize` for the users. Every operations except `authorize` require an access token where the server offers when the user agrees to authorize. Example is follows.

    ```kotlin
    // Your activity class
    findViewById<Button>(R.id.open_moneytree_button).setOnClickListener {
        // Create options first
        val options = MoneytreeAuthOptions.Builder()
            .authorizationHandler(/* Your handler */)
            // ...
            // Add more options as you want
            // ...
            .build(MoneytreeLink.getInstance().getConfiguration())

        // Start authorization process
        MoneytreeLink.getInstance().authorizeFrom(
            this@YoutActivity,
            options
        )
    }
    ```

    `Authorization.OnCompletionListener` will be used when you want to handle callback in a result of authorization request.

### [1b] MoneytreeLink Library (Authorization code grant type)

Simply, it delegates token exchange stuff to your server in order to save an access token into your own database. Therefore, SDK has limitations under this option. For instance, `getToken` method never works or it can't register user's device token via SDK. Because SDK doesn't have an access token. Your app has to communicate with your server to register/unregister a device token. Your server also have a responsibility refresh/revoke an access token based on user activity.

1. Initialize `MoneytreeLinkConfiguration` at your `Application` class. It's almost same as the [MoneytreeLink section](#1a-moneytreelink-library-pkce) so you can read that instead. But don't forget to add `redirectUri` to the `MoneytreeLinkConfiguration`. It's like

    ```kotlin
    val conf = MoneytreeLinkConfiguration.Builder()
        .isProduction(false)
        .clientId("1234567890abcde...")
        .scopes(MoneyTreeLinkClient.GuestRead, ...)
        // Set redirectUri where your server endpoint to accept an auth code
        .redirectUri("https://your.server.com/token-exchange-endpoint")
        .build()
    ```

2. And then, initialize `MoneytreeLink` using the configuration file. See [the above section](#1a-moneytreelink-library-pkce) since it's same.

3. Update your activity class to make a path to start authorization. Don't forget giving a `state` value to an option instance for security. Your server will identify users from this value. It should be unique per request. See also [the guideline](https://www.oauth.com/oauth2-servers/server-side-apps/authorization-code/).

    ```kotlin
    // Your activity class
    findViewById<Button>(R.id.open_moneytree_button).setOnClickListener {
        // Create options
        val options = MoneytreeAuthOptions.Builder()
            .codeGrantTypeOptions(
                MoneytreeAuthOptions.CodeGrantTypeOptions.Builder()
                    .setState(/* Your state */)
                    .completionHandler(/* Yourh handler */)
                    .build()
            )
            // ...
            // Add other parameters if you want
            // ...
            .build(MoneytreeLink.client().getConfiguration())

            // Authorize
            MoneytreeLink.client().authorizeFrom(
                this@YourActivity,
                options
            )
    }
    ```

4. Note that at least the following methods don't work under this option.
    - getToken
    - registerDeviceToken
    - deregisterDeviceTokend

### [2] Issho Tsucho Library

1. Initialize `MoneytreeLinkConfiguration` at your `Application` class. It's same as the [MoneytreeLink section](#1a-moneytreelink-library-pkce) so you can read that instead.

2. And then, initialize `IsshoTsucho` using the configuration file.

    ```kotlin
    // at your Application class
    IsshoTsucho.init(this, configuration)
    ```

3. Update your activity class to start `IsshoTsucho` whenever you want.

    ```kotlin
    IsshoTsucho.client().startIsshoTsucho(
        IsshoTsucho.OnCompletionListener() { /* implement */ }
    )
    ```

    `IsshoTsucho.OnCompletionListener` is an optional instance, so you may set `null` if you don't need. It will describe sample usage in the later section.

4. `MoneytreeLink` instance is initialized when you initialize `IsshoTsucho`, so you can get it like

    ```kotlin
    val linkClient = IsshoTsucho.linkClient();
    ```

    And you can see what `MoneytreeLink` (and `IsshoTsucho`) can by reading Javadoc.

### Moneytree Intelligence

1. Add `MoneytreeIntelligenceFactory` to `MoneytreeLinkConfiguration` when you initialize the SDK.

    ```kotlin
    val configuration = MoneytreeLinkConfiguration.Builder()
        // ...
        // other settings..
        // ...
        .modules(MoneytreeIntelligenceFactory()) // Add this
        .build()
    ```

    If it initializes successfully, you will see the following log message.
    > I/MoneytreeLink: Initialized module (MoneytreeIntelligence)

2. You may add events whenever you want.

    ```kotlin
    MoneytreeIntelligence.getInstance().recordEvent("__event_name__")
    ```

    ```kotlin
    MoneytreeIntelligence.getInstance().recordEvent(
        "__event_name__",
        mapOf("__key__" to "__value__")
    )
    ```

3. You'll see recorded events in the Control Center.

## Register device token for push notification

If you want to register a device token for push notification, it should be done after users give permission to access their data from your app. In this section, it proposes when the best timing to register device token is.

- If you choose [1a] option (`MoneytreeLink` with PKCE)

    In the `onSuccess` method that runs after authorization flow finishes would be the best. Example is follows.

    ```kotlin
    // Authorization option
    val options = MoneytreeAuthOptions.Builder().authorizationHandler({
        object : Anuthorization.OnCompletionListener() {
            override fun onSuccess(accessToken: String) {
                val deviceToken = ... // You should set the device token here
                // Registration method
                MoneytreeLink.client().registerDeviceToken(
                    deviceToken,
                    object : Api.OnCompletionListener() {
                        override fun onSuccess() {
                            // It runs registering device token finishes successfully.
                        }

                        override fun onError(exception: MoneytreeLinkException) {
                            // It runs registering device token fails.
                        }
                    }
                )
            }

            override fun onError(exception: MoneytreeLinkException) {
                getStatusTextView().setText(exception.getMessage())
            }
        }
    })
    ...
    .build(MoneytreeLink.client().getConfiguration());
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

```kotlin
// Any completionHandler code
override fun onError(exception: MoneytreeLinkException) {
    if (exception.getError() == MoneytreeLinkException.Error.BROWSER_NOT_SUPPORTED) {
        // You can ask user to [install Android System WebView] or [change default browser to Google Chrome] here
    }
}
```

### v4.1.0

We introduced `MoneytreeAuthOptions` class that replaces array of variables against `authorizeFrom` method. You can refactor your existing code by following the examples above. As described in Javadoc, existing contracts of `authorizeFrom` will be removed in the next version.

### v4.1.1

The timing of callbacks that belong to `openVault` and `openSettings` has been changed. In the previous version, it runs when the WebView opens, but since v4.1.1 it runs when users close the WebView from the top left button (or hardware back button)

### v5.0.0

- Minimum supported Android is now Android 5+
- Removed deprecated methods at `MoneytreeLink` class
- A `listener` for `openVaultFrom` has been changed to `Action.OnCompletionListener` from `Authorization.OnCompletionListener`. You need to call `getToken` whenever you want an `accessToken`.
- Updated versions of dependencies. See also [Set up dependencies and manifest](#set-up-dependencies-and-manifest) section above.

### v5.1.0

We introduced [MoneytreeIntelligence](#moneytree-intelligence) library.

- `MoneytreeLink#client()` is now deprecated. Use `MoneytreeLink#getInstance()` instead.
