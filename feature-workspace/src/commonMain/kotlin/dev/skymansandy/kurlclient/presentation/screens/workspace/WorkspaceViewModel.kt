package dev.skymansandy.kurlclient.presentation.screens.workspace

import dev.skymansandy.kurlclient.presentation.base.MviViewModel
import dev.skymansandy.kurlclient.presentation.screens.workspace.WorkspaceScreenContract.WorkspaceEffect
import dev.skymansandy.kurlclient.presentation.screens.workspace.WorkspaceScreenContract.WorkspaceEvent
import dev.skymansandy.kurlclient.presentation.screens.workspace.WorkspaceScreenContract.WorkspaceState
import dev.skymansandy.kurlclient.presentation.screens.workspace.model.WorkspaceTab
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
internal class WorkspaceViewModel : MviViewModel<WorkspaceState, WorkspaceEvent, WorkspaceEffect>() {

    override fun createInitialState() = WorkspaceState()

    override fun onEvent(event: WorkspaceEvent) {
        when (event) {
            is WorkspaceEvent.SelectTab -> setState { copy(selectedTab = event.tab) }

            is WorkspaceEvent.TryLoadRequest -> {
                if (event.hasUnsavedChanges) {
                    setState { copy(pendingRequest = event.saved, showDiscardDialog = true) }
                } else {
                    setState { copy(requestToLoad = event.saved, selectedTab = WorkspaceTab.Workspace) }
                }
            }

            WorkspaceEvent.ConfirmDiscard -> setState {
                copy(
                    showDiscardDialog = false,
                    requestToLoad = pendingRequest,
                    pendingRequest = null,
                    selectedTab = WorkspaceTab.Workspace,
                )
            }

            WorkspaceEvent.DismissDiscard -> setState {
                copy(showDiscardDialog = false, pendingRequest = null)
            }

            WorkspaceEvent.ClearLoadRequest -> setState { copy(requestToLoad = null) }
        }
    }
}
