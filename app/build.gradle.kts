plugins {
    kotlin("js")
}

kotlin {
    js(IR) {
        browser { }
        binaries.executable()
    }
}