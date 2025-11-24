import java.util.Properties
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.android.library)
}

val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(localPropertiesFile.inputStream())
}
val myApiKey = localProperties.getProperty("apiKey") ?: ""

kotlin {
    jvm()
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    iosX64(); iosArm64(); iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            // FIX: Using direct string literal for Koin BOM (resolves the "Cannot convert notation" error)
            implementation(platform("io.insert-koin:koin-bom:4.0.0"))

            // Ktor dependencies (resolved via TOML accessors, managed by the BOM version)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.ktor.client.logging)

            // KotlinX
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.coroutines.core)

            // Koin (Core without version, version managed by BOM)
            implementation(libs.koin.core)
        }

        val androidMain by getting {
            dependencies {
                api(libs.generativeai)
                implementation(libs.ktor.client.okhttp)
            }
        }

        val jvmMain by getting {
            dependencies {
                implementation(libs.ktor.client.okhttp)
            }
        }

        val iosMain by creating {
            dependsOn(commonMain.get())
            dependencies {
                implementation(libs.ktor.client.darwin)
            }
        }

        val iosX64Main by getting { dependsOn(iosMain) }
        val iosArm64Main by getting { dependsOn(iosMain) }
        val iosSimulatorArm64Main by getting { dependsOn(iosMain) }
    }
}

android {
    namespace = "com.example.project.shared.contract"
    compileSdk = 34
    defaultConfig {
        minSdk = 24
        buildConfigField("String", "API_KEY", "\"$myApiKey\"")
    }
    buildFeatures { buildConfig = true }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}
