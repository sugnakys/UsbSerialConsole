plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.kapt")
    id("androidx.navigation.safeargs.kotlin")
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
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
    val kotlinVersion = project.properties["kotlin"] as String
    val navigationVersion = project.properties["navigation"] as String
    val hiltVersion = project.properties["hilt"] as String
    val roomVersion = project.properties["room"] as String
    val pagingVersion = project.properties["paging"] as String

    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    testImplementation("junit:junit:4.13.2")
    testImplementation("io.mockk:mockk:1.14.4")
    testImplementation("com.google.truth:truth:1.4.4")

    implementation("com.google.android.material:material:1.12.0")

    // Android KTX
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("androidx.core:core-ktx:1.16.0")
    implementation("androidx.fragment:fragment-ktx:1.8.8")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.9.1")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.9.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.9.1")
    implementation("androidx.navigation:navigation-runtime-ktx:${navigationVersion}")
    implementation("androidx.navigation:navigation-fragment-ktx:${navigationVersion}")
    implementation("androidx.navigation:navigation-ui-ktx:${navigationVersion}")
    implementation("androidx.preference:preference-ktx:1.2.1")

    // room
    implementation("androidx.room:room-runtime:${roomVersion}")
    ksp("androidx.room:room-compiler:${roomVersion}")
    implementation("androidx.room:room-ktx:${roomVersion}")
    testImplementation("androidx.room:room-testing:${roomVersion}")
    implementation("androidx.room:room-paging:${pagingVersion}")

    // UsbSerial
    implementation("com.github.felHR85:UsbSerial:6.1.0")

    implementation("com.jaredrummler:colorpicker:1.1.0")

    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:${kotlinVersion}")

    // Hilt
    implementation("com.google.dagger:hilt-android:${hiltVersion}")
    ksp("com.google.dagger:hilt-android-compiler:${hiltVersion}")
    ksp("androidx.hilt:hilt-compiler:1.2.0")

    // Timber
    implementation("com.jakewharton.timber:timber:5.0.1")

    // LeakCanary
    debugImplementation("com.squareup.leakcanary:leakcanary-android:2.14")

    implementation("androidx.multidex:multidex:2.0.1")
}
