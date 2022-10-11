## LINK SDK 4 and 5 to LINK SDK 6

## Integrating the SDK

Since 6.0 the SDK is available via Maven Central as an Android AAR maven package.
When installing SDK 6 you can omit the `@aar` suffix found in previous verions.

Starting from `app.moneytree.link:core` 6.x sibling modules like  `link-kit`, previously `it`, have independent versioning. Including all packages from SDK 6.x would now look like this.

```groovy
implementation("app.moneytree.link:core:6.4.0")
implementation("app.moneytree.link:link-kit:6.4.0")
```

> ⚠️ Please refer to the [release page](https://github.com/moneytree/moneytree-link-android-sdk/releases) for the latest version.

## SDK Requirements

Link SDK 6 or greater require Android 6+. So, you will need to update your `minSdk` to 23 or greater. Also, starting with 6.2.0, the library depends on `androidx.browser:browser:1.4+` and `androidx.core:core-ktx:1.7+`, as such, the `compileSdk` must be set to 31 and above.

```groovy
android {

  compileSdk = 31 // minimum requirement
  
  defaultConfig {
    targetSdk 31 // recommended, but it's required that targetSdk >= minSdk
    minSdk 23
  }
}
```

## AndroidX

SDK 6 also now requires AndroidX.

Please follow Google's migration guide for [AndroidX](https://developer.android.com/jetpack/androidx/migrate) if you have not already done so.

As per the Guide above enable the following flags in your root `gradle.properties` if you still have dependencies that are not on AndroidX.

```properties
android.useAndroidX=true
# The Android plugin uses the appropriate AndroidX library instead of a Support Library.
android.enableJetifier=true
```

`com.android.support` packages must be upgraded to AndroidX.

## Changes to Manifest

### Removing SchemeHandlerActivity

In SDK v4.x, it was necessary to include our `SchemeHandlerActivity` in your manifest to ensure that your application handled incoming links appropriately. This is now handled by the Moneytree LINK SDK. If you are upgrading from SDK v4.x, please check your `AndroidManifest.xml` for something similar to the declaration below and remove it, otherwise it will show warnings that this class cannot be found.

```
<activity android:name="com.getmoneytree.auth.SchemeHandlerActivity">
    <intent-filter>
        <action android:name="android.intent.action.VIEW"/>
        <category android:name="android.intent.category.DEFAULT"/>
        <category android:name="android.intent.category.BROWSABLE"/>
        <!-- FIXME: Replace with your value -->
        <data android:scheme="mtlinkxxxxx"/>
    </intent-filter>
</activity>
```

> :information: If you are adopting the Passwordless Signup and Login feature added in v6, you will need to add an intent filter on one of your own Activities. See the documentation on [Passwordless Signup and Login](..#configuring-passwordless-sign-uplogin--login-link).

## MoneytreeLink

`MoneytreeLink` has undergone a lot of changes in both 5.x and 6.0 release. `MoneytreeLink` can now survives process death. To achieve this change the way events are returned to the app has changed.

Most callbacks are now returned as event via the `OnLinkResult` listener. There are two places you can register for events.

### LinkResult

`Authorized`
 - in PKCE configs this will have a ClientAccessToken.
 - in CodeGrant auth config the token property will be null.

`Event`
- contains an event property that tells you when important events occur in the SDK, such as the Vault closing.

`Error`
- contains a `moneytreeLinkException` property for any errors that happen inside the SDK or that are returned by Moneytree or any 3rd party server.

### Init

`MoneytreeLink.init` now takes a callback that can listen to the `LinkResult` the SDK emits.

```java
  MoneytreeLink.init(this, configuration, (result) -> {
    // if (result instanceof LinkResult.[sub-type-here]) {
    //
    // }
  });
```

### addOnLinkResult

`addOnLinkResult(...)` is used to register listeners for SDK result events. We recommend registering any event listeners in you `Activity`'s or `Fragment`'s `onCreate`. You don't need to un-register the listener as the SDK uses LifecycleOwner to automatically execute an un-register step.

#### MoneytreeLinkExtensions

`MoneytreeLinkExtensions` is a set of pre-configured `LinkResultListener` wrappers. These utility functions such as `addOnLinkResult` should be used in `Activities` and `Fragments` in the `onCreate` function.

* `onLoggedOut` - Is invoked when the SDK finished a logout flow.
* `onAuthorized` - Is invoked when the SDK gets a new or updated `ClientAccessToken` when PKCE is used as an auth flow. For Code Grant, it is invoked when the SDK has finished linking the user with your server and the user is connected, in which case the returned token will be null.
* `onError` - Is invoked when any error occurs.
* `onEvent` - Is invoked when any event occurs.


`MoneytreeLinkExtensions` is a Kotlin first utility class with Java support.
An example of using `onAuthorized` would look like this in Kotlin and Java

```kotlin
linkClient.onAuthorized(this) { token ->
  // do something with token
}
```

```java
MoneytreeLinkExtensions.onAuthorized(linkClient, activity, token -> {
  // do something with token
});
```

### client()

`MoneytreeLink.client()` is replaced with `MoneytreeLink.getInstance()`


#### getToken

getToken no longer takes `Authorization.OnCompletionListener`, instead `getToken` takes an activity.

```java
MoneytreeLink.getInstance().getToken(
    new Authorization.OnCompletionListener() { }
  );
```

Becomes

```java
MoneytreeLink.getInstance().getToken(activity);
```

### getTokenInfo

`getTokenInfo` was added in 6.0 and _only_ contains metadata about the token, specifically, the `scopes` and `resourceServer`.

```java
 MoneytreeLink.getInstance().getTokenInfo(new OnTokenInfoCallback() {
  @Override
  public void onSuccess(TokenInfoResponse tokenInfoResponse) {

  }

  @Override
  public void onError(LinkError linkError) {

  }
})
```

> :warning: This function is only supported when the authentication type is PKCE.

### openVaultFrom

```java
MoneytreeLink.getInstance().openVault()
```

`openVaultFrom` has been renamed to `openVault`.

`openVault` no longer accepts the `Authorization.OnCompletionListener` listener. Events like the Vault closing or
errors are emitted via [OnLinkResult](..#sdk-callback-flow).

`openVault` now allows for deep-linking to specific vault pages
`VaultOpenServicesOptions` is an optional argument that contains the data needed to deep-link to a page. See [Opening the Vault](..#opening-the-vault) for more details.

### MoneytreeAuthOptions

`MoneytreeAuthOptions` is replaced by `LinkAuthOptions`.

`LinkAuthOptions` is split into two sub-types.
- `LinkAuthOptions.Authorize` - Auth is required by `MoneytreeLink#authorize`, with the user email as an optional argument, when building `LinkAuthOptions.Authorize`.
- `LinkAuthOptions.Onboarding` - Onboarding options are required by `MoneytreeLink#onboard` as onboarding requires an email address to create a password-less account for the user.

### openSettingsFrom

`openSettingsFrom` is renamed `openSettings`

`openSettings` now takes an optional email in the event the user is not logged in. The SDK will resume the `openSettings` flow once the user has logged in.

### registerDeviceTokenFrom

`registerDeviceTokenFrom` is
* renamed to `registerFcmToken`
* No longer takes an activity
* takes a new optional listener type `com.getmoneytree.listener.Action`

### unregisterDeviceTokenFrom

`unregisterDeviceTokenFrom` is
* renamed to `unregisterFcmToken`
* No longer takes an activity
* takes a new optional listener type `com.getmoneytree.listener.Action`

### setLogoutHandler

`setLogoutHandler` has been removed. To register for logout events using the `OnLinkResult`.

To listen for a logout event you can register a callback

```java
final MoneytreeLink linkClient = MoneytreeLink.getInstance();
MoneytreeLinkExtensions.onLoggedOut(MoneytreeLink, this, () -> {
    // Logout success
});
// OR
linkClient.addOnLinkResult(this, result -> {
    if (result instanceof LinkResult.Event && ((LinkResult.Event) result).getEvent() == LinkEvent.LoggedOut) {
        // Logout success
    }
});
```

You can also listen for Logout events when calling `init`.

```java
MoneytreeLink.init(this, configuration, (result) -> {
  if (result instanceof LinkResult.Event && ((LinkResult.Event) result).getEvent() == LinkEvent.LoggedOut) {
      // Logout success
  }
});
```

### logoutFrom

`logoutFrom` is renamed `logout`
