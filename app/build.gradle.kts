plugins {
  id("com.android.application")
  kotlin("android")
}

repositories {
  maven(url = "https://dl.bintray.com/moneytree/app.moneytree")
}

android {
  compileSdkVersion(29)
  buildToolsVersion("30.0.2")

  defaultConfig {
    applicationId = "com.example.myawesomeapp"
    minSdkVersion(21)
    targetSdkVersion(29)
    versionCode = 1
    versionName = "1.0"

    buildConfigField(
      "com.example.myawesomeapp.AuthType",
      "authType",
      properties["awesome.authType"] as String
    )

    buildConfigField(
      "Boolean",
      "isProduction",
      properties["awesome.isProduction"] as String)

      val environment =
        if ((properties["awesome.isProduction"] as String).toBoolean()) ""
        else "-staging"

      manifestPlaceholders += mapOf(
        // Staging: myaccount-staging.getmoneytree.com
        // Production: myaccount.getmoneytree.com
        "linkHost" to "myaccount$environment.getmoneytree.com",
        // clientIdShort: "<first 5 chars of your client ID>"
        "clientIdShort" to "[clientIdShort]"
      )
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
  }

  buildTypes {
    getByName("release") {
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
    }
  }
}

dependencies {
  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.4.10")

  implementation("androidx.appcompat:appcompat:1.2.0")
  implementation("com.google.android.material:material:1.2.0")

  implementation("com.google.firebase:firebase-messaging:20.2.4")

  val sdkVersion = rootProject.extra["sdkVersion"] as String

  // Moneytree LINK SDK
  implementation("app.moneytree.link:core:${sdkVersion}")
  // Issho tsucho (Optional)
  implementation("app.moneytree.link:link-kit:${sdkVersion}")
  // Moneytree Intelligence (Optional)
  implementation("app.moneytree.link:intelligence:${sdkVersion}")
}

apply(plugin = "com.google.gms.google-services")
