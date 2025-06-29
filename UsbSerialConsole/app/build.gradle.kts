plugins {
    id(libs.plugins.android.application.get().pluginId)
    id(libs.plugins.kotlin.android.get().pluginId)
    id(libs.plugins.kotlin.kapt.get().pluginId)
    id(libs.plugins.navigation.safeargs.kotlin.get().pluginId)
    id(libs.plugins.hilt.android.get().pluginId)
    id(libs.plugins.google.devtools.ksp.get().pluginId)
}

android {
    namespace = "jp.sugnakys.usbserialconsole"
    compileSdk = 36

    defaultConfig {
        applicationId = "jp.sugnakys.usbserialconsole"
        minSdk = 21
        targetSdk = 36
        versionCode = 20000
        versionName = "2.0.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        multiDexEnabled = true
    }
    buildTypes {
        release {
            isMinifyEnabled = true
            isDebuggable = false
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
        debug {
            isMinifyEnabled = false
            isDebuggable = true
            isShrinkResources = false
        }
    }
    buildFeatures {
        dataBinding = true
        buildConfig = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    kotlin {
        jvmToolchain(21)
    }
    lint {
        disable += "GoogleAppIndexingWarning"
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.truth)

    implementation(libs.material)

    // Android KTX
    implementation(libs.appcompat)
    implementation(libs.core.ktx)
    implementation(libs.fragment.ktx)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.navigation.runtime.ktx)
    implementation(libs.navigation.fragment.ktx)
    implementation(libs.navigation.ui.ktx)
    implementation(libs.preference.ktx)

    // room
    implementation(libs.room.runtime)
    ksp(libs.room.compiler)
    implementation(libs.room.ktx)
    testImplementation(libs.room.testing)
    implementation(libs.room.paging)

    // UsbSerial
    implementation(libs.usbserial)

    implementation(libs.colorpicker)

    implementation(libs.kotlin.stdlib.jdk7)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)
    ksp(libs.hilt.compiler)

    // Timber
    implementation(libs.timber)

    // LeakCanary
    debugImplementation(libs.leakcanary)

    implementation(libs.multidex)
}