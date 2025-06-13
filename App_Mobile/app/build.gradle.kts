plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.gpsapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.gpsapp"
        minSdk = 23
        targetSdk = 34
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
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation ("com.google.android.libraries.identity.googleid:googleid:1.0.2")

    // Firebase BOM (chỉ giữ một lần)
    implementation(platform("com.google.firebase:firebase-bom:33.9.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-database")

    // Chỉ dùng credentials-play-services-auth (không cần play-services-auth)
    implementation("androidx.credentials:credentials:1.3.0")
    implementation("com.google.android.gms:play-services-auth:20.7.0")

    // Google Play Services và Maps API
    implementation("com.google.android.libraries.identity.googleid:googleid:1.0.2")
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    implementation("com.google.android.libraries.places:places:3.3.0")
    implementation("com.google.maps.android:android-maps-utils:2.3.0")
    implementation("com.google.android.gms:play-services-location:21.0.1")
    implementation("com.android.volley:volley:1.2.1")


    implementation ("com.github.bumptech.glide:glide:4.12.0")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.12.0")

    implementation ("androidx.appcompat:appcompat:1.6.1")

    implementation ("com.squareup.okhttp3:okhttp:4.12.0")
    implementation ("org.java-websocket:Java-WebSocket:1.5.3")

    implementation ("androidx.core:core:1.12.0")
    implementation ("com.google.firebase:firebase-messaging:23.4.1")

    implementation ("com.journeyapps:zxing-android-embedded:4.3.0")

}
