plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.joelromanpr.tinycompressor"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        minSdk = 26

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

}

dependencies {

    dependencies {
        implementation(libs.androidx.annotation)

        // Needed for EXIF preservation
        implementation(libs.androidx.exifinterface)
        implementation(libs.kotlinx.coroutines.core)

    }
}
