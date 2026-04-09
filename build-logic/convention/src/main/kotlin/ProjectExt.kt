import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project

@Suppress("UnusedReceiverParameter")
internal fun Project.configureKotlinAndroid(
    commonExtension: Any,
) {
    if (commonExtension is ApplicationExtension) {
        commonExtension.apply {
            compileSdk = 37
            defaultConfig {
                minSdk = 26
            }
            compileOptions {
                sourceCompatibility = JavaVersion.VERSION_17
                targetCompatibility = JavaVersion.VERSION_17
            }
        }
    } else if (commonExtension is LibraryExtension) {
        commonExtension.apply {
            compileSdk = 37
            defaultConfig {
                minSdk = 26
            }
            compileOptions {
                sourceCompatibility = JavaVersion.VERSION_17
                targetCompatibility = JavaVersion.VERSION_17
            }
        }
    }
}
