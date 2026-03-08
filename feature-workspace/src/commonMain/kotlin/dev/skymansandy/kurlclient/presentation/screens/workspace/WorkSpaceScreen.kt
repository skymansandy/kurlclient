package dev.skymansandy.kurlclient.presentation.screens.workspace

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.skymansandy.kurlclient.presentation.adaptive.WindowWidthClass
import dev.skymansandy.kurlclient.presentation.adaptive.toWindowWidthClass
import dev.skymansandy.kurlclient.presentation.dialog.DiscardWorkspaceAlertDialog
import dev.skymansandy.kurlclient.presentation.screens.playground.PlaygroundScreenContract.PlaygroundEvent
import dev.skymansandy.kurlclient.presentation.screens.playground.PlaygroundScreenModel
import dev.skymansandy.kurlclient.presentation.screens.workspace.WorkspaceScreenContract.WorkspaceEvent
import dev.skymansandy.kurlclient.presentation.screens.workspace.scaffold.CompactScaffold
import dev.skymansandy.kurlclient.presentation.screens.workspace.scaffold.ExpandedScaffold
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun WorkSpaceScreen(
    modifier: Modifier = Modifier,
) {
    val vm: WorkspaceViewModel = koinViewModel()
    val state by vm.state.collectAsStateWithLifecycle()

    val playgroundVm: PlaygroundScreenModel = koinViewModel()
    val playgroundState by playgroundVm.state.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(state.requestToLoad) {
        state.requestToLoad?.let {
            playgroundVm.onEvent(PlaygroundEvent.LoadSavedRequest(it))
            vm.onEvent(WorkspaceEvent.ClearLoadRequest)
        }
    }

    val onShowSnackbar: (String) -> Unit = { msg ->
        scope.launch { snackbarHostState.showSnackbar(msg) }
    }

    if (state.showDiscardDialog) {
        DiscardWorkspaceAlertDialog(
            onConfirm = { vm.onEvent(WorkspaceEvent.ConfirmDiscard) },
            onDismiss = { vm.onEvent(WorkspaceEvent.DismissDiscard) }
        )
    }

    BoxWithConstraints(modifier = modifier) {
        val windowClass = maxWidth.toWindowWidthClass()
        when (windowClass) {
            WindowWidthClass.Compact -> CompactScaffold(
                hasUnsavedChanges = playgroundState.hasUnsavedChanges,
                activeRequestId = playgroundState.loadedRequest?.id,
                selectedNav = state.selectedTab,
                snackbarHostState = snackbarHostState,
                onNavSelect = { vm.onEvent(WorkspaceEvent.SelectTab(it)) },
                onShowSnackbar = onShowSnackbar,
                onRequestSelected = { vm.onEvent(WorkspaceEvent.TryLoadRequest(it, playgroundState.hasUnsavedChanges)) },
                onSaveChanges = { playgroundVm.onEvent(PlaygroundEvent.OverwriteLoadedRequest) }
            )

            else -> ExpandedScaffold(
                activeRequestId = playgroundState.loadedRequest?.id,
                snackbarHostState = snackbarHostState,
                onShowSnackbar = onShowSnackbar,
                onRequestSelected = { vm.onEvent(WorkspaceEvent.TryLoadRequest(it, playgroundState.hasUnsavedChanges)) },
                onSaveChanges = { playgroundVm.onEvent(PlaygroundEvent.OverwriteLoadedRequest) }
            )
        }
    }
}
