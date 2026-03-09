package dev.skymansandy.kurlclient.presentation.screens.workspace

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import dev.skymansandy.kurlclient.presentation.screens.playground.PlaygroundScreenContract.PlaygroundEvent
import dev.skymansandy.kurlclient.presentation.screens.playground.PlaygroundScreenModel
import org.koin.compose.viewmodel.koinViewModel

class WorkspaceDesktopActions(
    val onImportCurl: () -> Unit,
    val onExportCurlCommand: () -> String,
)

@Composable
fun rememberWorkspaceDesktopActions(): WorkspaceDesktopActions {
    val playgroundVm: PlaygroundScreenModel = koinViewModel()
    return remember(playgroundVm) {
        WorkspaceDesktopActions(
            onImportCurl = { playgroundVm.onEvent(PlaygroundEvent.ShowImportCurlDialog) },
            onExportCurlCommand = { playgroundVm.buildCurlCommand() },
        )
    }
}
