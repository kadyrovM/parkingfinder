plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.parkingfinder"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.example.parkingfinder"
        minSdk = 24
        targetSdk = 37
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
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    // Google Maps
    implementation("com.google.android.gms:play-services-maps:19.0.0")
    // Places API
    implementation("com.google.android.libraries.places:places:3.5.0")
    // Геолокация
    implementation("com.google.android.gms:play-services-location:21.3.0")
    // Список парковок
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}