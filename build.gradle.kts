// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
  apply(from = "mtlink_version.gradle.kts")

  repositories {
    google()
  }

  dependencies {
    classpath("com.android.tools.build:gradle:7.1.2")
    classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.4.31")
  }
}

allprojects {
  repositories {
    google()
  }
}

tasks.register("clean", Delete::class) {
  delete(rootProject.buildDir)
}
