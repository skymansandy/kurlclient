package dev.skymansandy.kurlclient.presentation.core

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
import dev.skymansandy.kurlclient.navigation.NavDestination
import dev.skymansandy.kurlclient.presentation.core.components.NavIcon
import dev.skymansandy.kurlclient.presentation.screens.collections.presentation.screens.collection.CollectionsScreen
import dev.skymansandy.kurlclient.presentation.screens.workspace.presentation.screens.workspace.WorkspaceScreen
import dev.skymansandy.kurlstore.db.SavedRequest

@Composable
internal fun ExpandedScaffold(
    hasUnsavedChanges: Boolean,
    activeRequestId: Long?,
    selectedNav: NavDestination,
    snackbarHostState: SnackbarHostState,
    onNavSelect: (NavDestination) -> Unit,
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
                NavDestination.Workspace -> WorkspaceScreen(
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
                label = { Text(dest.label) },
                icon = {
                    NavIcon(
                        dest,
                        showBadge = dest == NavDestination.Workspace && hasUnsavedChanges
                    )
                },
            )
        }
    }
}