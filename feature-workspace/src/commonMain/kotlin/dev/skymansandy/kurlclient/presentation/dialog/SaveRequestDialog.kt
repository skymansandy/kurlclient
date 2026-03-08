package dev.skymansandy.kurlclient.presentation.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import dev.skymansandy.kurlstore.db.CollectionFolder
import kurlclient.feature_workspace.generated.resources.Res
import kurlclient.feature_workspace.generated.resources.action_new_folder
import kurlclient.feature_workspace.generated.resources.cancel
import kurlclient.feature_workspace.generated.resources.create
import kurlclient.feature_workspace.generated.resources.hint_folder_name
import kurlclient.feature_workspace.generated.resources.hint_request_name
import kurlclient.feature_workspace.generated.resources.label_new_folder
import kurlclient.feature_workspace.generated.resources.label_save_in_folder
import kurlclient.feature_workspace.generated.resources.no_folder_root
import kurlclient.feature_workspace.generated.resources.no_parent_root
import kurlclient.feature_workspace.generated.resources.save
import kurlclient.feature_workspace.generated.resources.title_save_request
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun SaveRequestDialog(
    initialName: String,
    initialFolderId: Long? = null,
    folders: List<CollectionFolder>,
    folderPaths: Map<Long, String>,
    onSave: (name: String, folderId: Long?) -> Unit,
    onCreateFolder: (name: String, parentId: Long?) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by remember { mutableStateOf(initialName) }
    var selectedFolderId by remember { mutableStateOf(initialFolderId) }
    var folderDropdownExpanded by remember { mutableStateOf(false) }

    var showNewFolderRow by remember { mutableStateOf(false) }
    var newFolderName by remember { mutableStateOf("") }
    var newFolderParentDropdownExpanded by remember { mutableStateOf(false) }
    var newFolderParentId by remember { mutableStateOf<Long?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
    ) {
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

                // Folder picker
                Column {
                    Text(
                        text = stringResource(Res.string.label_save_in_folder),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    Spacer(Modifier.height(4.dp))

                    OutlinedButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { folderDropdownExpanded = true },
                    ) {
                        Text(
                            text = selectedFolderId?.let { folderPaths[it] }
                                ?: stringResource(Res.string.no_folder_root),
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyMedium,
                        )

                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null,
                        )
                    }

                    DropdownMenu(
                        expanded = folderDropdownExpanded,
                        onDismissRequest = {
                            folderDropdownExpanded = false
                        },
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(Res.string.no_folder_root)) },
                            onClick = {
                                selectedFolderId = null
                                folderDropdownExpanded = false
                            },
                        )

                        folders.forEach { folder ->
                            DropdownMenuItem(
                                text = { Text(folderPaths[folder.id] ?: folder.name) },
                                onClick = {
                                    selectedFolderId = folder.id
                                    folderDropdownExpanded = false
                                },
                            )
                        }
                    }
                }

                // New folder section
                if (showNewFolderRow) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = stringResource(Res.string.label_new_folder),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )

                        OutlinedTextField(
                            value = newFolderName,
                            onValueChange = { newFolderName = it },
                            label = { Text(stringResource(Res.string.hint_folder_name)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )

                        // Parent folder for new folder
                        OutlinedButton(
                            onClick = { newFolderParentDropdownExpanded = true },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(
                                text = newFolderParentId?.let { folderPaths[it] }
                                    ?: stringResource(Res.string.no_parent_root),
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodyMedium,
                            )

                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                            )
                        }
                        DropdownMenu(
                            expanded = newFolderParentDropdownExpanded,
                            onDismissRequest = { newFolderParentDropdownExpanded = false },
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(Res.string.no_parent_root)) },
                                onClick = {
                                    newFolderParentId = null
                                    newFolderParentDropdownExpanded = false
                                },
                            )
                            folders.forEach { folder ->
                                DropdownMenuItem(
                                    text = { Text(folderPaths[folder.id] ?: folder.name) },
                                    onClick = {
                                        newFolderParentId =
                                            folder.id; newFolderParentDropdownExpanded = false
                                    },
                                )
                            }
                        }
                        Row(
                            horizontalArrangement = Arrangement.End,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            TextButton(
                                onClick = {
                                    showNewFolderRow = false
                                    newFolderName = ""
                                },
                            ) {
                                Text(stringResource(Res.string.cancel))
                            }

                            Spacer(Modifier.width(8.dp))

                            Button(
                                enabled = newFolderName.isNotBlank(),
                                onClick = {
                                    if (newFolderName.isNotBlank()) {
                                        onCreateFolder(newFolderName, newFolderParentId)
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
                    TextButton(
                        onClick = { showNewFolderRow = true },
                    ) {
                        Text(stringResource(Res.string.action_new_folder))
                    }
                }

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(Res.string.cancel))
                    }

                    Spacer(Modifier.width(8.dp))

                    Button(
                        onClick = { onSave(name, selectedFolderId) },
                        enabled = name.isNotBlank(),
                    ) {
                        Text(stringResource(Res.string.save))
                    }
                }
            }
        }
    }
}
