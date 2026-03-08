package dev.skymansandy.kurlclient.presentation.screens.workspace

import dev.skymansandy.kurlclient.presentation.base.contract.UiEffect
import dev.skymansandy.kurlclient.presentation.base.contract.UiEvent
import dev.skymansandy.kurlclient.presentation.base.contract.UiState
import dev.skymansandy.kurlclient.presentation.screens.workspace.model.WorkspaceTab
import dev.skymansandy.kurlstore.db.SavedRequest

internal data class WorkspaceState(
    val selectedTab: WorkspaceTab = WorkspaceTab.Workspace,
    val showDiscardDialog: Boolean = false,
    val pendingRequest: SavedRequest? = null,
    val requestToLoad: SavedRequest? = null,
) : UiState

internal sealed interface WorkspaceEvent : UiEvent {
    data class SelectTab(val tab: WorkspaceTab) : WorkspaceEvent
    data class TryLoadRequest(val saved: SavedRequest, val hasUnsavedChanges: Boolean) : WorkspaceEvent
    data object ConfirmDiscard : WorkspaceEvent
    data object DismissDiscard : WorkspaceEvent
    data object ClearLoadRequest : WorkspaceEvent
}

internal sealed interface WorkspaceEffect : UiEffect