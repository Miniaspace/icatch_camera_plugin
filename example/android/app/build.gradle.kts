plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    // The Flutter Gradle Plugin must be applied after the Android and Kotlin Gradle plugins.
    id("dev.flutter.flutter-gradle-plugin")
}

android {
    namespace = "com.example.icatch_camera_plugin_example"
    compileSdk = flutter.compileSdkVersion
    ndkVersion = "27.0.12077973"
    
    // Remove flatDir; repositories configured at project level

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_11.toString()
    }

    defaultConfig {
        // TODO: Specify your own unique Application ID (https://developer.android.com/studio/build/application-id.html).
        applicationId = "com.example.icatch_camera_plugin_example"
        // You can update the following values to match your application needs.
        // For more information, see: https://flutter.dev/to/review-gradle-config.
        minSdk = 27
        targetSdk = flutter.targetSdkVersion
        versionCode = flutter.versionCode
        versionName = flutter.versionName
    }

    buildTypes {
        release {
            // TODO: Add your own signing config for the release build.
            // Signing with the debug keys for now, so `flutter run --release` works.
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    
    lint {
        checkReleaseBuilds = false
    }
}

dependencies {
    implementation("se.emilsjolander:stickylistheaders:2.7.0")
    // Force Material Components to 1.10.0 to avoid AAPT2 compile issues with percentage dimen in 1.11.0
    implementation("com.google.android.material:material:1.10.0")
    // Now use Maven coordinates for local artifacts
    implementation("com.icatch.local:pulltorefreshlibrary:1.0.0")
    implementation("com.icatch.local:status-bar-compat:0.7")
}

flutter {
    source = "../.."
}
