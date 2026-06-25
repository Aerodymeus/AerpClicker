plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.services)
}

    val javaVersionStr = libs.versions.javaVersion.get()
    val javaVersion = JavaVersion.toVersion(javaVersionStr)

    extensions.configure<com.android.build.api.dsl.ApplicationExtension> {
        namespace = "dev.aerodymeus.aerpclicker"
        compileSdk = libs.versions.compileSdk.get().toInt()

        defaultConfig {
            applicationId = "dev.aerodymeus.aerpclicker"
            minSdk = libs.versions.minSdk.get().toInt()
            targetSdk = libs.versions.targetSdk.get().toInt()
            versionCode = libs.versions.versionCode.get().toInt()
            versionName = libs.versions.versionName.get()

            testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }

        buildTypes {
            release {
                isMinifyEnabled = true
                isShrinkResources = true
                proguardFiles(
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                    "proguard-rules.pro"
                )
                ndk {
                    debugSymbolLevel = "FULL"
                }
            }
        }

        compileOptions {
            sourceCompatibility = javaVersion
            targetCompatibility = javaVersion
        }

        buildFeatures {
            compose = true
            buildConfig = true
        }

        packaging {
            resources {
                excludes += "/META-INF/{AL2.0,LGPL2.1}"
            }
        }
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.fromTarget(javaVersionStr))
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
    implementation(libs.androidx.material.icons.core)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.appcompat)
    implementation(libs.kotlinx.coroutines.core)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
