plugins {
    id("reconnect.android.library")
}

android {
    namespace = "dev.pranav.reconnect.core.session"
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:storage"))
    implementation(libs.androidx.core.ktx)
}
