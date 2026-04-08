plugins {
    `kotlin-dsl`
}

group = "dev.pranav.reconnect.buildlogic"

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
    }
}

dependencies {
    compileOnly("com.android.tools.build:gradle:9.2.0-alpha07")
    compileOnly("org.jetbrains.kotlin:kotlin-gradle-plugin:2.3.20")
}

gradlePlugin {
    plugins {
        register("reconnectAndroidApplication") {
            id = "reconnect.android.application"
            implementationClass = "AndroidApplicationConventionPlugin"
        }
        register("reconnectAndroidLibrary") {
            id = "reconnect.android.library"
            implementationClass = "AndroidLibraryConventionPlugin"
        }
    }
}
