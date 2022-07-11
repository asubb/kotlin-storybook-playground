React Storybook for KotlinJS: Basic set up
===========

Prerequisites
------

The project with the components. For the sake of providing example, we'll create a project with some simple components.

Setting up the projects
----- 

Usually the stories are being kept as a separate project, the same we'll do here, the `app` project will contain all components while the stories will be contained under named the same project `stories`. So general structure of the prject would look like this:

```text
|-app/
|--src/
|--build.gradle.kts
|-stories/
|--src/
|--build.gradle.kts
|-build.gradle.kts
|-setting.gradle.kts
```

The root `build.gradle.kts` is setting up the KotlinJS project, we'll use Kotlin 1.7.0 available at the time of writing:

```kotlin
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
```

The `settings.gradle.kts` will include both our projects:
```kotlin
include(":app")
include(":stories")
```

The `app` and `stories` projects will remain empty for now, so just create empty `build.gradle.kts` files in both of them.