plugins {
    id("reconnect.android.library")
}

android {
    namespace = "dev.pranav.reconnect.core.storage"
}

dependencies {
    implementation(project(":core:model"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
}
