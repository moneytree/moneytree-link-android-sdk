plugins {
  id("com.android.application")
  kotlin("android")
}

apply(from = "../mtlink_version.gradle.kts")

val awesomeAuthType: String by project
val awesomeIsProduction: String by project

android {
  compileSdk = 31
  buildToolsVersion = "32.0.0"

  buildFeatures {
    viewBinding = true
  }

  defaultConfig {
    applicationId = "com.example.myawesomeapp"
    minSdk = 23
    targetSdk = 31
    versionCode = 1
    versionName = "1.0"

    buildConfigField("com.example.myawesomeapp.AuthType", "authType", awesomeAuthType)
    buildConfigField("Boolean", "isProduction", awesomeIsProduction)

    val myaccount =
    if (awesomeIsProduction.toBoolean()) "myaccount"
    else "myaccount-staging"

    manifestPlaceholders += mapOf(
      "linkHost" to "$myaccount.getmoneytree.com",
      // TODO: Set first 5 chars of your client ID
      "clientIdShort" to "[clientIdShort]"
    )
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
}

val sdkVersion: String by project

dependencies {
  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.6.10")
  implementation("androidx.appcompat:appcompat:1.4.1")
  implementation("com.google.android.material:material:1.5.0")
  // Moneytree LINK SDK
  implementation("app.moneytree.link:core:$sdkVersion")
  // LINK Kit (Optional)
  implementation("app.moneytree.link:link-kit:$sdkVersion")
}
