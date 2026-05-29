plugins {
    id("reconnect.android.library")
    alias(libs.plugins.ksp)
}

android {
    namespace = "dev.pranav.reconnect.data.local"
}

dependencies {
    implementation(projects.core.model)
    api(projects.core.storage)
    implementation(projects.videoCompressor)


    api(libs.androidx.room.runtime)
    api(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    implementation(libs.kotlinx.serialization.json)
}
