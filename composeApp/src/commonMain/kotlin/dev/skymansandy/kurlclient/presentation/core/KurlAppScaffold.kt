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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.skymansandy.kurlclient.presentation.screens.collections.presentation.screens.collection.CollectionsEvent
import dev.skymansandy.kurlclient.presentation.screens.collections.presentation.screens.collection.CollectionsViewModel
import dev.skymansandy.kurlclient.presentation.screens.workspace.presentation.screens.workspace.WorkspaceViewModel
import dev.skymansandy.kurlclient.presentation.screens.workspace.presentation.screens.workspace.WorkspaceEvent
import dev.skymansandy.kurlstore.db.SavedRequest
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun KurlAppScaffold() {
    // Both VMs are accessed here only for cross-feature coordination.
    // WorkspaceScreen and CollectionsScreen create their own instances via viewModel(),
    // which returns the same activity-scoped instance.
    val workspaceVm = koinViewModel<WorkspaceViewModel>()
    val workspaceState by workspaceVm.state.collectAsStateWithLifecycle()
    val collectionsVm = koinViewModel<CollectionsViewModel>()
    val collectionsState by collectionsVm.state.collectAsStateWithLifecycle()

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
        collectionsVm.onEvent(CollectionsEvent.Refresh)
        onShowSnackbar("Request saved to collections")
    }
    val onOverwriteSuccess: () -> Unit = {
        collectionsVm.onEvent(CollectionsEvent.Refresh)
        onShowSnackbar("Changes saved")
    }
    val onRequestSelected: (SavedRequest) -> Unit = { saved ->
        if (workspaceState.hasUnsavedChanges) {
            pendingRequest = saved
            showDiscardDialog = true
        } else {
            workspaceVm.onEvent(WorkspaceEvent.LoadSavedRequest(saved))
            selectedNav = NavDestination.Workspace
        }
    }

    if (showDiscardDialog) {
        DiscardWorkspaceAlertDialog(
            onConfirm = {
                showDiscardDialog = false
                pendingRequest?.let { workspaceVm.onEvent(WorkspaceEvent.LoadSavedRequest(it)) }
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
                hasUnsavedChanges = workspaceState.hasUnsavedChanges,
                activeRequestId = workspaceState.loadedRequest?.id,
                allFolders = collectionsState.allFolders,
                folderPaths = collectionsState.folderPaths,
                selectedNav = selectedNav,
                snackbarHostState = snackbarHostState,
                onNavSelect = { selectedNav = it },
                onSaveSuccess = onSaveSuccess,
                onOverwriteSuccess = onOverwriteSuccess,
                onShowSnackbar = onShowSnackbar,
                onCreateFolder = { name, parentId -> collectionsVm.onEvent(CollectionsEvent.CreateFolder(name, parentId)) },
                onRequestSelected = onRequestSelected,
                onSaveChanges = { workspaceVm.onEvent(WorkspaceEvent.OverwriteLoadedRequest) }
            )

            else -> ExpandedScaffold(
                hasUnsavedChanges = workspaceState.hasUnsavedChanges,
                activeRequestId = workspaceState.loadedRequest?.id,
                allFolders = collectionsState.allFolders,
                folderPaths = collectionsState.folderPaths,
                selectedNav = selectedNav,
                snackbarHostState = snackbarHostState,
                onNavSelect = { selectedNav = it },
                onSaveSuccess = onSaveSuccess,
                onOverwriteSuccess = onOverwriteSuccess,
                onShowSnackbar = onShowSnackbar,
                onCreateFolder = { name, parentId -> collectionsVm.onEvent(CollectionsEvent.CreateFolder(name, parentId)) },
                onRequestSelected = onRequestSelected,
                onSaveChanges = { workspaceVm.onEvent(WorkspaceEvent.OverwriteLoadedRequest) }
            )
        }
    }
}
