package dev.skymansandy.kurlclient.presentation.core

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import dev.skymansandy.kurlclient.navigation.NavDestination
import dev.skymansandy.kurlclient.presentation.adaptive.WindowWidthClass
import dev.skymansandy.kurlclient.presentation.adaptive.toWindowWidthClass
import dev.skymansandy.kurlclient.presentation.core.dialog.DiscardWorkspaceAlertDialog
import dev.skymansandy.kurlclient.presentation.screens.workspace.rememberWorkspaceCrossFeatureState
import dev.skymansandy.kurlstore.db.SavedRequest
import kotlinx.coroutines.launch

@Composable
fun KurlAppScaffold() {
    val workspaceCrossState = rememberWorkspaceCrossFeatureState()

    var selectedNav by remember { mutableStateOf(NavDestination.Workspace) }
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
            selectedNav = NavDestination.Workspace
        }
    }

    if (showDiscardDialog) {
        DiscardWorkspaceAlertDialog(
            onConfirm = {
                showDiscardDialog = false
                pendingRequest?.let { workspaceCrossState.onLoadSavedRequest(it) }
                pendingRequest = null
                selectedNav = NavDestination.Workspace
            },
            onDismiss = {
                showDiscardDialog = false
                pendingRequest = null
            }
        )
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val windowClass = maxWidth.toWindowWidthClass()
        when (windowClass) {
            WindowWidthClass.Compact -> CompactScaffold(
                hasUnsavedChanges = workspaceCrossState.hasUnsavedChanges,
                activeRequestId = workspaceCrossState.loadedRequestId,
                selectedNav = selectedNav,
                snackbarHostState = snackbarHostState,
                onNavSelect = { selectedNav = it },
                onShowSnackbar = onShowSnackbar,
                onRequestSelected = onRequestSelected,
                onSaveChanges = workspaceCrossState.onOverwriteLoadedRequest
            )

            else -> ExpandedScaffold(
                hasUnsavedChanges = workspaceCrossState.hasUnsavedChanges,
                activeRequestId = workspaceCrossState.loadedRequestId,
                selectedNav = selectedNav,
                snackbarHostState = snackbarHostState,
                onNavSelect = { selectedNav = it },
                onShowSnackbar = onShowSnackbar,
                onRequestSelected = onRequestSelected,
                onSaveChanges = workspaceCrossState.onOverwriteLoadedRequest
            )
        }
    }
}