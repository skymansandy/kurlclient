package dev.skymansandy.kurlclient.presentation.screens.workspace.scaffold

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
import dev.skymansandy.kurlclient.presentation.component.NavIcon
import dev.skymansandy.kurlclient.presentation.screens.collections.CollectionsScreen
import dev.skymansandy.kurlclient.presentation.screens.playground.PlaygroundScreen
import dev.skymansandy.kurlclient.presentation.screens.workspace.model.WorkspaceTab
import dev.skymansandy.kurlstore.db.SavedRequest
import kurlclient.feature_workspace.generated.resources.Res
import kurlclient.feature_workspace.generated.resources.tab_collections
import kurlclient.feature_workspace.generated.resources.tab_workspace
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun CompactScaffold(
    hasUnsavedChanges: Boolean,
    activeRequestId: Long?,
    selectedNav: WorkspaceTab,
    snackbarHostState: SnackbarHostState,
    onNavSelect: (WorkspaceTab) -> Unit,
    onShowSnackbar: (String) -> Unit,
    onRequestSelected: (SavedRequest) -> Unit,
    onSaveChanges: () -> Unit,
) {
    Scaffold(
        bottomBar = {
            KurlNavigationBar(
                selected = selectedNav,
                hasUnsavedChanges = hasUnsavedChanges,
                onSelect = onNavSelect,
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) { Snackbar(snackbarData = it) } },
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            when (selectedNav) {
                WorkspaceTab.Workspace -> PlaygroundScreen(
                    onShowSnackbar = onShowSnackbar,
                    modifier = Modifier.fillMaxSize(),
                )

                WorkspaceTab.Collections -> CollectionsScreen(
                    activeRequestId = activeRequestId,
                    onRequestSelected = onRequestSelected,
                    onSaveChanges = onSaveChanges,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}

@Composable
private fun KurlNavigationBar(
    selected: WorkspaceTab,
    hasUnsavedChanges: Boolean,
    onSelect: (WorkspaceTab) -> Unit,
) {
    NavigationBar {
        WorkspaceTab.entries.forEach { dest ->
            NavigationBarItem(
                selected = selected == dest,
                onClick = { onSelect(dest) },
                icon = {
                    NavIcon(
                        dest,
                        showBadge = dest == WorkspaceTab.Workspace && hasUnsavedChanges,
                    )
                },
                label = {
                    Text(
                        stringResource(
                            when (dest) {
                                WorkspaceTab.Workspace -> Res.string.tab_workspace
                                WorkspaceTab.Collections -> Res.string.tab_collections
                            },
                        ),
                    )
                },
            )
        }
    }
}
