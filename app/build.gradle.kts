plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}
android {
    namespace = "com.mrsiktyyy.textdark"
    compileSdk = 36
    defaultConfig {
        applicationId = "com.mrsiktyyy.textdark"
        minSdk = 26
        targetSdk = 36          // работает на Android 17; поднимите до 37, когда он появится в SDK Manager
        versionCode = 1
        versionName = "3.3-mobile"
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("debug") // для личной установки; свой ключ см. README
        }
    }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
    kotlinOptions { jvmTarget = "17" }
}
dependencies {
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.webkit:webkit:1.12.1")
}
