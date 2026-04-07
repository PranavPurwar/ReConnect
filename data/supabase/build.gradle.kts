plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "dev.pranav.reconnect.data.supabase"
    compileSdk = 37

    defaultConfig {
        minSdk = 26
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:storage"))

    api(platform(libs.supabase.bom))
    api(libs.supabase.auth)
    implementation(libs.supabase.postgrest)
    implementation(libs.supabase.realtime)
    implementation(libs.supabase.storage)
    implementation(libs.supabase.sketch)

    // Ktor
    implementation(libs.ktor.cio)

    implementation(libs.sketch.compose)
    implementation(libs.sketch.compose.resources)
    implementation(libs.sketch.http)
    implementation(libs.sketch.video)
    implementation(libs.sketch.zoom)
}
