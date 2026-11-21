plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
}
android {
    namespace = "cl.comunidad.conectada"
    compileSdk = 36
    defaultConfig {
        applicationId = "cl.comunidad.conectada"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0"
    }
    buildFeatures { compose = true }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
}
dependencies {
    implementation(platform("com.google.firebase:firebase-bom:35.2.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-database")
    implementation("com.google.firebase:firebase-messaging")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.10.2")
    implementation("org.eclipse.paho:org.eclipse.paho.client.mqttv3:1.2.5")
    implementation("androidx.activity:activity-compose:1.13.0")
    implementation("androidx.compose.material3:material3:1.4.0")
    implementation("androidx.compose.ui:ui:1.10.0")
    implementation("androidx.compose.ui:ui-tooling-preview:1.10.0")
    debugImplementation("androidx.compose.ui:ui-tooling:1.10.0")
    testImplementation("junit:junit:4.13.2")
}
