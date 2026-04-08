plugins {
    id("reconnect.android.library")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "dev.pranav.reconnect.core.model"
}

dependencies {
    implementation(libs.kotlinx.serialization.json)
}
