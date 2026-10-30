import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.dudlatitat.nextchapter"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.dudlatitat.nextchapter"
        minSdk = 26
        targetSdk = 35
        // Each CI build gets a higher number, so the phone accepts it as an update.
        val build = System.getenv("GITHUB_RUN_NUMBER")?.toIntOrNull() ?: 0
        versionCode = 100 + build
        versionName = "1.$build"
        resourceConfigurations += listOf("en", "zh-rCN")
    }

    // One lasting key, so each new build installs over the old one and keeps your progress.
    signingConfigs {
        create("shared") {
            storeFile = file(System.getenv("NC_KEYSTORE") ?: "../signing/next-chapter.jks")
            storePassword = System.getenv("NC_KEY_PASSWORD") ?: "Mqi7FZ2cIA8B548erFqF5v"
            keyAlias = "next-chapter"
            keyPassword = System.getenv("NC_KEY_PASSWORD") ?: "Mqi7FZ2cIA8B548erFqF5v"
        }
    }

    buildTypes {
        debug { signingConfig = signingConfigs.getByName("shared") }
        release {
            signingConfig = signingConfigs.getByName("shared")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures { compose = true }
    lint {
        disable += setOf("GradleDependency", "AndroidGradlePluginVersion", "NewerVersionAvailable", "MissingTranslation")
        abortOnError = false
        textReport = true
    }
}

kotlin { compilerOptions { jvmTarget.set(JvmTarget.JVM_17) } }

dependencies {
    implementation(libs.androidx.profileinstaller)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
