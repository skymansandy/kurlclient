package dev.skymansandy.kurlclient.presentation.screens.collections.component.tree

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.skymansandy.kurlclient.presentation.screens.collections.CollectionsScreenContract.CollectionsState.TreeItem

@Composable
internal fun FolderItem(
    item: TreeItem.Folder,
    isDropTarget: Boolean,
    onToggle: () -> Unit,
    onNewSubfolder: () -> Unit,
    onDelete: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val indent = (item.depth * 16).dp
    val lineColor = MaterialTheme.colorScheme.outlineVariant
    val dropHighlight = MaterialTheme.colorScheme.primary

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                // Vertical indent guide lines for each ancestor level
                for (level in 0 until item.depth) {
                    val x = 8.dp.toPx() + level * 16.dp.toPx() + 9.dp.toPx()
                    drawLine(
                        lineColor,
                        Offset(x, 0f),
                        Offset(x, size.height),
                        strokeWidth = 1.5.dp.toPx()
                    )
                }
            }
            .then(
                if (isDropTarget) Modifier.border(
                    1.dp,
                    dropHighlight,
                    MaterialTheme.shapes.extraSmall
                ) else Modifier
            )
            .then(if (isDropTarget) Modifier.background(dropHighlight.copy(alpha = 0.12f)) else Modifier)
            .clickable(onClick = onToggle)
            .padding(start = 8.dp + indent, end = 4.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(
            imageVector = if (item.isExpanded) Icons.Default.ArrowDropDown else Icons.Default.ArrowRight,
            contentDescription = if (item.isExpanded) "Collapse" else "Expand",
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Icon(
            imageVector = if (item.isExpanded) Icons.Default.FolderOpen else Icons.Default.Folder,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Text(
            text = item.folder.name,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Box {
            IconButton(
                onClick = { menuExpanded = true },
                modifier = Modifier.size(28.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Folder options",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false },
            ) {
                DropdownMenuItem(
                    text = { Text("New subfolder") },
                    onClick = { menuExpanded = false; onNewSubfolder() },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                )

                DropdownMenuItem(
                    text = {
                        Text(
                            text = "Delete",
                            color = MaterialTheme.colorScheme.error,
                        )
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                    },
                    onClick = {
                        menuExpanded = false
                        onDelete()
                    },
                )
            }
        }
    }
}
