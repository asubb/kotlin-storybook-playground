package storybook.playground

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