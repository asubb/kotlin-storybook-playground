plugins {
    kotlin("js")
}

kotlin {
    js(IR) {
        browser { }
        binaries.executable()
    }
}

val kotlinWrappersVersion = "1.0.0-pre.354"
fun kotlinw(target: String): String = "org.jetbrains.kotlin-wrappers:kotlin-$target"

dependencies {
    implementation(enforcedPlatform(kotlinw("wrappers-bom:$kotlinWrappersVersion")))
    implementation(kotlinw("emotion"))
    implementation(kotlinw("react"))
    implementation(kotlinw("react-core"))
    implementation(kotlinw("react-dom"))
    implementation(kotlinw("react-router-dom"))
}