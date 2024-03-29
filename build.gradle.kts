// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
  apply(from = "mtlink_version.gradle.kts")

  repositories {
    google()
    mavenCentral()
  }

  dependencies {
    classpath("com.android.tools.build:gradle:7.4.0")
    classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.6.21")
  }
}

allprojects {
  repositories {
    google()
    mavenCentral()
  }
}

tasks.register("clean", Delete::class) {
  delete(rootProject.buildDir)
}
