package dev.skymansandy.kurlclient.presentation.screens.workspace.presentation.screens.workspace.scaffold

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.skymansandy.kurlclient.presentation.screens.workspace.presentation.components.NavIcon
import dev.skymansandy.kurlclient.presentation.screens.workspace.presentation.screens.collections.CollectionsScreen
import dev.skymansandy.kurlclient.presentation.screens.workspace.presentation.screens.playground.PlaygroundScreen
import dev.skymansandy.kurlclient.presentation.screens.workspace.presentation.screens.workspace.model.WorkspaceTab
import dev.skymansandy.kurlstore.db.SavedRequest

@Composable
internal fun ExpandedScaffold(
    hasUnsavedChanges: Boolean,
    activeRequestId: Long?,
    selectedNav: WorkspaceTab,
    snackbarHostState: SnackbarHostState,
    onNavSelect: (WorkspaceTab) -> Unit,
    onShowSnackbar: (String) -> Unit,
    onRequestSelected: (SavedRequest) -> Unit,
    onSaveChanges: () -> Unit
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) { Snackbar(snackbarData = it) } }
    ) { innerPadding ->
        Row(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            KurlNavigationRail(
                selected = selectedNav,
                hasUnsavedChanges = hasUnsavedChanges,
                onSelect = onNavSelect
            )
            VerticalDivider()
            when (selectedNav) {
                WorkspaceTab.Workspace -> PlaygroundScreen(
                    onShowSnackbar = onShowSnackbar,
                    modifier = Modifier.fillMaxSize()
                )

                WorkspaceTab.Collections -> CollectionsScreen(
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
private fun KurlNavigationRail(
    selected: WorkspaceTab,
    hasUnsavedChanges: Boolean,
    onSelect: (WorkspaceTab) -> Unit
) {
    NavigationRail(modifier = Modifier.fillMaxHeight()) {
        WorkspaceTab.entries.forEach { dest ->
            NavigationRailItem(
                selected = selected == dest,
                onClick = { onSelect(dest) },
                label = { Text(dest.label) },
                icon = {
                    NavIcon(
                        dest,
                        showBadge = dest == WorkspaceTab.Workspace && hasUnsavedChanges
                    )
                },
            )
        }
    }
}