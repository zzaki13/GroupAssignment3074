plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    // Room needs kapt for codegen
    id("kotlin-kapt")
}

android {
    namespace = "ca.gbc.restaurantguide"
    compileSdk = 36

    defaultConfig {
        applicationId = "ca.gbc.restaurantguide"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // Enable ViewBinding (required for ActivityMainBinding, etc.)
    buildFeatures {
        viewBinding = true
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
    // Java 11 is fine for these libs; keep as-is unless your project uses 17
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    // Existing template deps (from your version catalog)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)                 // Material 3 widgets
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // --- Add these (not in your catalog by default) ---
    // RecyclerView
    implementation("androidx.recyclerview:recyclerview:1.3.2")

    // Lifecycle + LiveData + ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.6")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.6")

    // Room (runtime + Kotlin extensions + compiler via kapt)
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
