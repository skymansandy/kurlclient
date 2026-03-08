package dev.skymansandy.kurlclient.presentation.component

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.skymansandy.kurlclient.presentation.screens.workspace.model.WorkspaceTab
import kurlclient.feature_workspace.generated.resources.Res
import kurlclient.feature_workspace.generated.resources.tab_collections
import kurlclient.feature_workspace.generated.resources.tab_workspace
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun NavIcon(
    dest: WorkspaceTab,
    showBadge: Boolean = false,
) {
    BadgedBox(
        badge = {
            if (showBadge) {
                Badge(modifier = Modifier.size(8.dp))
            }
        },
    ) {
        Icon(
            contentDescription = when (dest) {
                WorkspaceTab.Workspace -> stringResource(Res.string.tab_workspace)
                WorkspaceTab.Collections -> stringResource(Res.string.tab_collections)
            },
            imageVector = when (dest) {
                WorkspaceTab.Workspace -> Icons.Default.Dashboard
                WorkspaceTab.Collections -> Icons.AutoMirrored.Filled.List
            },
        )
    }
}
