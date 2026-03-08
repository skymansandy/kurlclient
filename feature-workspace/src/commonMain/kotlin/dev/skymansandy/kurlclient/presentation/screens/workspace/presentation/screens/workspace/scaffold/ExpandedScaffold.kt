package dev.skymansandy.kurlclient.presentation.screens.workspace.presentation.screens.workspace.scaffold

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.skymansandy.kurlclient.presentation.screens.workspace.presentation.screens.collections.CollectionsScreen
import dev.skymansandy.kurlclient.presentation.screens.workspace.presentation.screens.playground.PlaygroundScreen
import dev.skymansandy.kurlstore.db.SavedRequest

@Composable
internal fun ExpandedScaffold(
    activeRequestId: Long?,
    snackbarHostState: SnackbarHostState,
    onShowSnackbar: (String) -> Unit,
    onRequestSelected: (SavedRequest) -> Unit,
    onSaveChanges: () -> Unit
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) { Snackbar(snackbarData = it) } }
    ) { innerPadding ->
        Row(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            CollectionsScreen(
                activeRequestId = activeRequestId,
                onRequestSelected = onRequestSelected,
                onSaveChanges = onSaveChanges,
                modifier = Modifier.width(300.dp).fillMaxHeight()
            )
            VerticalDivider()
            PlaygroundScreen(
                onShowSnackbar = onShowSnackbar,
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
        }
    }
}
