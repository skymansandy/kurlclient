package dev.skymansandy.kurlclient.presentation.screens.playground.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.SaveAs
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalIconButton
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
import kurlclient.feature_workspace.generated.resources.Res
import kurlclient.feature_workspace.generated.resources.action_copy_curl
import kurlclient.feature_workspace.generated.resources.action_delete_request
import kurlclient.feature_workspace.generated.resources.action_import_curl
import kurlclient.feature_workspace.generated.resources.cd_close_playground
import kurlclient.feature_workspace.generated.resources.cd_more_actions
import kurlclient.feature_workspace.generated.resources.cd_save_collection
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun PlaygroundToolbarActions(
    modifier: Modifier = Modifier,
    isNewRequest: Boolean = false,
    showSave: Boolean,
    isLoading: Boolean,
    onSave: () -> Unit,
    onDelete: () -> Unit,
    onClose: () -> Unit,
    onImportCurl: () -> Unit,
    onCopyCurl: () -> Unit,
) {
    var moreMenuExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (showSave) {
            FilledTonalIconButton(
                onClick = onSave,
                enabled = !isLoading,
            ) {
                Icon(
                    imageVector = if (isNewRequest) Icons.Default.SaveAs else Icons.Default.Save,
                    contentDescription = stringResource(Res.string.cd_save_collection),
                )
            }
        }

        // ⋮ More menu: cURL import / export / delete
        Box(
            modifier = Modifier.wrapContentSize(Alignment.TopStart),
        ) {
            IconButton(
                onClick = {
                    moreMenuExpanded = true
                },
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = stringResource(Res.string.cd_more_actions),
                )
            }

            DropdownMenu(
                expanded = moreMenuExpanded,
                onDismissRequest = { moreMenuExpanded = false },
            ) {
                DropdownMenuItem(
                    text = { Text(stringResource(Res.string.action_import_curl)) },
                    onClick = {
                        moreMenuExpanded = false
                        onImportCurl()
                    },
                )
                DropdownMenuItem(
                    text = { Text(stringResource(Res.string.action_copy_curl)) },
                    onClick = {
                        moreMenuExpanded = false
                        onCopyCurl()
                    },
                )
                if (!isNewRequest) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = stringResource(Res.string.action_delete_request),
                                color = MaterialTheme.colorScheme.error,
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                            )
                        },
                        onClick = {
                            moreMenuExpanded = false
                            onDelete()
                        },
                    )
                }
            }
        }

        IconButton(onClick = onClose) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = stringResource(Res.string.cd_close_playground),
            )
        }
    }
}
