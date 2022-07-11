plugins {
    kotlin("js") version "1.7.0"
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