package dev.skymansandy.kurlclient.presentation.screens.workspace.presentation.screens.workspace

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import dev.skymansandy.kurlclient.presentation.adaptive.WindowWidthClass
import dev.skymansandy.kurlclient.presentation.adaptive.toWindowWidthClass
import dev.skymansandy.kurlclient.presentation.screens.workspace.presentation.dialog.DiscardWorkspaceAlertDialog
import dev.skymansandy.kurlclient.presentation.screens.workspace.presentation.screens.workspace.model.WorkspaceTab
import dev.skymansandy.kurlclient.presentation.screens.workspace.presentation.screens.workspace.scaffold.CompactScaffold
import dev.skymansandy.kurlclient.presentation.screens.workspace.presentation.screens.workspace.scaffold.ExpandedScaffold
import dev.skymansandy.kurlclient.presentation.screens.workspace.rememberWorkspaceCrossFeatureState
import dev.skymansandy.kurlstore.db.SavedRequest
import kotlinx.coroutines.launch

@Composable
fun WorkSpaceScreen(
    modifier: Modifier = Modifier,
) {
    val workspaceCrossState = rememberWorkspaceCrossFeatureState()

    var selectedTab by remember { mutableStateOf(WorkspaceTab.Workspace) }
    var pendingRequest by remember { mutableStateOf<SavedRequest?>(null) }
    var showDiscardDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val onShowSnackbar: (String) -> Unit = { msg ->
        scope.launch { snackbarHostState.showSnackbar(msg) }
    }
    val onRequestSelected: (SavedRequest) -> Unit = { saved ->
        if (workspaceCrossState.hasUnsavedChanges) {
            pendingRequest = saved
            showDiscardDialog = true
        } else {
            workspaceCrossState.onLoadSavedRequest(saved)
            selectedTab = WorkspaceTab.Workspace
        }
    }

    if (showDiscardDialog) {
        DiscardWorkspaceAlertDialog(
            onConfirm = {
                showDiscardDialog = false
                pendingRequest?.let { workspaceCrossState.onLoadSavedRequest(it) }
                pendingRequest = null
                selectedTab = WorkspaceTab.Workspace
            },
            onDismiss = {
                showDiscardDialog = false
                pendingRequest = null
            }
        )
    }

    BoxWithConstraints(modifier = modifier) {
        val windowClass = maxWidth.toWindowWidthClass()
        when (windowClass) {
            WindowWidthClass.Compact -> CompactScaffold(
                hasUnsavedChanges = workspaceCrossState.hasUnsavedChanges,
                activeRequestId = workspaceCrossState.loadedRequestId,
                selectedNav = selectedTab,
                snackbarHostState = snackbarHostState,
                onNavSelect = { selectedTab = it },
                onShowSnackbar = onShowSnackbar,
                onRequestSelected = onRequestSelected,
                onSaveChanges = workspaceCrossState.onOverwriteLoadedRequest
            )

            else -> ExpandedScaffold(
                hasUnsavedChanges = workspaceCrossState.hasUnsavedChanges,
                activeRequestId = workspaceCrossState.loadedRequestId,
                selectedNav = selectedTab,
                snackbarHostState = snackbarHostState,
                onNavSelect = { selectedTab = it },
                onShowSnackbar = onShowSnackbar,
                onRequestSelected = onRequestSelected,
                onSaveChanges = workspaceCrossState.onOverwriteLoadedRequest
            )
        }
    }
}
