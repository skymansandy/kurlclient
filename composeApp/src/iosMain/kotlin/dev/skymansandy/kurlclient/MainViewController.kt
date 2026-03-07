package dev.skymansandy.kurlclient

import androidx.compose.ui.window.ComposeUIViewController
import dev.skymansandy.kurlclient.di.initKoin
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController {
    initKoin()
    return ComposeUIViewController { App() }
}