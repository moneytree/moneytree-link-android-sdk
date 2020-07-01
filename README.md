# Moneytree Link SDK (Android) Set Up Instructions

- [Moneytree Link SDK (Android) Set Up Instructions](#moneytree-link-sdk-android-set-up-instructions)
  - [Requirements](#requirements)
  - [Set up dependencies and manifest](#set-up-dependencies-and-manifest)
  - [Implementation](#implementation)
    - [[1a] MoneytreeLink Library (PKCE)](#1a-moneytreelink-library-pkce)
    - [[1b] MoneytreeLink Library (Authorization code grant type)](#1b-moneytreelink-library-authorization-code-grant-type)
    - [[2] Issho Tsucho Library](#2-issho-tsucho-library)
  - [Register device token for push notification](#register-device-token-for-push-notification)
  - [Change Log](#change-log)
    - [v3](#v3)
    - [v3.0.8](#v308)
    - [v4.1.0](#v410)
    - [v4.1.1](#v411)
    - [v5.0.0](#v500)
    - [v5.1.0](#v510)
    - [v5.2.0](#v520)
    - [v5.3.0](#v530)
    - [v5.3.1](#v531)
  - [AwesomeApp](#awesomeapp)
    - [How to change the configuration easily](#how-to-change-the-configuration-easily)
  - [Troubleshooting](#troubleshooting)

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
    ```

> :information_source: The configuration of the reference `AwesomeApp` might also be helpful when setting up the SDK.

> :exclamation: All libraries are subjected to certain contracts with Moneytree and will not function if such a contract does not exist.

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
                    .completionHandler(/* Your handler */)
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

> :warning: The following methods will not work if `CODE_GRANT` is the selected auth method.
>    - getToken
>    - registerDeviceToken
>    - deregisterDeviceToken

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

## Change Log

### v3

MoneytreeLink SDK v3 brings some breaking changes to class names.
The following is just an example. Refer to the JavaDocs and Awesome app's MainActivity for more information.

| Old Class Name                     | New Class Name                     | Note                                   |
| ---------------------------------- | ---------------------------------- | -------------------------------------- |
| CompletionHandler                  | Authorization.OnCompletionListener | Methods for authorization              |
| (same as above)                    | Action.OnCompletionListener        | Used for openSettings, openInstitution |
| MoneytreeLink.ApiCompletionHandler | Api.OnCompletionListener           | Renamed                                |
| IsshoTsucho.CompletionHandler      | IsshoTsucho.OnCompletionListener   | Renamed                                |

### v3.0.8

> :warning: All web content is now handled with `Chrome Custom Tabs`, effectively removing support for other browsers. The SDK returns a `MoneytreeLinkException.Error.BROWSER_NOT_SUPPORTED` in the `onError` of the `OnCompletionHandler` for every flow that requires Chrome. Make sure to account for this error in case Chrome is not installed on the user's device.

```kotlin
// Any completionHandler code
override fun onError(exception: MoneytreeLinkException) {
    if (exception.getError() == MoneytreeLinkException.Error.BROWSER_NOT_SUPPORTED) {
        // You can ask user to [install Android System WebView] or [change default browser to Google Chrome] here
    }
}
```

### v4.1.0

We introduced `MoneytreeAuthOptions` class that replaces the array of variables previously used in `authorizeFrom` method. You can refactor your existing code by following the examples above. As described in JavaDoc, existing contracts of `authorizeFrom` will be removed in the next version.

### v4.1.1

The timing of callbacks originating from `openVault` and `openSettings` has been changed. In previous versions, callbacks returned when the Browser opened. Since v4.1.1 they return when users close the Browser (UI button or system back button).

### v5.0.0

- Minimum supported Android is now Android 5+
- Removed deprecated methods at `MoneytreeLink` class
- A `listener` for `openVaultFrom` has been changed to `Action.OnCompletionListener` from `Authorization.OnCompletionListener`. You need to call `getToken` whenever you want an `accessToken`.
- Updated versions of dependencies. See also [Set up dependencies and manifest](#set-up-dependencies-and-manifest) section above.

### v5.1.0

- `MoneytreeLink#client()` is now deprecated. Use `MoneytreeLink#getInstance()` instead.

### v5.2.0

Adds the following Vault navigation interface methods:
- `openVaultFrom(activity, listener, path)` that helps deep link to specific vault paths.
- `openCustomerSupport(...)` x2 variants. Deep link to the Vault's customer support page.
- `connectService(...)` x2 variants. Deep link to the Vault's service information page. Requires a service key to navigate.
- `serviceSetting(...)` x2 variants. Deep link to a service's settings page.
- `openServices(...)` x2 variants. Receives a `VaultOpenServicesOptions` and deep links to the Vault's search screen with relevant results. `VaultOpenServicesOptions` holds all the search parameters required to get results in the services screen.

Constructing a `VaultOpenServicesOptions` object:

```kotlin
val options = VaultOpenServicesOptions.Builder()
    .type("bank")
    .group("grouping_bank")
    .search("aeon")
    .build()
```

For more information on usage please refer to the JavaDocs and the AwesomeApp (example).

### v5.3.0

- Improvements to the Vault navigation interface methods added in [v5.2.0](#5.2.0)
- Adds `MoneytreeLinkConfiguration.Builder.linkEnvironment()`. Takes a `LinkEnvironment` to switch between the staging and production back ends.
- Adds new scopes
- Adds new Exception types and a new `Action.OnErrorListener` for better error management.

### v5.3.1

- Deprecates all Vault navigation interface methods
- Adds `openVault(requestCode, requestContext)` and `openVault(requestContext)` variant to replace previous navigation methods.
    - The `requestCode` is an Android activity result code. Checking for it in your `onActivityResult` callback along with `MoneytreeLink.RESULT_CODE_VAULT_CLOSED` will provide you with information of when the Vault Browser has closed. Supports both Activities and Fragments.
    - Adds `LinkRequestContext`. Use its builder to provide all required information for vault navigation.

```kotlin
MoneytreeLink.getInstance().openVault(
    vaultReqCode,
    LinkRequestContext.Builder.from(this) // Can be your activity or fragment
        .listener(object : Action.OnCompletionListener {
            override fun onSuccess() {
                // Browser opened...
            }

            override fun onError(exception: MoneytreeLinkException) {
                // Something went wrong...
            }
        })
        .guestEmail("awesome.email@awesome.awe")
        .build()
)
```
A snippet of the `LinkRequestContext.Builder` follows with all it's options and what they do:

```kotlin
LinkRequestContext.Builder.from(this) // Activity or Fragment
    .listener(Action.OnCompletionListener)
    // set the listener if you need to handle callbacks
    .vaultOpenServicesOptions(VaultOpenServicesOptions)
    // carries the search parameters the vault will search in services with
    .path(String)
    // Can be any of
    // MoneytreeLink.VAULT_SERVICE, opens the vault directly to a specific connected service.
    // MoneytreeLink.VAULT_SERVICE_SETTINGS, opens the vault to the settings of a specific connected service.
    // MoneytreeLink.VAULT_SUPPORT, opens the customer support page of the vault.
    // When vaultOpenServicesOptions() is used the path is overridden, using the proper path.
    .guestEmail(String)
    // if provided and unauthorized the login page will have the email field prefilled.
    .pathSuffix(String)
    // If the path is MoneytreeLink.VAULT_SERVICE, provide the service key you want to navigate to.
    // If the path is MoneytreeLink.VAULT_SERVICE_SETTINGS, provide the service id of which the settings you want to navigate to.
    // Using this in any other case will result in an invalid url forcing the vault to it's default page.
    .build()
```

## AwesomeApp

The `AwesomeApp` is a reference app to try out what the SDK provides. You can edit it as you please to understand the various use cases of the SDK.

### How to change the configuration easily

The `AwesomeApp` by default gets its `authType` and `isProduction` values from the `BuildConfig`. If you want to change them, you can edit the root `gradle.properties` and build the project again. Alternatively you can edit the source code as you see fit.

To switch between `PKCE` and `Code Grant` auth flows, you can edit as follows:

```properties
awesome.authType=com.example.myawesomeapp.AuthType.PKCE
```
or

```properties
awesome.authType=com.example.myawesomeapp.AuthType.CODE_GRANT
```

> :information_source: This is **NOT** part of the SDK's spec, just done so in the AwesomeApp.

## Troubleshooting

You might get some confusion or question while you try to implement the SDK against your app. In this case, we'd happy to help you of course, and it'd appliciate it if you could give the following information, especially when you face a specific issue around the SDK.

- SDK version
- A simple project that can reproduce your issue
  - You can modify `AwesomeApp` for example. It'd be hard to identify issues for us without specific code.
- Anything valuable other than above
