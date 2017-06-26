## AwesomeApp

The `AwesomeApp` is a reference app created to showcase the SDK's capabilities. It is there to help you understand some of the SDK's base use cases. Feel free to edit it as you please.

### SDK configuration in the AwesomeApp

The `AwesomeApp` by default gets its `authType` and `isProduction` values from the build generated `BuildConfig`. If you want to change them, you can edit the root `gradle.properties` and build the project again. Alternatively you can edit the source code as you see fit.

To switch between `PKCE` and `Code Grant` auth flows, you can edit the following:

```properties
awesome.authType=com.example.myawesomeapp.AuthType.PKCE
```
or

```properties
awesome.authType=com.example.myawesomeapp.AuthType.CODE_GRANT
```

> :information_source: This is **NOT** part of the SDK's spec, just done so in the AwesomeApp. You can implement the configuration of the SDK in any way you prefer.

Finally you need to find and replace int the code the `[clientId]` with the Client ID you received, as well as `[clientIdShort]` with the first 5 characters of you Client ID.

You should now be able to build and use Awesome App.
