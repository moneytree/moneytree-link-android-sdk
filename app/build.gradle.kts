plugins {
  id("com.android.application")
  kotlin("android")
}

apply(from = "../mtlink_version.gradle.kts")

val linkEnvironment: String by project
val linkClientId: String by project

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

    buildConfigField("Boolean", "isProduction", "${linkEnvironment == "production"}")
    buildConfigField("String", "clientId", "\"${linkClientId}\"")

    val myaccount =
      when(linkEnvironment) {
        "production" -> "myaccount"
        else -> "myaccount-staging"
      }

    manifestPlaceholders += mapOf(
      "linkHost" to "$myaccount.getmoneytree.com",
      "clientIdShort" to linkClientId.substring(0,5)
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
  implementation("com.google.android.material:material:1.7.0")

  // Moneytree LINK SDK
  implementation("app.moneytree.link:core:$sdkVersion")
  // LINK Kit (Optional)
  implementation("app.moneytree.link:link-kit:$sdkVersion")
}
