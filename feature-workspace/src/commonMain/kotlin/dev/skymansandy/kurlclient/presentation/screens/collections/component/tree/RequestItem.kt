package dev.skymansandy.kurlclient.presentation.screens.collections.component.tree

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import dev.skymansandy.kurl.core.model.HttpMethod
import dev.skymansandy.kurl.core.utils.formatRelativeTime
import dev.skymansandy.kurlclient.presentation.screens.collections.CollectionsScreenContract.CollectionsState.TreeItem

@Composable
internal fun RequestItem(
    item: TreeItem.Request,
    isActive: Boolean,
    highlightQuery: String = "",
    onLoad: () -> Unit,
    onSaveChanges: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val indent = (item.depth * 16).dp
    val request = item.request
    val lineColor = MaterialTheme.colorScheme.outlineVariant

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
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
                if (isActive) Modifier.background(
                    MaterialTheme.colorScheme.primaryContainer.copy(
                        alpha = 0.3f
                    )
                )
                else Modifier
            )
            .clickable(onClick = onLoad)
            .padding(start = 32.dp + indent, end = 4.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Surface(
            color = methodColor(request.method),
            shape = MaterialTheme.shapes.extraSmall,
        ) {
            Text(
                text = request.method,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
            )
        }
        Column(
            modifier = Modifier.weight(1f),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                HighlightedText(
                    text = request.name,
                    query = highlightQuery,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.weight(1f, fill = false),
                )
                Text(
                    text = formatRelativeTime(request.created_at),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = request.url,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Box {
            IconButton(
                onClick = { menuExpanded = true },
                modifier = Modifier.size(28.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Request options",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false },
            ) {
                DropdownMenuItem(
                    text = { Text("Load") },
                    onClick = {
                        menuExpanded = false
                        onLoad()
                    }
                )

                if (isActive) {
                    DropdownMenuItem(
                        text = { Text("Save changes") },
                        onClick = {
                            menuExpanded = false
                            onSaveChanges()
                        }
                    )
                }

                DropdownMenuItem(
                    text = { Text("Duplicate") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.ContentCopy,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                        )
                    },
                    onClick = {
                        menuExpanded = false
                        onDuplicate()
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
                            tint = MaterialTheme.colorScheme.error,
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

@Composable
private fun methodColor(method: String) = when (method) {
    HttpMethod.GET.name -> MaterialTheme.colorScheme.primary
    HttpMethod.POST.name -> MaterialTheme.colorScheme.secondary
    HttpMethod.PUT.name -> MaterialTheme.colorScheme.tertiary
    HttpMethod.DELETE.name -> MaterialTheme.colorScheme.error
    HttpMethod.PATCH.name -> MaterialTheme.colorScheme.secondary
    else -> MaterialTheme.colorScheme.primary
}
