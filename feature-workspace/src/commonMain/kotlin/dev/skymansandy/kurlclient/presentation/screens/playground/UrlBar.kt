package dev.skymansandy.kurlclient.presentation.screens.playground

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.SaveAs
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import dev.skymansandy.kurl.core.model.HttpMethod
import dev.skymansandy.kurlclient.util.compose.methodColor
import kurlclient.feature_workspace.generated.resources.Res
import kurlclient.feature_workspace.generated.resources.action_copy_curl
import kurlclient.feature_workspace.generated.resources.action_delete_request
import kurlclient.feature_workspace.generated.resources.action_import_curl
import kurlclient.feature_workspace.generated.resources.cd_close_playground
import kurlclient.feature_workspace.generated.resources.cd_more_actions
import kurlclient.feature_workspace.generated.resources.cd_save_collection
import kurlclient.feature_workspace.generated.resources.placeholder_url
import kurlclient.feature_workspace.generated.resources.send
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun UrlBar(
    method: HttpMethod,
    url: String,
    isLoading: Boolean,
    isNewRequest: Boolean,
    hasUnsavedChanges: Boolean,
    onMethodChange: (HttpMethod) -> Unit,
    onUrlChange: (String) -> Unit,
    onSend: () -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit,
    onClose: () -> Unit,
    onCopyCurl: () -> Unit,
    onImportCurl: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    var moreMenuExpanded by remember { mutableStateOf(false) }

    val showSave = isNewRequest || hasUnsavedChanges

    Row(
        modifier = modifier.height(48.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier.wrapContentSize(Alignment.TopStart),
        ) {
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = methodColor(method),
                modifier = Modifier.clip(RoundedCornerShape(6.dp)),
            ) {
                TextButton(onClick = { expanded = true }) {
                    Text(
                        text = method.name,
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                HttpMethod.entries.forEach { m ->
                    DropdownMenuItem(
                        text = { Text(m.name) },
                        onClick = { onMethodChange(m); expanded = false },
                    )
                }
            }
        }

        BasicTextField(
            value = url,
            onValueChange = onUrlChange,
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurface,
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            decorationBox = { inner ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(6.dp))
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    if (url.isEmpty()) {
                        Text(
                            stringResource(Res.string.placeholder_url),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    inner()
                }
            },
            modifier = Modifier.weight(1f).fillMaxSize(),
        )

        Button(
            onClick = onSend,
            enabled = !isLoading,
            shape = RoundedCornerShape(6.dp),
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            } else {
                Text(stringResource(Res.string.send))
            }
        }

        Row(
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
                        Icons.Default.MoreVert,
                        contentDescription = stringResource(Res.string.cd_more_actions),
                    )
                }

                DropdownMenu(
                    expanded = moreMenuExpanded,
                    onDismissRequest = { moreMenuExpanded = false },
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(Res.string.action_import_curl)) },
                        onClick = { moreMenuExpanded = false; onImportCurl() },
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(Res.string.action_copy_curl)) },
                        onClick = { moreMenuExpanded = false; onCopyCurl() },
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
                            onClick = { moreMenuExpanded = false; onDelete() },
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
}
