package storybook.playground

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