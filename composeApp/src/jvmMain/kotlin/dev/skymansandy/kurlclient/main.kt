package dev.skymansandy.kurlclient

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import dev.skymansandy.kurlclient.di.initKoin

fun main() {
    initKoin()
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "KurlClient",
        ) {
            App()
        }
    }
}
