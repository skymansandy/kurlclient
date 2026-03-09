package dev.skymansandy.kurlclient

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import dev.skymansandy.kurlclient.di.initKoin
import dev.skymansandy.kurlclient.presentation.screens.workspace.rememberWorkspaceDesktopActions
import dev.skymansandy.kurlclient.presentation.theme.KurlTheme
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

fun main() {
    initKoin()
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "KurlClient",
        ) {
            val workspaceActions = rememberWorkspaceDesktopActions()
            var showAboutDialog by remember { mutableStateOf(false) }

            MenuBar {
                Menu("Workspace") {
                    Item("Import cURL") {
                        workspaceActions.onImportCurl()
                    }
                    Item("Export cURL") {
                        val curl = workspaceActions.onExportCurlCommand()
                        Toolkit.getDefaultToolkit().systemClipboard
                            .setContents(StringSelection(curl), null)
                    }
                }
                Menu("Help") {
                    Item("About KurlClient") {
                        showAboutDialog = true
                    }
                }
            }

            App()

            if (showAboutDialog) {
                KurlTheme {
                    AboutDialog(onDismiss = { showAboutDialog = false })
                }
            }
        }
    }
}
