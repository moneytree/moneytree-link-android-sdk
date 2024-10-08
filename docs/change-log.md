# Change Log

## v6.6.4

- Fixed issue with LINK Kit not respecting user logged in status when attempting to open the Vault from its settings.

## v6.6.3

- No changes to SDK.
- Made corrections to the text of the sample app, "AwesomeApp."

## v6.6.2

- No changes to SDK.
- Made corrections to the text of the sample app, "AwesomeApp."

## v6.6.1

Hotfix, for an issue with Passwordless Login not working when opening the Moneytree Account settings. 

## v6.6.0

We have introduced a new authentication configuration parameter that will allow you to choose your preferred mode (Authentication, Passwordless, Single Sign On) when the Moneytree authentication web page shows. This configuration does _not_ guarantee that the selected mode will be the one presented as the feature relies on the configuration of the client ID provided to you.

If you do not provide the new configuration, the default order of available methods configured in you client ID will be used.

For more on the feature and how it works, please refer to [Choosing your Authentication method](../README.md#choosing-your-authentication-method).

### Added
- `AuthenticationMethod` enum that allows to choose your preferred authentication mode.
- `create` function in `LinkAuthOptions`'s `companion object` to provide a Kotlin friendly way to build the object.
- `create` functions in `MoneytreeLinkConfiguration`'s `companion object` to provide a Kotlin friendly way to build the object. There is an overload of the function providing the ability to add scopes directly as `String`s or using the `MoneytreeLinkScope` enum.
  - For Java user the overloads appear with the names `createWithStringScopes` and `createWithEnumScopes`.
- `MoneytreeLinkConfiguration.Builder` now provides `authenticationMethod` with which you can set the authentication method.

## v6.5.4

Added additional scope, `RequestRefresh`, to LINK Kit.

## v6.5.3

Improved internal state (OAuth) handling. 

For Code Grant the SDK would require simple, unencoded strings to be provided. This change removes that requirement. 

## v6.5.2

Improved handling of app revocation for LINK Kit. If an end user has revoked authorization for your application, instead of returning an error, LINK Kit will automatically give the user an opportunity to re-authenticate. This reduces the need for error handling for LINK Kit implementations.

## v6.5.1
- Calling `MoneytreeLink#logout` will result to only one (instead of two) `LinkEvent.LoggedOut` event be sent to `LinkResultListener` listeners at the end of a successful logout flow.
- This is to be consistent with previous versions of the SDK prior to version 6.

## v6.5.0
- Deprecated all types related to the ability to select Code Grant (without PKCE) as your auth type. Code Grant without PKCE will be removed in upcoming versions.
  - `LinkAuthOptions.Builder.auth()`
  - `LinkAuthFlow`, `LinkAuthFlow.CodeGrant`, `LinkAuthFlow.Pkce`
  - `MoneytreeLinkConfiguration.Builder.redirectUri()`
  - `LinkError.FUNCTION_IS_NOT_AVAILABLE`

## v6.4.0
- Updated SDK's `compileSdk` to 33
- Updated Sample App's `targetSDK` and `compileSDK` to 33
- Changed in-app-browser handling to now allow use of any browser supporting the Custom Tabs framework.
- Introduced further filtering for Trusted Web Activities (TWA) for LINK Kit. LINK Kit now requires a TWA capable browser to be available.
  - Introduced a separate `LinkError.LINK_KIT_NOT_SUPPORTED` to notify the client app when Trusted Web Activity is not available.
- The SDK will now open Custom Tabs in the following order:
  - Chrome stable or beta version
  - System default browser if TWA capable
  - Any available TWA capable browser
  - System default browser if Custom Tabs capable
  - Any available Custom Tabs capable browser.

## v6.3.0
- Updating from ISTC to LINKIT will no longer require user to re-authenticate with login credentials. They will only need to grant consent to the new LINKIT scopes.
- Remove `SchemeHandlerActivity` and internally replace it with `LinkHandlerActivity`
- Finalize implementation of `updateConfiguration` by addressing some of it's internal issues

## v6.2.1

- Fixes an issue where LINK Kit would close, instead of re-authenticating, if the token did not have sufficient scopes.

## v6.2.0

`ClientAccessToken` now exposes the full OAuth token response in a backward compatible way.
New properties are:

* `ClientAccessToken.accessToken` - same as `ClientAccessToken.value`
* `ClientAccessToken.refreshToken` - use it for the OAuth `refresh` grant.
* `ClientAccessToken.createdAt` and `ClientAccessToken.expiresIn` - inform you about the token's expiry.

`ClientAccessToken.value` is deprecated in favor of `ClientAccessToken.accessToken`.

## v6.1.3 [Deprecated]

> :exclamation: This version has known delivery issues. We have requested its removal from Maven Central but unfortunately is still available. Please skip this version and update to the latest one if you need the fixes mentioned here!

- `SchemeHandlerActivity` is now `exported`, this fixes an issue when building with `targetSDKVersion` is `31`
- Fixes an issue with closing LINK Kit
- Fixes an issue where opening a Login Link from email would fail to complete Code Grant auth flow
- Fixes an issue where Universal Vault unexpectedly opened the Add Services screen instead of the Financial Services list. If your implementation expects the first screen shown to be Add Services by default, you will need to open vault with `openVaultServicesOptions` instead.

## v6.1.2

- Fixes an issue with OAuth including unnecessary parameters
- Fixes an issue with token storage when migrating from an earlier version of the SDK
- Improved documentation of sample app usage

## v6.1.1

- Fixes an internal encoding that impacted authentication in some use cases
- Improved documentation of Settings feature

## v6.1.0

Deprecates the following methods:
- `MoneytreeLink.requestMagicLink`, use `MoneytreeLink.requestLoginLink` instead
- `MoneytreeLink.consumeMagicLink`, use `MoneytreeLink.consumeLoginLink` instead
- `MoneytreeLink.registerFcmToken`, use `MoneytreeLink.registerRemoteToken` instead
- `MoneytreeLink.unregisterFcmToken`, use `MoneytreeLink.unregisterRemoteToken` instead

## v6.0.1

- Fixes and stability improvements in the LINK Kit integration

## v6.0.0

- all deprecated functions from 5.x have been removed.
  - `onActivityResult` functions have all been removed.
  - `LinkAuthOptions` replaces `MoneytreeAuthOptions` and ensures greater compile-time safety.
  - `ClientAccessToken` replaces `OAuthToken` in PKCE configurations.
  - `MoneytreeAuthCompletion` has been removed.
  - `WebActionListener` has been removed.
  - `OAuthCredentialProvider` has been removed.
  - `OAuthHandler` has been removed.
  - `Api` listener had been removed and is replaced by the `Action` listener.
- Requires Android 6+

### MoneytreeLink
- `MoneytreeLink.init` now takes a `LinkResultListener` so you can listen to SDK event's
    for the lifetime of your app.
- `LinkRequestContext` no longer takes an `Activity` or `Fragment`
- `openVault` must now be pass the `Activity` explicitly.
- `consumeMagicLink` now uses the action callback; the callback is optional and may not
    always be called in the event Android OS kills the app;

    We **_Recommend_** using the `LinkResultListener` is your `Application`, `Activity` or
    `Fragment` `#onCreate` to ensure you get the result if the application is killed while background-ed and it's critical for your use case.
- `MoneytreeLinkException.Error` has been renamed `LinkError`
- `setLogoutHandler` has been removed and is replaced by `LinkResult.Event(LoggedOut)` in `LinkResultListener`
- `authorizeFrom` was renamed `authorize`
- `authorize` now takes the `LinkAuthOptions.Authorize` sub-type.
- `onboardFrom` was renamed `onboard`
- `onboard` now takes the `LinkAuthOptions.Onboarding` sub-type.
- `logoutFrom` was renamed `logout` as no longer takes a requestCode.
- `openSettingsFrom` was renamed `openSettings`
- `registerDeviceTokenFrom` was renamed to `registerFcmToken` & the callback type was updated to `Action` listener.
- `unregisterDeviceTokenFrom` was renamed to `unregisterFcmToken` & the callback type was updated to `Action` listener.
- `getTokenInfo` can be used to get the OAuth token info if `stayLoggedIn` is enabled.
- `getToken` no longer takes a callback and instead you need to use the `LinkResultListener` via `MoneytreeLink.addOnResultListener`
- `updateConfiguration` was added.
  - it's not **_recommended_** that you use `updateConfiguration` in the 6.0 release as the implementation is not final and is subject to change. Hence it is marked as `@deprecated`.
Once the implementation is finalized we will remove the deprecation flag.


### LinkResultListener
The LinkResultListener replaces most of the callbacks in the SDK. LinkResultListener is able to survive the OS killing the app when opening Vault or authorizing users. `LinkResultListener` also removes the need for the `onActivityResult`. `LinkResultListener` means that you never miss success or error results from the SDK.

We recommend that you put the `addOnLinkResult` in your `Activity` or `Fragment`s `onCreate` to ensure that your callbacks are re-attached to the SDK when the application is being re-created.

### LinkResult

LinkResult is the new unified event result class. Authorization, Events and Errors are all reported using a LinkResult type.

* `Authorized` - Is emitted when the SDK finished an authorization flow.
    - In PKCE configured applications the `Authorized` the `token` property will not be null.
    - In CodeGrant configured the `Authorized` the `token` property will always be null.
    - `MoneytreeLinkExtensions` extension function `onPkceAuthorized` and `onCodeGrantAuthorized` handle the null check
* `Event` - Is emitted when the SDK triggers a noticeable event.
    - `ExternalOAuthAdded`: An OAuth account was added when using a browser outside of the App.
    - `LinkWebSessionStarted`: An In-App-Browser is about open and the app is about to be background-ed.
    - `LinkWebSessionFinished`: An In-App-Browser has closed and the app has returned to the foreground.
    - `LoggedOut`: Logout flow finished successfully.
    - `RequestCancelled`:The user has closed the In-App-Browser without any expected callbacks been triggered.
    - `VaultOpened`: The Vault is about to be opened.
    - `VaultClosed`: The Vault was opened and is now closed retuning the app to the foreground
* `Error` - An exception occurred or an error was returned to the SDK and this resulted in an `MoneytreeLinkException`


### MoneytreeLinkExtensions

Theses are Kotlin extensions that make consuming LinkResults easier. They also work in Java.

- `onLoggedOut` - will register a lifecycleOwner scoped listener for loggedOut events
- `onPkceAuthorized` - will register a lifecycleOwner scoped listener for PKCE authorizations events.
- `onCodeGrantAuthorized` - will register a lifecycleOwner scoped listener for CodeGrant authorizations events.
- `onError` - will register a lifecycleOwner scoped listener for any SDK Error's.
- `onEvent` - will register a lifecycleOwner scoped listener for any SDK Events.

### v5.3.1

- Deprecates all Vault navigation interface methods
- Adds `openVault(requestCode, requestContext)` and `openVault(requestContext)` variant to replace previous navigation methods.
    - The `requestCode` is an Android activity result code. Checking for it in your `onActivityResult` callback along with `MoneytreeLink.RESULT_CODE_VAULT_CLOSED` will provide you with information of when the Vault Browser has closed. Supports both Activities and Fragments.
    - Adds `LinkRequestContext`. Use its builder to provide all required information for vault navigation.

### v5.3.0

- Improvements to the Vault navigation interface methods added in [v5.2.0](#5.2.0)
- Adds `MoneytreeLinkConfiguration.Builder.linkEnvironment()`. Takes a `LinkEnvironment` to switch between the staging and production back ends.
- Adds new scopes
- Adds new Exception types and a new `Action.OnErrorListener` for better error management.

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

### v5.1.0

- `MoneytreeLink#client()` is now deprecated. Use `MoneytreeLink#getInstance()` instead.

### v5.0.0

- Minimum supported Android is now Android 5+ (API level 21+)
- Removed deprecated methods at `MoneytreeLink` class
- A `listener` for `openVaultFrom` has been changed to `Action.OnCompletionListener` from `Authorization.OnCompletionListener`. You need to call `getToken()` whenever you want an `accessToken`.
- Updated versions of dependencies. See also [Set up dependencies and manifest](#set-up-dependencies-and-manifest) section above.

### v4.1.1

The timing of callbacks originating from `openVault` and `openSettings` has been changed. In previous versions, callbacks returned when the Browser opened. Since v4.1.1 they return when users close the Browser (UI button or system back button).

### v4.1.0

We introduced `MoneytreeAuthOptions` class that replaces the array of variables previously used in `authorizeFrom` method. You can refactor your existing code by following the examples above. As described in JavaDoc, existing contracts of `authorizeFrom` will be removed in the next version.

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

### v3

MoneytreeLink SDK v3 brings some breaking changes to class names.
The following is just an example. Refer to the JavaDocs and Awesome app's MainActivity for more information.

| Old Class Name                     | New Class Name                     | Note                                   |
| ---------------------------------- | ---------------------------------- | -------------------------------------- |
| CompletionHandler                  | Authorization.OnCompletionListener | Methods for authorization              |
| (same as above)                    | Action.OnCompletionListener        | Used for openSettings, openInstitution |
| MoneytreeLink.ApiCompletionHandler | Api.OnCompletionListener           | Renamed                                |
| IsshoTsucho.CompletionHandler      | IsshoTsucho.OnCompletionListener   | Renamed                                |
s
