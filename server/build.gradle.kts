plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktor)
    application
}

application {
    mainClass.set("io.ktor.server.netty.EngineMain")
}

dependencies {
    // This is all that's needed now. The fix in shared-contract will make it work.
    implementation(project(":shared-contract"))

    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.logback.classic)
}
