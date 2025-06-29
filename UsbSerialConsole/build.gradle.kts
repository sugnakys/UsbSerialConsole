// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
    val kotlinVersion = project.properties["kotlin"] as String
    val navigationVersion = project.properties["navigation"] as String
    val hiltVersion = project.properties["hilt"] as String

    repositories {
        mavenCentral()
        google()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.11.0")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${kotlinVersion}")
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:${navigationVersion}")
        classpath("com.google.dagger:hilt-android-gradle-plugin:${hiltVersion}")

        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
    }
}

plugins {
    id("com.cookpad.android.plugin.license-tools") version "1.2.8"
    id("com.google.devtools.ksp") version "2.2.0-2.0.2" apply false
}

allprojects {
    repositories {
        mavenCentral()
        google()
        maven { url = uri("https://jitpack.io") }
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}