@file:OptIn(kotlin.js.ExperimentalJsExport::class)

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