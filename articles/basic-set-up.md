React Storybook for Kotlin/JS: Basic set up
===========

Introduction
------

The Storybook is very helpful approach for developing UI applications that allows you to test the component in isolation
which improves not only its testability but forces you to have a better design.

This tutorial attempts to find a way to use storybooks within Kotlin/JS applications. That is just a first try, so the
way provided here is not the best, but it works. There is a lot of room of improvement, but before we need to understand
where the touch points are, and what needs to be improved. All ideas and any feedback are very welcome.

Disclaimer: I'm in no way the expert in neither Kotlin/JS nor JS/Storybook/React, but I do my best.

Prerequisites
------

Before we start let's make sure we have enough to kick us up. From the Reader the following is expected:

* Understanding of the Kotlin/JS as well as interoperability with React. We'll cover the main points here but won't go
  much deep.
* Vanilla JS knowledge will be very helpful as well as previous experience with Storybooks. We'll provide the ways in
  Kotlin/JS but mainly without much explanation what is that.

For the sake of providing example, we'll create a project with some simple component together.

Setting up the projects backbone
----- 

Usually the stories are being kept isolated, or as a separate project, the same we'll do here, the `app` project will
contain all components while the stories will be contained under `stories` project. So general structure of the project
would look like this:

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

The root `build.gradle.kts` is setting up the Kotlin/JS project, we'll use Kotlin 1.7.10 available at the time of
writing:

```kotlin
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

    // common dependencies
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
```

Here we define that we need to use Kotlin/JS gradle plugin of version 1.7.10 and build JS for the browser. Then we add
to all projects the repositories to fetch the artifacts from. Finally, we add React dependencies to all children, so you
won't duplicate it there. The bom version points to React 17 compatible wrappers.

*NOTE: at the time of writing, the React 18 was introduced, but Storybook didn't support it fully so the React 17 is
used here. Though, the upgrade to 18 should be fairly straight forward once the Storybook adds the full support.*

The `settings.gradle.kts` will include both of our projects:

```kotlin
include(":app")
include(":stories")
```

The `app` and `stories` projects will remain empty for now, so just create empty `build.gradle.kts` files in both of
the directories.

Setting up the test `app` project
-------

We'll need some components to test with. We would need to set up Kotlin React project with basic routing and one
component implemented as a function (`FC`). The component should also have some properties, so we could play around with
this as well.

Firstly, we make an app as Kotlin/JS by adding the following into `app/build.gradle.kts`:

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
```

The react dependencies is provided by the root project.

We'll be using IR-backend despite it being experimental at the time (though the whole Kotlin/JS thing is rather
immature).

You can build the project now, so it would fetch the dependencies and make sure they are there and fix version if any
error happen. Do the `./gradlew build` from the root of the project.

Once import and npm-install tasks are successful, let's create the entry files and simplest component.

Then add `src/main/resources/index.html` to keep the initial element for the React application:

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

Now you can run the project via `./gradlew :app:run`, and you should be able to see the `Hello world!` in your browser.

Creating a Story
-----

There are a few things we need to take care of here, on top of just providing storybook dependencies and stories
themselves. Most of these points require separate investigation, and we'll probably attempt to do it at some point:

1. Storybook uses the one js-file per component
   using [CSF format](https://storybook.js.org/blog/component-story-format-3-0/). That implies one file per component
   with a set of stories. Kotlin/JS compiler generates one file for the whole module, as well as the internals are not
   very straight forward and might be hard to keep them compatible while the compiler is being developed. To solve that
   we'll use some VanillaJS files with boilerplate code. It might easily be resolved by implementing a gradle plugin
   that generates that code for us. But we'll keep it simple here.
2. Storybook needs access to libraries fetched by npm, and uses npm to start up the storybook process. That seems not
   possible with Kotlin/JS gradle plugin at the moment, though perhaps needs to be investigated deeper. As a workaround,
   we'll use standalone gradle npm plugin that uses generated `package.json` from the project, but needs to fetch all
   modules once again.
3. All dependencies defined as regular maven wrapper dependencies needs to be duplicated as `npm` so they'll appear in
   generated `package.json`. Kotlin/JS plugin connects them via workspaces, which at the moment is not clear how to
   reuse. That is somewhat similar issue to the mentioned in the point above.
4. Storybook process and rebuild process that generates JS files from Kotlin are done as two separate gradle tasks,
   and rebuild should be run every time the Kotlin classes are changed.

So keeping all that in mind let's start working on the very first story.

Firstly, we need to add dependencies into the project. Let's add the following into `stories/build.gradle.kts`:

```kotlin
plugins {
    kotlin("js")
    id("com.github.node-gradle.node") version "3.4.0"
}

kotlin {
    js(IR) {
        // let's rename it to more reusable as under that name we will access it in our boilerplate code
        moduleName = "stories"
        // browser also works fine here, we just need it for compiling purposes as of now
        nodejs {}
        // add a startup script to our package json
        compilations["main"].packageJson {
            customField(
                "scripts",
                mapOf("storybook" to "start-storybook -p 6006 -c $projectDir/.storybook --ci")
            )
        }
        binaries.executable()
    }
}


tasks.named<DefaultTask>("build") {
    dependsOn("assemble")
    dependsOn("copyJsStories")
}

tasks.register<Copy>("copyJsStories") {
    dependsOn("developmentExecutableCompileSync")
    from("$projectDir/src/main/js")
    into("$buildDir/compileSync/main/developmentExecutable/kotlin")
    // flatten all files to appear on one level
    eachFile {
        if (isDirectory) {
            exclude()
        }
        path = path.replace("/", ".")
    }
}

tasks.register<Copy>("copyPackageJson") {
    dependsOn("build")
    from("$buildDir/tmp/publicPackageJson/package.json")
    into("$projectDir")
}

tasks.register<com.github.gradle.node.npm.task.NpmTask>("start") {
    dependsOn("build")
    dependsOn("npmInstall")
    args.addAll("run", "storybook")
}

tasks.named<com.github.gradle.node.npm.task.NpmInstallTask>("npmInstall") {
    dependsOn("copyPackageJson")
    workingDir.set(file("$projectDir"))
    inputs.file("package.json")
}

dependencies {
    // dependency to the project with components
    implementation(project(":app"))

    // react dependencies to put on package.json explicitly
    // can resolve the actual versions on https://github.com/JetBrains/kotlin-wrappers
    implementation(npm("react", "^17.0.2"))
    implementation(npm("react-dom", "^17.0.2"))
    implementation(npm("react-router-dom", "^6.2.2"))

    // storybook specific dependencies
    implementation(npm("@storybook/builder-webpack5", "^6.5.9"))
    implementation(npm("@storybook/manager-webpack5", "^6.5.9"))
    implementation(npm("@storybook/node-logger", "^6.5.9"))
    implementation(npm("@storybook/preset-create-react-app", "^4.1.2"))
    implementation(npm("@storybook/react", "^6.5.9"))
}
```

That script also introduces two main custom gradle tasks:

1. `start` to initiate the storybook process. You would need to run it once and keep it running in the background. It
   automatically fetches the required dependencies.
2. `build` to build the source files to be picked up by the storybook process. Whenever you change the stories source or
   bindings you would need to run that task.

Also, there are a few supportive tasks that you don't need to call directly:

* `copyJsStories` copies over the bindings from source folder to build folder nearby the compiled Kotlin classes.
* `copyPackageJson` copies over the generated `package.json` file into the project root, so it'll be picked up by the
  npm process for storybook.
* `npmInstall` is an extension of `npm install` task to make sure it'll find everything needed in that project
  configuration.

Secondly, let's provide the configuration file for our storybook instance. It's a regular configuration file with only
one difference: the definition where to search for the stories, we'll point into build directory where all Kotlin files
and bindings are being copied over to. The content of the file `stories/.storybook/main.js` is:

```js
module.exports = {
    "stories": [
        "../build/compileSync/main/developmentExecutable/kotlin/*.stories.js"
    ]
}
```

And you also need to add preview configuration even though we won't change anything for our example (but you may if you
need to), the content of `stories/.storybook/preview.js` is just an empty object:

```js
export const parameters = {}
```

Lastly, let's define simple stories. The stories will consist of two parts:

1. Kotlin/JS implementation of the stories under `src/main/kotlin`.
2. VanillaJS bindings under `src/main/js`.

The Kotlin story file `HelloStories` is the regular class that is marked with `@JsExport` so it can be used within
VanillaJS files (a "must" for IR backend). The story is supposed to be a function that creates a component instance with
certain parameters. The whole class would look this:

```kotlin
package storybook.playground

import react.create

@JsExport
class HelloStories {

    val title: String = "Hello"

    val component = Hello

    val helloStory = {
        Hello.create {
            who = "story"
        }
    }

    val helloUniverse = {
        Hello.create {
            who = "Universe"
        }
    }
}
```

Here we defined two stories: `helloStory` and `helloUniverse` as well as title and component to be populated via
bindings to the storybook.

Binding is the javascript file written in convention to `csf` format, it contains only boilerplate code to connect
Kotlin files with Storybook. It'll be copied over as is. Here is how `Hello.stories.js` would look like:

```js
import React from 'react';
import * as x from './stories.js'

const stories = new x.storybook.playground.HelloStories()

export default {
    title: stories.title,
    component: stories.component,
}

export const helloStory = stories.helloStory
export const helloUniverse = stories.helloUniverse
```

The `HelloStories` instance are imported from compiled Kotlin code that is compiled into `./stories.js` (the file name
is defined in gradle file of the module `kotlin.js.moduleName`). Then the instance of the class is instantiated and we
can get access to its fields. And this is what we do by populating the default exported object with title and component,
as well as exporting each individual story as a separate constant.

The storybook process can be started via `./gradlew :stories:start` which also performs the initial build of the source
code. Whenever the code got changed, run `./gradlew :stories:build` and the changes will automatically be picked up by
the running storybook process. The storybook can be accessed via the browser by default
over [http://localhost:6006](http://localhost:6006).

As you see the bindings define how the stories will be interpreted by the storybook, so it's up to you if you want to
have one class to one binder, or multiple story binders per class, or other way around, but one-to-one seems to be
reasonable approach.

Conclusion
------

* We were able to make simple story to run (almost) fully from Kotlin keeping the nice things like type safety,
  compilation and meaningful suggestions in IDE.
* There is a big room for improvements, but now we understand what is the actual flow should be and what is better to
  automate within gradle plugin.
* You can find the source code on [GitHub]()

Feel free to leave any comments, feedback or ideas. Happy Koding!
