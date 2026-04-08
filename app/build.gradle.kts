plugins {
    id("reconnect.android.application")
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "dev.pranav.reconnect"

    defaultConfig {
        applicationId = "dev.pranav.reconnect"
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    flavorDimensions += "backend"
    productFlavors {
        create("privateLocal") {
            dimension = "backend"
            buildConfigField("boolean", "ENABLE_LOGIN_GATE", "false")
        }
        create("playstoreSupabase") {
            dimension = "backend"
            buildConfigField("boolean", "ENABLE_LOGIN_GATE", "true")
        }
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
    buildFeatures {
        compose = true
        buildConfig = true
    }

}

composeCompiler {
    stabilityConfigurationFiles.add(rootProject.layout.projectDirectory.file("app/stability_config.conf"))
}

dependencies {
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.ui)

    implementation(project(":core:model"))
    implementation(project(":core:storage"))
    implementation(project(":core:session"))

    "privateLocalImplementation"(project(":data:local"))
    "playstoreSupabaseImplementation"(project(":data:supabase"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material3.adaptive.navigation.suite)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.compose.ui.text.google.fonts)
    implementation(libs.androidx.palette.ktx)

    implementation(libs.kotlinx.serialization.json)

    implementation(libs.ktor.cio)

    implementation(libs.sketch.compose)
    implementation(libs.sketch.compose.resources)
    implementation(libs.sketch.extensions.compose)
    implementation(libs.sketch.extensions.compose.resources)
    implementation(libs.sketch.http)
    implementation(libs.sketch.video)
    implementation(libs.sketch.zoom)


    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}
