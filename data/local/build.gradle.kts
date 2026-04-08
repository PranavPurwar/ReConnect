plugins {
    id("reconnect.android.library")
    alias(libs.plugins.ksp)
}

android {
    namespace = "dev.pranav.reconnect.data.local"
}

dependencies {
    implementation(project(":core:model"))
    api(project(":core:storage"))

    api(libs.androidx.room.runtime)
    api(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    implementation(libs.kotlinx.serialization.json)
}
