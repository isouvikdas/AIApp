// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
    repositories {
        // Make sure that you have the following two repositories
        google()  // Google's Maven repository
        mavenCentral()  // Maven Central repository
        maven ("https://jitpack.io")
    }

    dependencies {
        // Add the Maven coordinates and latest version of the plugin
        classpath ("com.google.gms:google-services:4.4.0")
        classpath("com.android.tools.build:gradle:8.1.4")
        classpath( "org.jetbrains.kotlin:kotlin-gradle-plugin:1.5.21")
    }
}
plugins {
    id("com.android.application") version "8.2.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.10" apply false
    id("com.android.library") version "8.1.2" apply false
}