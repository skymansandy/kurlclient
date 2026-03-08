package dev.skymansandy.kurlclient.presentation.screens.workspace.presentation.components

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
import dev.skymansandy.kurlclient.presentation.screens.workspace.presentation.screens.workspace.model.WorkspaceTab

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
        }
    ) {
        when (dest) {
            WorkspaceTab.Workspace -> Icon(
                Icons.Default.Dashboard,
                contentDescription = dest.label
            )

            WorkspaceTab.Collections -> Icon(
                Icons.AutoMirrored.Filled.List,
                contentDescription = dest.label
            )
        }
    }
}
