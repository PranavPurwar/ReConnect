import java.io.FileInputStream

plugins {
    id("reconnect.android.library")
    alias(libs.plugins.kotlin.serialization)
}

import java . util . Properties
        import java . io . FileInputStream

android {
    namespace = "dev.pranav.reconnect.data.supabase"

    buildFeatures {
        buildConfig = true
    }

    val localProperties = Properties()
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        FileInputStream(localPropertiesFile).use { localProperties.load(it) }
    }

    val supabaseUrl = localProperties.getProperty("SUPABASE_URL") ?: System.getenv("SUPABASE_URL")
    ?: project.findProperty("SUPABASE_URL")?.toString() ?: ""
    val supabaseKey = localProperties.getProperty("SUPABASE_KEY") ?: System.getenv("SUPABASE_KEY")
    ?: project.findProperty("SUPABASE_KEY")?.toString() ?: ""

    defaultConfig {
        buildConfigField("String", "SUPABASE_URL", "\"$supabaseUrl\"")
        buildConfigField("String", "SUPABASE_KEY", "\"$supabaseKey\"")
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
