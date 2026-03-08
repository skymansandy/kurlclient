package dev.skymansandy.kurlclient.presentation.screens.workspace

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.skymansandy.kurlclient.presentation.screens.workspace.presentation.screens.playground.PlaygroundEvent
import dev.skymansandy.kurlclient.presentation.screens.workspace.presentation.screens.playground.PlaygroundScreenModel
import dev.skymansandy.kurlstore.db.SavedRequest
import org.koin.compose.viewmodel.koinViewModel

data class WorkspaceCrossFeatureState(
    val hasUnsavedChanges: Boolean,
    val loadedRequestId: Long?,
    val onLoadSavedRequest: (SavedRequest) -> Unit,
    val onOverwriteLoadedRequest: () -> Unit
)

@Composable
fun rememberWorkspaceCrossFeatureState(): WorkspaceCrossFeatureState {
    val vm: PlaygroundScreenModel = koinViewModel()
    val state by vm.state.collectAsStateWithLifecycle()
    return WorkspaceCrossFeatureState(
        hasUnsavedChanges = state.hasUnsavedChanges,
        loadedRequestId = state.loadedRequest?.id,
        onLoadSavedRequest = { vm.onEvent(PlaygroundEvent.LoadSavedRequest(it)) },
        onOverwriteLoadedRequest = { vm.onEvent(PlaygroundEvent.OverwriteLoadedRequest) }
    )
}