React Storybook for Kotlin/JS: Basic set up
===========

Introduction
------

The Storybook is very helpful approach for developing UI applications that allows to test the component in isolation
which improves not only its testability but forces you to have a better design.

This tutorial attempts to find a way to use storybooks within Kotlin/JS applications. That is just a first try, so the
way provided here is not the best, but it works. There is a lot of room of improvement, but before we need to understand
where the touch points are, and what needs to be improved. All ideas and any feedback are very welcome.

Prerequisites
------

Before we start let's make sure we have enough to kick us up. From the Reader the followinf is expected:

* Understanding of the Kotlin/JS and React and Kotlin/JS interoperability, we'll cover the main points here but won't go too much deep.
* Vanilla JS knowledge will be very helpful as well as previous experience with Storybooks. We'll provide the ways in Kotlin/JS but mainly without much explanation what is that.

For the sake of providing example, we'll create a project with some simple component together.

Setting up the projects backbone
----- 

Usually the stories are being kept isolated, or as a separate project, the same we'll do here, the `app` project will
contain all
components while the stories will be contained under `stories` project. So general structure of the project would look
like this:

```text
|-app/
  |-src/
  |-build.gradle.kts
|-stories/
  |-src/
  |-build.gradle.kts
|-build.gradle.kts
|-setting.gradle.kts
```

The root `build.gradle.kts` is setting up the Kotlin/JS project, we'll use Kotlin 1.7.0 available at the time of
writing:

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

Here we define that we need to use Kotlin/JS gradle plugin of version 1.7.0 and build it for browser. Also we add to all
projects the repositories to fetch the artifacts from.

The `settings.gradle.kts` will include both our projects:

```kotlin
include(":app")
include(":stories")
```

The `app` and `stories` projects will remain empty for now, so just create empty `build.gradle.kts` files in both of
the directories.

Setting up the test `app` project
-------

We'll need some components to test with. We would need to set up Kotlin React project with basic routing and one
component implemented as a function (`FC`). The component should also have some properties so we could play out with
this as well.

Firstly, we add React dependencies into `app/build.gradle.kts`:

```kotlin
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
```

We'll be using IR-backend despite it being experimental at the time (though the whole Kotlin/JS thing is rather
immature)
. You can build the project now, so it would fetch the dependencies and make sure they are there and fix version if any
error happen. Do the `./gradlew build` from the root of the project.

*NOTE: at the time of writing that the React 18 was introduced and an example is developed with that in mind.*

Once import and npm-install tasks are successful, let's create the entry files and simplest component.

The `src/main/resources/index.html` to keep the initial element for the React application:

```html
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Storybook Playground</title>
</head>
<body>
<div id="root"></div>
<script type="text/javascript" src="app.js"></script>
</body>
</html>
```

Add simplest component implementation:

```kotlin
import react.FC
import react.Props
import react.dom.html.ReactHTML.div

external interface HelloProps : Props {
    var who: String
}

val Hello = FC<HelloProps> { props ->
    div {
        +"Hello ${props.who}!"
    }
}
```

Here the `Hello` functional component has defined the properties `HelloProps` so we can pass some arguments in.

Finally `src/main/kotlin/Main.kt` to contain start up code with the basic routing for `/`:

```kotlin
import kotlinx.browser.document
import react.FC
import react.Props
import react.create
import react.dom.client.createRoot
import react.router.Route
import react.router.Routes
import react.router.dom.BrowserRouter

fun main() {
    val root = createRoot(requireNotNull(document.getElementById("root")) {
        "The root element with id=`root` is not found"
    })
    root.render(App.create())
}

val App = FC<Props> {
    BrowserRouter {
        Routes {
            Route {
                path = "/"
                element = Hello.create {
                    who = "world"
                }
            }
        }
    }
}
```

Now you can run the project via `./gradlew :app:run` and you should be able to see the `Hello world!` in your browser.

