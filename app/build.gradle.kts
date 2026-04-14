import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(FileInputStream(localPropertiesFile))
}

android {
    namespace = "com.example.spotifystats"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.spotifystats"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // 4. This reads the file safely now
        val clientId = localProperties.getProperty("SPOTIFY_CLIENT_ID") ?: ""
        buildConfigField("String", "SPOTIFY_CLIENT_ID", "\"$clientId\"")

        val clientSecret = localProperties.getProperty("SPOTIFY_CLIENT_SECRET") ?: ""
        buildConfigField("String", "SPOTIFY_CLIENT_SECRET", "\"$clientSecret\"")

        val lastFmKey = localProperties.getProperty("lastFmApiKey") ?:"  "
        buildConfigField("String","lastFmApiKey","\"$lastFmKey\"")

        manifestPlaceholders += mapOf(
            "redirectSchemeName" to "dakshstats",
            "redirectHostName" to "callback"
        )
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
        buildConfig = true
    }
}

dependencies {

    //Spotify auth librarie
    implementation("com.spotify.android:auth:2.1.1")

    //Navigation Libraries
    val nav_version = "2.7.7"
    implementation("androidx.navigation:navigation-compose:$nav_version")

    // Retrofit Libraries
    implementation("com.squareup.retrofit2:retrofit:3.0.0")
    implementation("com.squareup.retrofit2:converter-gson:3.0.0")

    // to load images
    implementation("io.coil-kt:coil-compose:2.7.0")

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}