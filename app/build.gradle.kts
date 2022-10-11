plugins {
  id("com.android.application")
  kotlin("android")
}

apply(from = "../mtlink_version.gradle.kts")

val awesomeAuthType: String by project
val awesomeIsProduction: String by project

android {
  compileSdk = 33

  buildFeatures {
    viewBinding = true
  }

  defaultConfig {
    applicationId = "com.example.myawesomeapp"
    minSdk = 23
    targetSdk = 33
    versionCode = 1
    versionName = "1.0"

    // TODO: replace with your client ID
    val clientId = "[clientId]"

    buildConfigField("com.example.myawesomeapp.AuthType", "authType", awesomeAuthType)
    buildConfigField("Boolean", "isProduction", awesomeIsProduction)
    buildConfigField("String", "clientId", "\"${clientId}\"")

    val myaccount =
    if (awesomeIsProduction.toBoolean()) "myaccount"
    else "myaccount-staging"

    manifestPlaceholders += mapOf(
      "linkHost" to "$myaccount.getmoneytree.com",
      "clientIdShort" to clientId.substring(0,5)
    )
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }

  java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(11))
  }
}

val sdkVersion: String by project

dependencies {
  implementation("androidx.appcompat:appcompat:1.5.1")
  implementation("com.google.android.material:material:1.6.1")
  // Moneytree LINK SDK
  implementation("app.moneytree.link:core:$sdkVersion")
  // LINK Kit (Optional)
  implementation("app.moneytree.link:link-kit:$sdkVersion")
}
