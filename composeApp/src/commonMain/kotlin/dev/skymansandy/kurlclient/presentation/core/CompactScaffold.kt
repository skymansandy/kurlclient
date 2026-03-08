package dev.skymansandy.kurlclient.presentation.core

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.skymansandy.kurlclient.navigation.NavDestination
import dev.skymansandy.kurlclient.presentation.core.components.NavIcon
import dev.skymansandy.kurlclient.presentation.screens.collections.CollectionsScreen
import dev.skymansandy.kurlclient.presentation.screens.workspace.WorkspaceScreen
import dev.skymansandy.kurlstore.db.CollectionFolder
import dev.skymansandy.kurlstore.db.SavedRequest

// ── Mobile layout ─────────────────────────────────────────────────────────────

@Composable
internal fun CompactScaffold(
    hasUnsavedChanges: Boolean,
    activeRequestId: Long?,
    allFolders: List<CollectionFolder>,
    folderPaths: Map<Long, String>,
    selectedNav: NavDestination,
    snackbarHostState: SnackbarHostState,
    onNavSelect: (NavDestination) -> Unit,
    onSaveSuccess: () -> Unit,
    onOverwriteSuccess: () -> Unit,
    onShowSnackbar: (String) -> Unit,
    onCreateFolder: (name: String, parentId: Long?) -> Unit,
    onRequestSelected: (SavedRequest) -> Unit,
    onSaveChanges: () -> Unit
) {
    Scaffold(
        bottomBar = {
            KurlNavigationBar(
                selected = selectedNav,
                hasUnsavedChanges = hasUnsavedChanges,
                onSelect = onNavSelect
            )
        },
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
                icon = {
                    NavIcon(
                        dest,
                        showBadge = dest == NavDestination.Workspace && hasUnsavedChanges
                    )
                },
                label = { Text(dest.label) }
            )
        }
    }
}
