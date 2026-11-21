plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
}
if (file("google-services.json").exists()) apply(plugin = "com.google.gms.google-services")
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
    implementation("androidx.credentials:credentials:1.3.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.3.0")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.10.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.10.0")
    implementation("androidx.compose.material:material-icons-core:1.7.8")
    implementation(platform("com.google.firebase:firebase-bom:34.7.0"))
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
