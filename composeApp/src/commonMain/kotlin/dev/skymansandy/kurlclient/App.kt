package dev.skymansandy.kurlclient

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.skymansandy.kurlclient.presentation.screens.workspace.presentation.screens.workspace.WorkSpaceScreen
import dev.skymansandy.kurlclient.presentation.theme.KurlTheme

@Composable
fun App() {
    KurlTheme {

        // For now only 1 screen
        // TODO setup jetbrains navigation3
        WorkSpaceScreen(
            modifier = Modifier.fillMaxSize(),
        )
    }
}
