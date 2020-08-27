plugins {
  id("com.android.application")
  kotlin("android")
}

repositories {
  maven(url = "https://dl.bintray.com/moneytree/app.moneytree")
}

android {
  compileSdkVersion(29)
  buildToolsVersion("29.0.3")

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
  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.3.72")

  implementation("androidx.appcompat:appcompat:1.2.0")
  implementation("com.google.android.material:material:1.2.0")

  implementation("com.google.firebase:firebase-messaging:20.2.4")

  val sdkVersion = rootProject.extra["sdkVersion"] as String

  // Moneytree LINK SDK
  implementation("app.moneytree.link:core:${sdkVersion}")
  // Issho tsucho (Optional)
  implementation("app.moneytree.link:it:${sdkVersion}")
  // Moneytree Intelligence (Optional)
  implementation("app.moneytree.link:intelligence:${sdkVersion}")
}

apply(plugin = "com.google.gms.google-services")
