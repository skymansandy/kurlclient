package dev.skymansandy.kurlclient.presentation.dialog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import dev.skymansandy.kurlstore.db.CollectionFolder
import kurlclient.feature_workspace.generated.resources.Res
import kurlclient.feature_workspace.generated.resources.action_new_folder
import kurlclient.feature_workspace.generated.resources.action_save_here
import kurlclient.feature_workspace.generated.resources.cancel
import kurlclient.feature_workspace.generated.resources.create
import kurlclient.feature_workspace.generated.resources.hint_folder_name
import kurlclient.feature_workspace.generated.resources.hint_request_name
import kurlclient.feature_workspace.generated.resources.label_root
import kurlclient.feature_workspace.generated.resources.label_save_in_folder
import kurlclient.feature_workspace.generated.resources.msg_no_subfolders
import kurlclient.feature_workspace.generated.resources.title_save_request
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun SaveRequestDialog(
    initialName: String,
    initialFolderId: Long? = null,
    folders: List<CollectionFolder>,
    onSave: (name: String, folderId: Long?) -> Unit,
    onCreateFolder: (name: String, parentId: Long?) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by remember { mutableStateOf(initialName) }
    var currentFolderId by remember { mutableStateOf(initialFolderId) }

    var showNewFolderRow by remember { mutableStateOf(false) }
    var newFolderName by remember { mutableStateOf("") }

    // Build breadcrumb: list of ancestors from root down to currentFolderId
    val breadcrumb = remember(currentFolderId, folders) {
        buildBreadcrumb(folders, currentFolderId)
    }

    // Folders visible at the current level
    val visibleFolders = remember(currentFolderId, folders) {
        folders.filter { it.parent_id == currentFolderId }
    }

    val rootLabel = stringResource(Res.string.label_root)

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.large,
            tonalElevation = 6.dp,
        ) {
            Column(
                modifier = Modifier.padding(24.dp).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = stringResource(Res.string.title_save_request),
                    style = MaterialTheme.typography.titleLarge,
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(Res.string.hint_request_name)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                // Folder browser section
                Column {
                    Text(
                        text = stringResource(Res.string.label_save_in_folder),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    Spacer(Modifier.height(6.dp))

                    // Breadcrumb
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        // Root segment
                        val isAtRoot = currentFolderId == null
                        Text(
                            text = rootLabel,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isAtRoot)
                                MaterialTheme.colorScheme.onSurface
                            else
                                MaterialTheme.colorScheme.primary,
                            modifier = if (!isAtRoot) Modifier.clickable { currentFolderId = null } else Modifier,
                        )
                        breadcrumb.forEachIndexed { index, folder ->
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            val isLast = index == breadcrumb.lastIndex
                            Text(
                                text = folder.name,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isLast)
                                    MaterialTheme.colorScheme.onSurface
                                else
                                    MaterialTheme.colorScheme.primary,
                                modifier = if (!isLast) Modifier.clickable { currentFolderId = folder.id } else Modifier,
                            )
                        }
                    }

                    Spacer(Modifier.height(4.dp))
                    HorizontalDivider()

                    // Folder list
                    if (visibleFolders.isEmpty()) {
                        Text(
                            text = stringResource(Res.string.msg_no_subfolders),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 8.dp),
                        )
                    } else {
                        LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
                            items(visibleFolders, key = { it.id }) { folder ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { currentFolderId = folder.id }
                                        .padding(vertical = 10.dp, horizontal = 4.dp),
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Folder,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp),
                                        tint = MaterialTheme.colorScheme.primary,
                                    )
                                    Text(
                                        text = folder.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.weight(1f),
                                    )
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                HorizontalDivider()
                            }
                        }
                    }

                    HorizontalDivider()
                }

                // New folder inline form
                if (showNewFolderRow) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = newFolderName,
                            onValueChange = { newFolderName = it },
                            label = { Text(stringResource(Res.string.hint_folder_name)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Row(
                            horizontalArrangement = Arrangement.End,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            TextButton(
                                onClick = { showNewFolderRow = false; newFolderName = "" },
                            ) {
                                Text(stringResource(Res.string.cancel))
                            }
                            Spacer(Modifier.width(8.dp))
                            Button(
                                enabled = newFolderName.isNotBlank(),
                                onClick = {
                                    if (newFolderName.isNotBlank()) {
                                        onCreateFolder(newFolderName, currentFolderId)
                                        showNewFolderRow = false
                                        newFolderName = ""
                                    }
                                },
                            ) {
                                Text(stringResource(Res.string.create))
                            }
                        }
                    }
                } else {
                    TextButton(onClick = { showNewFolderRow = true }) {
                        Text(stringResource(Res.string.action_new_folder))
                    }
                }

                // Save here button
                Button(
                    onClick = { onSave(name, currentFolderId) },
                    enabled = name.isNotBlank(),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(Res.string.action_save_here))
                }
            }
        }
    }
}

private fun buildBreadcrumb(
    folders: List<CollectionFolder>,
    currentFolderId: Long?,
): List<CollectionFolder> {
    if (currentFolderId == null) return emptyList()
    val result = mutableListOf<CollectionFolder>()
    var id: Long? = currentFolderId
    while (id != null) {
        val folder = folders.find { it.id == id } ?: break
        result.add(0, folder)
        id = folder.parent_id
    }
    return result
}
