package dev.skymansandy.kurlclient

import androidx.compose.ui.window.ComposeUIViewController
import dev.skymansandy.kurl.store.AppDatabase
import dev.skymansandy.kurl.store.createDatabaseDriver
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController {
    AppDatabase.init(createDatabaseDriver())
    return ComposeUIViewController { App() }
}