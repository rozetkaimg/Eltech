import java.text.SimpleDateFormat
import java.util.Date

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.rozetka.epolitech"
    compileSdk = 36
    val varMajor = 1
    val varMinor = 0
    val varPatch = 0
    val varBuild = 18
    fun getBuildDate(): String = SimpleDateFormat("ddMMyyyy").format(Date())
    defaultConfig {
        applicationId = "com.rozetka.eltech"
        minSdk = 26
        targetSdk = 36
        versionCode = (System.currentTimeMillis() / 60000).toInt()
        versionName = "$varMajor.$varMinor.$varPatch$varBuild${getBuildDate()}"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    dependenciesInfo {
        includeInApk = false
        includeInBundle = false
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
        }

        debug {
            applicationIdSuffix = ".debug"
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    kotlin {
        jvmToolchain(21)
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.glance.appwidget)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.glance.material3)
    implementation(libs.koin.androidx.compose)
    implementation(libs.koin.android)
    implementation(project(":network"))
    implementation(project(":storage"))
    implementation(project(":domain"))
    implementation(project(":data"))
    implementation(project(":model"))
    implementation("io.coil-kt:coil:2.7.0")
    implementation(project(":presentation"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.koin.core)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.koin.android)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material3.window.size.class1)
    implementation(libs.androidx.compose.foundation)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}