package dev.skymansandy.kurlclient.presentation.core

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.skymansandy.kurlclient.presentation.adaptive.WindowWidthClass
import org.koin.compose.viewmodel.koinViewModel
import dev.skymansandy.kurlclient.presentation.adaptive.toWindowWidthClass
import dev.skymansandy.kurlclient.presentation.screens.collections.CollectionsScreen
import dev.skymansandy.kurlclient.presentation.screens.collections.CollectionsViewModel
import dev.skymansandy.kurlclient.presentation.screens.workspace.RequestViewModel
import dev.skymansandy.kurlclient.presentation.screens.workspace.WorkspaceScreen
import dev.skymansandy.kurlstore.db.CollectionFolder
import dev.skymansandy.kurlstore.db.SavedRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

private enum class NavDestination(val label: String) {
    Workspace("Workspace"), Collections("Collections")
}

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
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false; pendingRequest = null },
            title = { Text("Discard changes?") },
            text = { Text("You have unsaved changes. Opening another request will discard them.") },
            confirmButton = {
                TextButton(onClick = {
                    showDiscardDialog = false
                    pendingRequest?.let { workspaceVm.loadSavedRequest(it) }
                    pendingRequest = null
                    selectedNav = NavDestination.Workspace
                }) { Text("Discard") }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardDialog = false; pendingRequest = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val windowClass = maxWidth.toWindowWidthClass()
        when (windowClass) {
            WindowWidthClass.Compact -> CompactScaffold(
                hasUnsavedChanges = workspaceVm.hasUnsavedChanges,
                activeRequestId = workspaceVm.loadedRequest?.id,
                allFolders = collectionsVm.allFolders,
                folderPaths = collectionsVm.folderPaths,
                selectedNav = selectedNav,
                snackbarHostState = snackbarHostState,
                scope = scope,
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
                scope = scope,
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

// ── Mobile layout ─────────────────────────────────────────────────────────────

@Composable
private fun CompactScaffold(
    hasUnsavedChanges: Boolean,
    activeRequestId: Long?,
    allFolders: List<CollectionFolder>,
    folderPaths: Map<Long, String>,
    selectedNav: NavDestination,
    snackbarHostState: SnackbarHostState,
    scope: CoroutineScope,
    onNavSelect: (NavDestination) -> Unit,
    onSaveSuccess: () -> Unit,
    onOverwriteSuccess: () -> Unit,
    onShowSnackbar: (String) -> Unit,
    onCreateFolder: (name: String, parentId: Long?) -> Unit,
    onRequestSelected: (SavedRequest) -> Unit,
    onSaveChanges: () -> Unit
) {
    Scaffold(
        bottomBar = { KurlNavigationBar(selected = selectedNav, hasUnsavedChanges = hasUnsavedChanges, onSelect = onNavSelect) },
        snackbarHost = { SnackbarHost(snackbarHostState) { Snackbar(snackbarData = it) } }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            when (selectedNav) {
                NavDestination.Workspace -> WorkspaceScreen(
                    allFolders = allFolders,
                    folderPaths = folderPaths,
                    onSaveSuccess = onSaveSuccess,
                    onOverwriteSuccess = onOverwriteSuccess,
                    onCreateFolder = onCreateFolder,
                    onShowSnackbar = onShowSnackbar,
                    modifier = Modifier.fillMaxSize()
                )
                NavDestination.Collections -> CollectionsScreen(
                    activeRequestId = activeRequestId,
                    onRequestSelected = onRequestSelected,
                    onSaveChanges = onSaveChanges,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

// ── Desktop layout ────────────────────────────────────────────────────────────

@Composable
private fun ExpandedScaffold(
    hasUnsavedChanges: Boolean,
    activeRequestId: Long?,
    allFolders: List<CollectionFolder>,
    folderPaths: Map<Long, String>,
    selectedNav: NavDestination,
    snackbarHostState: SnackbarHostState,
    scope: CoroutineScope,
    onNavSelect: (NavDestination) -> Unit,
    onSaveSuccess: () -> Unit,
    onOverwriteSuccess: () -> Unit,
    onShowSnackbar: (String) -> Unit,
    onCreateFolder: (name: String, parentId: Long?) -> Unit,
    onRequestSelected: (SavedRequest) -> Unit,
    onSaveChanges: () -> Unit
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) { Snackbar(snackbarData = it) } }
    ) { innerPadding ->
        Row(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            KurlNavigationRail(selected = selectedNav, hasUnsavedChanges = hasUnsavedChanges, onSelect = onNavSelect)
            VerticalDivider()
            when (selectedNav) {
                NavDestination.Workspace -> WorkspaceScreen(
                    allFolders = allFolders,
                    folderPaths = folderPaths,
                    onSaveSuccess = onSaveSuccess,
                    onOverwriteSuccess = onOverwriteSuccess,
                    onCreateFolder = onCreateFolder,
                    onShowSnackbar = onShowSnackbar,
                    modifier = Modifier.fillMaxSize()
                )
                NavDestination.Collections -> CollectionsScreen(
                    activeRequestId = activeRequestId,
                    onRequestSelected = onRequestSelected,
                    onSaveChanges = onSaveChanges,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

// ── Navigation components ─────────────────────────────────────────────────────

@Composable
private fun KurlNavigationBar(
    selected: NavDestination,
    hasUnsavedChanges: Boolean,
    onSelect: (NavDestination) -> Unit
) {
    NavigationBar {
        NavDestination.entries.forEach { dest ->
            NavigationBarItem(
                selected = selected == dest,
                onClick = { onSelect(dest) },
                icon = { NavIcon(dest, showBadge = dest == NavDestination.Workspace && hasUnsavedChanges) },
                label = { Text(dest.label) }
            )
        }
    }
}

@Composable
private fun KurlNavigationRail(
    selected: NavDestination,
    hasUnsavedChanges: Boolean,
    onSelect: (NavDestination) -> Unit
) {
    NavigationRail(modifier = Modifier.fillMaxHeight()) {
        NavDestination.entries.forEach { dest ->
            NavigationRailItem(
                selected = selected == dest,
                onClick = { onSelect(dest) },
                icon = { NavIcon(dest, showBadge = dest == NavDestination.Workspace && hasUnsavedChanges) },
                label = { Text(dest.label) }
            )
        }
    }
}

@Composable
private fun NavIcon(dest: NavDestination, showBadge: Boolean = false) {
    BadgedBox(badge = {
        if (showBadge) Badge(modifier = Modifier.size(8.dp))
    }) {
        when (dest) {
            NavDestination.Workspace -> Icon(Icons.Default.Dashboard, contentDescription = dest.label)
            NavDestination.Collections -> Icon(Icons.AutoMirrored.Filled.List, contentDescription = dest.label)
        }
    }
}