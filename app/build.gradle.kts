plugins {
  id("com.android.application")
  kotlin("android")
}

val awesomeAuthType: String by project
val awesomeIsProduction: String by project

android {
  compileSdkVersion(30)
  buildToolsVersion("30.0.3")

  defaultConfig {
    applicationId = "com.example.myawesomeapp"
    minSdkVersion(21)
    targetSdkVersion(30)
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
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
  }
}

val sdkVersion: String by project

dependencies {
  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.4.31")
  implementation("androidx.appcompat:appcompat:1.2.0")
  implementation("com.google.android.material:material:1.3.0")
  // Moneytree LINK SDK
  implementation("app.moneytree.link:core:$sdkVersion")
  // LINK Kit (Optional)
  implementation("app.moneytree.link:link-kit:$sdkVersion")
}
