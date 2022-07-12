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