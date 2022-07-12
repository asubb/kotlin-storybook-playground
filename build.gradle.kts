plugins {
    kotlin("js") version "1.7.10"
}

kotlin {
    js { browser { } }
}

version = "0.0.1"

allprojects {
    repositories {
        mavenCentral()
        maven { url = uri("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/kotlin-js-wrappers") }
    }
}

subprojects {

    apply(plugin = "org.jetbrains.kotlin.js")

    val kotlinWrappersVersion = "0.0.1-pre.323-kotlin-1.6.10"
    fun kotlinw(target: String): String = "org.jetbrains.kotlin-wrappers:kotlin-$target"

    // common dependencies to resolve by the projects
    dependencies {
        // react dependencies for Kotlin/JS
        implementation(enforcedPlatform(kotlinw("wrappers-bom:$kotlinWrappersVersion")))
        implementation(kotlinw("emotion"))
        implementation(kotlinw("react"))
        implementation(kotlinw("react-core"))
        implementation(kotlinw("react-dom"))
        implementation(kotlinw("react-router-dom"))
    }

}