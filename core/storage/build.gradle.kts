plugins {
    id("reconnect.android.library")
}

android {
    namespace = "dev.pranav.reconnect.core.storage"
}

dependencies {
    implementation(project(":core:model"))
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
}
