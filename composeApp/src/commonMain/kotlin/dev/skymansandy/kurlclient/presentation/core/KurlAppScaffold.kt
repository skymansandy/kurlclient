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
import dev.skymansandy.kurlclient.presentation.screens.collections.CollectionsViewModel
import dev.skymansandy.kurlclient.presentation.screens.workspace.RequestViewModel
import dev.skymansandy.kurlstore.db.SavedRequest
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun KurlAppScaffold() {
    // Both VMs are accessed here only for cross-feature coordination.
    // WorkspaceScreen and CollectionsScreen create their own instances via viewModel(),
    // which returns the same activity-scoped instance.
    val workspaceVm = koinViewModel<RequestViewModel>()
    val collectionsVm = koinViewModel<CollectionsViewModel>()

    var selectedNav by remember { mutableStateOf(NavDestination.Workspace) }
    var pendingRequest by remember { mutableStateOf<SavedRequest?>(null) }
    var showDiscardDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Cross-feature callbacks passed down to the layout composables
    val onShowSnackbar: (String) -> Unit = { msg ->
        scope.launch { snackbarHostState.showSnackbar(msg) }
    }
    val onSaveSuccess: () -> Unit = {
        collectionsVm.refresh()
        onShowSnackbar("Request saved to collections")
    }
    val onOverwriteSuccess: () -> Unit = {
        collectionsVm.refresh()
        onShowSnackbar("Changes saved")
    }
    val onRequestSelected: (SavedRequest) -> Unit = { saved ->
        if (workspaceVm.hasUnsavedChanges) {
            pendingRequest = saved
            showDiscardDialog = true
        } else {
            workspaceVm.loadSavedRequest(saved)
            selectedNav = NavDestination.Workspace
        }
    }

    if (showDiscardDialog) {
        DiscardWorkspaceAlertDialog(
            onConfirm = {
                showDiscardDialog = false
                pendingRequest?.let { workspaceVm.loadSavedRequest(it) }
                pendingRequest = null
                selectedNav = NavDestination.Workspace
            },
            onDismiss = {
                showDiscardDialog = false
                pendingRequest = null
            }
        )
    }

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize(),
    ) {
        val windowClass = maxWidth.toWindowWidthClass()
        when (windowClass) {
            WindowWidthClass.Compact -> CompactScaffold(
                hasUnsavedChanges = workspaceVm.hasUnsavedChanges,
                activeRequestId = workspaceVm.loadedRequest?.id,
                allFolders = collectionsVm.allFolders,
                folderPaths = collectionsVm.folderPaths,
                selectedNav = selectedNav,
                snackbarHostState = snackbarHostState,
                onNavSelect = { selectedNav = it },
                onSaveSuccess = onSaveSuccess,
                onOverwriteSuccess = onOverwriteSuccess,
                onShowSnackbar = onShowSnackbar,
                onCreateFolder = collectionsVm::createFolder,
                onRequestSelected = onRequestSelected,
                onSaveChanges = workspaceVm::overwriteLoadedRequest
            )

            else -> ExpandedScaffold(
                hasUnsavedChanges = workspaceVm.hasUnsavedChanges,
                activeRequestId = workspaceVm.loadedRequest?.id,
                allFolders = collectionsVm.allFolders,
                folderPaths = collectionsVm.folderPaths,
                selectedNav = selectedNav,
                snackbarHostState = snackbarHostState,
                onNavSelect = { selectedNav = it },
                onSaveSuccess = onSaveSuccess,
                onOverwriteSuccess = onOverwriteSuccess,
                onShowSnackbar = onShowSnackbar,
                onCreateFolder = collectionsVm::createFolder,
                onRequestSelected = onRequestSelected,
                onSaveChanges = workspaceVm::overwriteLoadedRequest
            )
        }
    }
}
