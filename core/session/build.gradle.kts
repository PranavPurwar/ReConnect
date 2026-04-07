plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "dev.pranav.reconnect.core.session"
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
    implementation(libs.androidx.core.ktx)
}
