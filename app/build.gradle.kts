plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.myfirstgame"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.myfirstgame"
        minSdk = 30
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation("androidx.core:core-ktx:1.12.0") // Oder neuere Version
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0") // Oder neuere Version
    implementation("androidx.activity:activity-compose:1.8.2") // Oder neuere Version
    implementation(platform("androidx.compose:compose-bom:2024.02.02")) // Oder neuere Version

        // ... other dependencies
        implementation("androidx.compose.ui:ui:1.6.0") // Use the latest stable version
        implementation("androidx.compose.material3:material3:1.2.0") // Or material if you are using Material 2
        implementation("androidx.compose.ui:ui-tooling-preview:1.6.0")
        implementation("androidx.activity:activity-compose:1.8.0")
        implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
}