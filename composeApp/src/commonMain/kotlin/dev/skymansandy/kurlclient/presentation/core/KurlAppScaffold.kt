package dev.skymansandy.kurlclient.presentation.core

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.skymansandy.kurlclient.presentation.screens.workspace.presentation.screens.workspace.WorkSpaceScreen

@Composable
fun KurlAppScaffold() {

    WorkSpaceScreen(
        modifier = Modifier.fillMaxSize(),
    )
}
