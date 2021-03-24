# Moneytree LINK SDK (Android)

The ***Moneytree Link SDK*** is a toolbox you can use to integrate with Moneytree's services.

The SDK provides ways to authenticate, store tokens, and launch Moneytree Web Services. It also provides a plugin for ***LINK Kit***, a fully integrated web PFM solution.

> :information_source: All code samples provided here are examples for your convenience.

## Contents

- [Moneytree LINK SDK (Android)](#moneytree-link-sdk-android)
  - [Contents](#contents)
  - [Integration Guide](#integration-guide)
    - [Getting the SDK](#getting-the-sdk)
    - [Configuring the SDK](#configuring-the-sdk)
      - [Manifest entries, callbacks](#manifest-entries-callbacks)
    - [Configuring Passwordless Sign Up/Login & Login Link](#configuring-passwordless-sign-uplogin--login-link)
    - [Initializing the SDK](#initializing-the-sdk)
      - [Initializing Core for PKCE flow](#initializing-core-for-pkce-flow)
      - [Initializing Core for Authorization Code Grant flow](#initializing-core-for-authorization-code-grant-flow)
      - [Initializing LINK Kit](#initializing-link-kit)
    - [Account OAuth Access Scopes (User Access Permissions)](#account-oauth-access-scopes-user-access-permissions)
  - [Using the SDK](#using-the-sdk)
    - [Authorizing the SDK (and your app)](#authorizing-the-sdk-and-your-app)
    - [Passwordless Sign Up/Login and Login Link](#passwordless-sign-uplogin-and-login-link)
      - [Passwordless Sign Up/Login](#passwordless-sign-uplogin)
      - [Login Link](#login-link)
    - [De-authorizing, Logging Out](#de-authorizing-logging-out)
    - [Opening the Vault](#opening-the-vault)
    - [Open account settings](#open-account-settings)
    - [SDK callback flow](#sdk-callback-flow)
    - [Register device token for push notification](#register-device-token-for-push-notification)
  - [Change Log](#change-log)
  - [SDK 4/5 to 6 Migration Guide](#sdk-45-to-6-migration-guide)
  - [AwesomeApp Example Setup](#awesomeapp-example-setup)
  - [Troubleshooting](#troubleshooting)

## Integration Guide

The Moneytree LINK SDK is comprised of a Core SDK and the Moneytree LINK Kit module.

The Core SDK is the tool offering you connectivity to our services, plus some state handling for tokens.

The Moneytree LINK Kit provides you with a web based UI version of our PFM services and is there to simplify the integration process.

> :information_source: Moneytree LINK Kit is the name of the refreshed and re-branded version of Issho Tsucho.


### Getting the SDK

> :warning: The SDK requires Android 5.0 (Lollipop) and above.
> Your `minSdkVersion` must be set to at least `21`.

The Moneytree LINK SDK is offered through Maven Central. Add the following dependencies in your `build.gradle` or `build.gradle.kts` file:

```kotlin
dependencies {
    // Moneytree LINK SDK
    implementation("app.moneytree.link:core:6.x.x")
}
```

**Optionally,** if you have contracted LINK Kit:

```kotlin
dependencies {
    // Moneytree LINK SDK
    implementation("app.moneytree.link:core:6.x.x")
    // LINK Kit too.
    implementation("app.moneytree.link:link-kit:6.x.x")
}
```

> :exclamation: All libraries are subjected to certain contracts with Moneytree. Moneytree servers will reject any SDK request that does not have a valid and active contract.

### Configuring the SDK

#### Manifest entries, callbacks

The SDK works by receiving callbacks from our web services through custom schemes. The required *Android Manifest* entries have already been setup and will be merged by the *Manifest Merger*. They are using *Android Manifest Variables*, so to receive the callbacks you need to add the following to your app's `build.gradle` file.

```groovy
android {
    defaultConfig {
        manifestPlaceholders += [
            // For production remove `-staging` from below
            "linkHost": "myaccount-staging.getmoneytree.com",
            // clientIdShort: "<first 5 chars of your client ID>"
            "clientIdShort": "xxxxx"
        ]
    }
}
```

Replacing `xxxxx` above with the **first 5 characters of your Client ID**.
For example; if your  Client ID is `abcde1234567890moneytree`, your `clientIdShort` would be `abcde`.

> :information_source: The Client ID will be provided by Moneytree once you have an active contract. Please contact us for more information or further questions.

Of course, you can choose any ways to define the values above as you like.

### Configuring Passwordless Sign Up/Login & Login Link

You need to add the following intent filter to an activity of your choice:

```xml
<intent-filter>
    <action android:name="android.intent.action.VIEW" />
    <category android:name="android.intent.category.DEFAULT" />
    <category android:name="android.intent.category.BROWSABLE" />

    <data
        android:host="${linkHost}"
        android:pathPrefix="/link/${clientIdShort}"
        android:scheme="https" />
</intent-filter>
```

You will finally need to add the `INTERNET` permission if you do not have it already.

```xml
<uses-permission android:name="android.permission.INTERNET" />
```

> :information_source: Complete configuration by contacting Moneytree with the information in *[Passwordless Sign Up/Login and Login Link](#passwordless-sign-uplogin-and-login-link)*

### Initializing the SDK

In this step you first need to choose an authorization type for your application. Ask our representatives if you are unclear as to which one meets your needs.

The supported authorization flows are:

| Module   | Authorization flow                                                                 | Notes                                                                                      |
| -------- | ---------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------ |
| Core     | PKCE; Saves the user access token on device                                        |                                                                                            |
| Core     | Authorization Code Grant Type; Saves the user access token in your external server | Since v4.0.0                                                                               |
| LINK Kit | PKCE; Saves a token locally on device                                              | Using this module forces the PKCE flow as its purpose is to keep the implementation simple |

#### Initializing Core for PKCE flow

The entry point of the SDK is the `MoneytreeLink` class which contains all the public interfaces.

To initialize it call its `init(context, configuration)` method. The `configuration` parameter is of type `MoneytreeLinkConfiguration`. Use its builder to provide the SDK with its configuration:

```kotlin
// Application class
override fun onCreate() {
    super.onCreate()

    val configuration = MoneytreeLinkConfiguration.Builder()
        .linkEnvironment(LinkEnvironment.Staging)    // .Staging or .Production
        .clientId("1234567890abcde...")              // your ClientId
        .scopes(MoneyTreeLinkClient.GuestRead, ...)  // scope(s)
        .build();

    MoneytreeLink.init(this, configuration)
}
```

The `scopes(...)` function used in the `Builder` above is basically a set of permissions the user needs to provide to the accessing application to be able to get a user token. More on this in [Account OAuth Access Scopes (User Access Permissions)](#account-oauth-access-scopes-user-access-permissions).

> :information_source: As the SDK is a singleton and will most likely be used throughout your app we strongly recommend to do all initialization inside your `Application` class.

#### Initializing Core for Authorization Code Grant flow

Authorization code grant is a type of auth flow that delegates the user access token exchange process to your server. That way you gain more control over the token's location, however, with some drawbacks.

The SDK functionality is limited when using an authorization code as it does not have the access token required. For instance the `getToken()` method cannot function and will throw an error, or the SDK cannot be used to register the user's FCM token. Another drawback is the addition of networking steps in the process as your app now has to communicate with your server to register/unregister access tokens based on user activity.

The SDK initialization is basically the same as in the [PKCE flow](#initializing-core-for-pkce-flow), with a small difference on the initialization of the `MoneytreeLinkConfiguration` class:

```kotlin
val conf = MoneytreeLinkConfiguration.Builder()
    .linkEnvironment(LinkEnvironment.Staging)
    .clientId("1234567890abcde...")
    .scopes(MoneyTreeLinkClient.GuestRead, ...)
    // Set redirectUri to your server endpoint to receive an auth code
    .redirectUri("https://your.server.com/token-exchange-endpoint")
    .build()
```

We add a call to `redirectUri(uri)` that takes the string URI of the endpoint connection for token processing.

> :warning: The following methods will not work if `Authorization Code Grant` is the selected auth flow.
>
> - getToken
> - registerRemoteToken
> - unregisterRemoteToken

#### Initializing LINK Kit

LINK Kit, the re-brand and refresh of Issho Tsucho, is a fully integrated web PFM (Personal Financial Management) module offered with the purpose of simplifying the presentation of the data we offer.

> :warning: To use LINK Kit you need to have a contract and in extension a Client ID that supports it.

To initialize the module you first need a `MoneytreeLinkConfiguration`, similar to the [PKCE flow](#initializing-core-for-pkce-flow).

Then call the `LinkKit`'s `init(context, configuration)`:

```kotlin
// Application class
override fun onCreate() {
    super.onCreate()

    val configuration = MoneytreeLinkConfiguration.Builder()...

    // Initializing LINK Kit
    LinkKit.init(this, configuration)
}
```

You will notice that you do not need to call `MoneytreeLink.init(...)`. LINK Kit will take care of initializing the Core SDK for you. In case that you need to access it call the following:

```kotlin
val linkClient = LinkKit.linkClient();
```

### Account OAuth Access Scopes (User Access Permissions)

As seen in the previous sections, a set of scopes must be defined at initialization for the SDK to function. These are effectively data access level permissions that the host app requests from the user's account. What you can do with the access provided depends on the scopes defined.

The Link SDK is defining the following scopes in the `MoneytreeLinkScope` enum:

| Scope                      | Provided permission                                                                                                 |
| -------------------------- | ------------------------------------------------------------------------------------------------------------------- |
| GuestRead                  | Access to basic account information.                                                                                |
| AccountsRead               | Access to read personal account balances and information.                                                           |
| TransactionsRead           | Access to read personal account transactions.                                                                       |
| TransactionsWrite          | Access to write personal account transactions.                                                                      |
| CategoriesRead             | Access to read transaction categories.                                                                              |
| InvestmentAccountsRead     | Access to read investment account balances and information.                                                         |
| InvestmentTransactionsRead | Access to read investment account transactions.                                                                     |
| RequestRefresh             | Allows your application to manually request Moneytree to retrieve up-to-date user data from financial institutions. |
| PointsRead                 | Access to read point account information.                                                                           |
| PointTransactionsRead      | Access to read point account transactions.                                                                          |
| NotificationsRead          | Access to read notification information.                                                                            |

For the base functionality of the SDK (Vault, Login/out, Customer support) we recommend to at least ask for the following 3 scopes:

- GuestRead
- AccountsRead
- TransactionsRead

LINK Kit has strict requirements and is always requesting the following scopes:

- GuestRead
- AccountsRead
- TransactionsRead
- TransactionsWrite
- PointsRead
- InvestmentAccountsRead
- InvestmentTransactionsRead

Contact our representative if you have questions about scopes.

## Using the SDK

### Authorizing the SDK (and your app)

The first step to using the SDK is to authorize it, have a valid login/register for an account and consequently a token.

You can authorize by calling `authorize(activity, options)` on the `MoneytreeLink` singleton. The `config` parameter is of type `LinkAuthOptions.Authorize`.
You can use `LinkAuthOptions.create {}` (LinkAuthOptions.Companion.create in Java) extension function or the `LinkAuthOptions.builder()` to setup your authorization request.

***PKCE flow:***

```kotlin
// Create your options first
val authConfig = LinkAuthOptions
    .create {
        authFlow = LinkAuthFlow.Pkce.create()
    }
    .buildAuthorize(email = "user@example.com")

// Start authorization process
MoneytreeLink.getInstance().authorize(activity, authConfig)
```

***Authorization Code flow:***

Similar to PKCE flow above. The main difference is that the `LinkAuthOptions.CodeGrant(String)` requires a state string for the OAuth process. This state needs to be unique per request. For more information refer to [the OAuth guidelines](https://www.oauth.com/oauth2-servers/server-side-apps/authorization-code/).

```kotlin
// Create options
val authConfig = LinkAuthOptions
    .create {
        authFlow = LinkAuthFlow.CodeGrant("random-state-string") // Provide the OAuth Authorization Code flow state
    }
    .buildAuthorize(email = "user@example.com")


// Authorize
MoneytreeLink.getInstance().authorize(activity, authConfig)
```

> :information_source: Full `LinkAuthOptions` builder options:
>
>```kotlin
>LinkAuthOptions.builder()
>    .presentSignUp(Boolean) // Boolean on whether to show login or register on the web
>    .auth(LinkAuthFlow) // `Pkce.create()` or `CodeGrant("state")`. Choose your poison...
>    .forceLogout(Boolean) // Will force the login flow again
>    // Used for flows that go through the `MoneytreeLink.getInstance().authorize(...)` call.
>    // Takes the user's email. This is optional and can be used to pre-populate the email fields
>    // in the web form.
>    .buildAuthorize(String)
>    // Used for flows that go through the `MoneytreeLink.getInstance().onboard(...)` call (more on this in the next section).
>    // Takes the user's email. This is required as the process creates the account with this email
>    // and sends a confirmation email.
>    .buildOnboarding(String)
>```

***LINK Kit flow:***

LINK Kit enforces a PKCE flow behind the scenes as it needs to have the user token available to start properly scoped.

To start LINK Kit use:

```kotlin
LinkKit.getInstance().launch(
    activity,
    object : LinkKit.LinkKitListener {
        override fun onLaunched() {
            // callback
        }

        override fun onError(exception: MoneytreeLinkException?) {
            // callback
        }
    }
)
```

`LinkKit.LinkKitListener` is not required if you do not need to get callbacks.

### Passwordless Sign Up/Login and Login Link

_Passwordless Sign Up/Login and Login Link_ are new secure, passwordless, email-based registration and login features offered from v6 in order to allow your customers easier access to Moneytree services. These features are email based. When _Passwordless Sign Up/Login_ is requested, the user will receive a one-time url capable of creating an account. When _Login Link_ is requested, the user will receive a one-time url that can log them in or navigate to their account settings.

> :warning: Passwordless Sign Up/Login is currently available _only_ for Core services. Login Link is available for _all_ services, including LINK Kit.
> :warning: Please complete [Configuring Passwordless Sign Up/Login & Login Link](#configuring-passwordless-sign-uplogin--login-link) first.

You must inform Moneytree's integration team if you want to support either or both Passwordless Sign Up/Login and Login Link. When doing so, please provide your client ID, the bundle ID of your iOS app and the SHA-1 fingerprint certificate of your Android app, as well as whether it is for the test environment, production, or both.

Your SHA-1 fingerprint certificate is necessary because Login Link uses _Android App Links_ for extra security. It verifies the connection between your app and the link received and send the intent directly to your app without showing the system's app selection sheet. To that effect you will need to provide us with your production key's fingerprint for your released app. If you want to be able to confirm this functionality on your debug artifact as well you will have to create a static debug signing key and provide its fingerprint too. You can learn more on App Links [here](https://developer.android.com/training/app-links/verify-site-associations)

Once Moneytree completes the configuration of your app, your users will see the new registration and login screens. Note that these screens still provide the option to register or log in with a password if they prefer.

#### Passwordless Sign Up/Login

To use it call `onboard(Activity, LinkAuthOptions.Onboarding)`, where:

- `activity` is the `Activity` in the context of which the onboarding request is created.
- `options` of type `LinkAuthOptions.Onboarding`. Constructed by calling `LinkAuthOptions.create {}` (LinkAuthOptions.Companion.create in Java) extension function or the `LinkAuthOptions.builder()`, it holds all the information required to go through any authorization flow.

```kotlin
val authConfig = LinkAuthOptions
    .create {
        authFlow = LinkAuthFlow.Pkce.create()
    }
    .buildOnboarding(email = "user@example.com") // Email is required for Onboarding

// Launch the onboarding process
MoneytreeLink.getInstance().onboard(activity, options)
```

#### Login Link

It has two main uses. Login and navigation.

Login can be done through any of the normal auth flows. The user will be given the option to login through email link (Login Link) or using email and password.

For requesting a Login Link navigation use `requestMagicLink(String, String, Action)`, where the parameters are:

- `email`: the user's email used to locate the user requesting the link.
- `destination`: the path that will be used to navigate to the requested account page
- `listener`: of type `Action.OnCompletionListener`, tells us if requesting the Magic Link has succeeded or not.

> :information_source: The destinations currently supported by magic links are:
>
> - `MoneytreeLink.ML_DESTINATION_SETTINGS`, navigating to the settings page of the user's account.
> - `MoneytreeLink.ML_DESTINATION_LANGUAGE`, navigating to the language selection page of the user's account settings.
> - `MoneytreeLink.ML_DESTINATION_AUTHORIZED_APPS`, navigating to the authorized applications page of the user's account settings.
> - `MoneytreeLink.ML_DESTINATION_DELETE_ACCOUNT`, navigating to the account deletion page of the user's account settings.
> - `MoneytreeLink.ML_DESTINATION_EMAIL_PREFERENCES`, navigating to the email preferences page of the user's account settings.
> - `MoneytreeLink.ML_DESTINATION_UPDATE_EMAIL`, navigating to the email update page of the user's account settings.
> - `MoneytreeLink.ML_DESTINATION_UPDATE_PASSWORD`, navigating to the password update page of the user's account settings.

After you request a Login Link flow you need to consume the incoming deep link.
To do so, you need to capture the URI from the email using an [Intent Filter](#Manifest-entries-callbacks) and then provide it to the SDK using `consumeMagicLink(AppCompatActivity, Uri, Action)`.

When you receive the intent in your activity call `consumeMagicLink(AppCompatActivity, Uri, Action)`

```kotlin
intent.data?.also { uri ->
    MoneytreeLink.getInstance().consumeMagicLink(Activity, Uri)
}
```

The parameters are:

- `activity`: is the context used to open the in app browser and setup a `LinkResultListener` if the `listener` parameter is set.
- `uri`: the Uri you receive from `intent.getData()` when the app captures the link from the received email.
- `listener`: optional (overloaded), automatically adds, and removes when not needed, an `Action` listener to the centralized lifecycle capable callback system.
This is more of a convenience tool rather than a necessity as you can still subscribe to the events of the system as explained in [SDK callback flow](#SDK-callback-flow)

### De-authorizing, Logging Out

You might need to logout your user from the Moneytree services or clear the tokens the SDK is holding. For that we provide the following functions:

- `deleteCredentials()` clears the saved Token from the encrypted storage.
- `logout(activity)` takes the activity instance and launches an in-app browser flow calling our services to logout and clear all browser sessions. Calls `deleteCredentials()` internally.

> :warning: Please have in mind that `deleteCredentials()` does not clear the user's logged in state from the browser session. It simply deletes the access token the SDK holds. If you try to authorize again and the browser session has not expired you will not go through the login page. A new Token will be provided for the SDK to store.

### Opening the Vault

To open the Vault the SDK interface provides the `openVault(Activity, LinkRequestContext)` function, where:

- `activity` is the context used to start the web navigation process.
- `requestContext` is created using `LinkRequestContext.Builder` and holds all the required information for opening the vault and navigating to the requested page.

```kotlin
MoneytreeLink.getInstance().openVault(
    activity,
    LinkRequestContext.Builder()
        .userEmail(email = "user@example.com") // Used to pre-populate the login screen if not already logged in.
        .build()
)
```

> :warning: Opening the Vault before calling `authorize(...)` will bring up the Moneytree login site and create a session, but will not provide an access token to the SDK and in extend to the app.
> This is done by design and to offer a way to open the Vault outside the responsibility of your app. While in this state you can still call `authorize(...)` to get the access token.
>
> :information_source: A snippet of the `LinkRequestContext.Builder` follows with all it's options and what they do:
>
>```kotlin
>LinkRequestContext.Builder()
>    // carries the search parameters the vault will search with in its services list
>    .vaultOpenServicesOptions(VaultOpenServicesOptions)
>    // The path to navigate to. Can be any of:
>    // MoneytreeLink.VAULT_SERVICE, opens the vault directly to a specific connected service.
>    // MoneytreeLink.VAULT_SERVICE_SETTINGS, opens the vault to the settings of a specific connected service.
>    // MoneytreeLink.VAULT_SUPPORT, opens the customer support page of the vault.
>    // When vaultOpenServicesOptions() is used the path is overridden, using the proper path.
>    .path(String)
>    // if provided and unauthorized the login page will have the email field pre-filled.
>    .userEmail(String)
>    // If the path is MoneytreeLink.VAULT_SERVICE, provide the service key you want to navigate to.
>    // If the path is MoneytreeLink.VAULT_SERVICE_SETTINGS, provide the service id of the connected service's settings you want to navigate to.
>    // Using this in any other case will result in an invalid url forcing the vault to it's default page.
>    .pathSuffix(String)
>    .build()
>```

The `LinkRequestContext.Builder` gives you the ability to provide a `path(String)` with some parameters (`pathSuffix(String)` or `VaultOpenServicesOptions`) to open a specific page of the Vault.

Generally, the pages you can navigate to are:

- The top Vault page, where a list of your connected services and their top level information are displayed.
- The details page of a specific service.
- The settings page of a connected service, if for example you need to remove it from your account.
- The services search screen. Search will be performed for the parameters provided in `vaultOpenServicesOptions(...)`.

The search feature takes a `VaultOpenServicesOptions` object that can be constructed using its `Builder`

```kotlin
VaultOpenServicesOptions.Builder()
    .type("bank") // the type of service you are looking for
    .group("grouping_bank") // group the service belongs to
    .search("aeon") // the actual term you are looking for (search bar contents)
    .build()
```

> :information_source: The possible groups to search for are the following:
>
> - grouping_bank
> - grouping_bank_credit_card
> - grouping_bank_dc_card
> - grouping_corporate_credit_card
> - grouping_credit_card
> - grouping_credit_coop
> - grouping_credit_union
> - grouping_dc_pension_plan
> - grouping_debit_card
> - grouping_digital_money
> - grouping_ja_bank
> - grouping_life_insurance
> - grouping_point
> - grouping_regional_bank
> - grouping_stock
> - grouping_testing

### Open account settings

You might want to provide an easy way to allow the user to access their Moneytree account settings from within your app.

The SDK provides the interface function `openSettings(Activity, String)` for that purpose. The function does not use the saved tokens. Instead it is using the session information from your browser. If that session is expired then the user will have to login again. This is designed this way to offer an extra layer of security as the user is accessing possibly destructive features.

The parameters required are:

- `activity`: the context used to open the in app browser
- `email`: used to pre-fill the login web form

```kotlin
MoneytreeLink.getInstance().openSettings(
    activity,
    "user@example.com"
)
```

> :information_source: As described in [Passwordless Sign Up/Login and Login Link](#passwordless-sign-uplogin-and-login-link) you can perform settings navigation using Magic Links

### SDK callback flow

The SDK, since v6.0, offers a new centralized callback system (delegate) that takes care of moving the required events and any data that they might need to hold. Your application simply needs to implement for these in a location that will respect the Android lifecycle (most of the time `onCreate` of your component). This will ensure that callbacks will be received under all events that might cause a restart of your component, like configuration changes or process death due to memory pressure.

Internally this system holds a list of listeners and sends the events to all of them. This means that, for example, you can have a listener listening to specific events on the application level and another one in your Activity or Fragment.

The interface of this listener is the `LinkResultListener` and can be added by calling `addOnLinkResult(LifecycleOwner, LinkResultListener)`, where:

- `lifecycleOwner` is used to follow the lifecycle of the component setting up the listener and remove it in its `onDestroy()` call.
- `onLinkResult` the listener itself.

To listen to these event you have 2 options:

1. You can directly use `addOnLinkResult(...)` and manually check for combinations of `LinkResult`, `LinkEvent`, and `LinkError` to know what it is that you are receiving.

    ```kotlin
    MoneytreeLink.getInstance().addOnLinkResult(this) { result ->
        when {
            result is LinkResult.Authorized -> {
                // Authorized and you token lives here: result.token?.value
                // If null you are using Code Grant auth flow
            }
            result is LinkResult.Event && result.event == LinkEvent.LoggedOut -> {
                // Logged out
            }
            result is LinkResult.Event && result.event == LinkEvent.VaultClosed -> {
                // The Vault has closed
            }
            result is LinkResult.Event && result.event == LinkEvent.ExternalOAuthAdded -> {
                // Auth credential that launches in full browser has just been added.
            }
            result is LinkResult.Error -> {
                val error = result.moneytreeLinkException.error
                if (error == LinkError.UNAUTHORIZED) {
                    // User didn't complete authorization process.
                } else {
                    // Look into what an error happened
                }
            }
            else -> Unit
        }
    }
    ```

2. You can use a set of Kotlin extension functions provided.

    ```kotlin
    with(MoneytreeLink.getInstance()) {
        onCodeGrantAuthorized(context) {
            // Code Grant flow Auth complete
        }
        onPkceAuthorized(context) { token ->
            // PKCE flow Auth complete, get your token here: token.value
        }
        onLoggedOut(context) {
            // Logged out
        }
        onEvent(context) { event ->
            when (event) {
                LinkEvent.ExternalOAuthAdded -> {
                    // Auth credential that launches in full browser has just been added.
                }
                LinkEvent.LinkWebSessionStarted -> {
                    // You can do here whatever needs to happen before the in app browser opens
                }
                LinkEvent.LinkWebSessionFinished -> {
                    // Whatever needs to happens after the browser closes.
                    // This has no specific meaning. It simply indicates that the browser has closed
                    // For example VaultClosed should be used if you need to refresh data
                }
                LinkEvent.RequestCancelled -> {
                    // This event is transmitted when the in app browser closes without a scheme triggering
                    // a flow. This means that the browser close button or system back button were used.
                }
                LinkEvent.VaultOpened -> {
                    // Specific event to indicate that the browser is opening the vault.
                }
                LinkEvent.VaultClosed -> {
                    // The Vault closed, the user might have done something, update or something...
                }
                LinkEvent.LoggedOut -> {
                    // This is the event for a successful logout.
                    // The extension function onLoggedOut is a shortcut of this.
                }
            }
        }
        onError(context) { e ->
            if (e.error == LinkError.UNAUTHORIZED) {
                // User didn't complete authorization process.
            } else {
                // Look into what an error happened
            }
        }
    }
    ```

If you would like to use the above extension from *Java* (Java 8 required), we are providing the following way:

```java
MoneytreeLink link = MoneytreeLink.Companion.getInstance();
MoneytreeLinkExtensions.onPkceAuthorized(link, this, (token) -> {
    // Your token is in: token.getValue()
});
MoneytreeLinkExtensions.onEvent(link, this, (event) -> {
    if (event == LinkEvent.LoggedOut) {
        // Logged out
    }
});
```

### Register device token for push notification

We offer the ability to register a device token with our services. If you do so the users will be able to receive push notifications with useful information about their account.
Such registration should be done after the user has given permission to access their data from your app, while you should unregister the token when the user logs out, or takes other destructive actions.

> :warning: This feature is available only when using the PKCE OAuth flow and in extension when using LINK Kit

The best place to register for the token would be when you receive the callback for a successful authorization

For example when using Core SDK with PKCE

```kotlin
with(MoneytreeLink.instance) {
    onPkceAuthorized(context) {
        // Register for notifications
        val deviceToken = yourMethodToGetDeviceToken()
        MoneytreeLink
            .instance
            .registerRemoteToken(
                deviceToken,
                object : Action {
                    override fun onSuccess() {
                        // callback
                    }

                    override fun onError(exception: MoneytreeLinkException) {
                        // callback
                    }
                }
            )
    }
}
```

> :information_source: `registerRemoteToken(...) is clever enough to do nothing is an old token is used again or unregister an old token if a new one is provided, so you do not need to care about that.

Similarly you can call `unregisterRemoteToken(String, Action?)` when logging out to ensure no token is still active.

When using only LINK Kit and want to register for notification you can do a similar operation in the `onLaunched()` event of the LINK Kit launcher:

```kotlin
LinkKit.getInstance().launch(
    activity,
    object : LinkKit.LinkKitListener {
        override fun onLaunched() {
            val deviceToken = yourMethodToGetDeviceToken()
            MoneytreeLink
                .instance
                .registerRemoteToken(
                    deviceToken,
                    object : Action {
                        override fun onSuccess() {
                            // callback
                        }

                        override fun onError(exception: MoneytreeLinkException) {
                            // callback
                        }
                    }
                )
        }

        override fun onError(exception: MoneytreeLinkException) {
        }
    }
)
```

## [Change Log](./docs/change-log.md)

## [SDK 4/5 to 6 Migration Guide](./docs/migration-guide.md)

## [AwesomeApp Example Setup](./docs/awesome-app.md)

## Troubleshooting

You might have questions concerning the integration of the SDK in your application. We are happy to provide support.

On that front we would appreciate if you can provide us the following information:

- Environment information like:
  - SDK version
  - Android version
  - Anything else that might be specific to your setup
- A simple project that can reproduce your issue
  - We **strongly** recommend you try and reproduce your issue by modifying the ***AwesomeApp***. If not able then any working sample will do. It can be hard to find issues without the implementation details.
- Anything else you might think that can help us identify the problem and help you with it.
