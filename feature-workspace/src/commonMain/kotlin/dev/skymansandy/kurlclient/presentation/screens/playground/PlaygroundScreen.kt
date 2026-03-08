package dev.skymansandy.kurlclient.presentation.screens.playground

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.skymansandy.kurlclient.presentation.dialog.DiscardWorkspaceAlertDialog
import dev.skymansandy.kurlclient.presentation.dialog.ImportCurlDialog
import dev.skymansandy.kurlclient.presentation.dialog.SaveRequestDialog
import dev.skymansandy.kurlclient.presentation.screens.playground.PlaygroundScreenContract.PlaygroundEvent
import dev.skymansandy.kurlclient.presentation.screens.playground.PlaygroundScreenContract.PlaygroundState
import dev.skymansandy.kurlclient.presentation.screens.playground.panel.PlaygroundPanel
import kurlclient.feature_workspace.generated.resources.Res
import kurlclient.feature_workspace.generated.resources.action_new_request
import kurlclient.feature_workspace.generated.resources.msg_changes_saved
import kurlclient.feature_workspace.generated.resources.msg_copied_as_curl
import kurlclient.feature_workspace.generated.resources.msg_no_request_open
import kurlclient.feature_workspace.generated.resources.msg_no_request_subtitle
import kurlclient.feature_workspace.generated.resources.msg_request_saved
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun PlaygroundScreen(
    onShowSnackbar: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewmodel: PlaygroundScreenModel = koinViewModel()
    val state by viewmodel.state.collectAsStateWithLifecycle()
    val clipboard = LocalClipboardManager.current

    val msgRequestSaved = stringResource(Res.string.msg_request_saved)
    val msgChangesSaved = stringResource(Res.string.msg_changes_saved)
    val msgCopiedAsCurl = stringResource(Res.string.msg_copied_as_curl)

    LaunchedEffect(state.saveSuccess) {
        if (state.saveSuccess) {
            onShowSnackbar(msgRequestSaved)
            viewmodel.onEvent(PlaygroundEvent.ClearSaveSuccess)
        }
    }

    LaunchedEffect(state.overwriteSuccess) {
        if (state.overwriteSuccess) {
            onShowSnackbar(msgChangesSaved)
            viewmodel.onEvent(PlaygroundEvent.ClearOverwriteSuccess)
        }
    }

    PlaygroundDialogs(
        state = state,
        viewmodel = viewmodel,
    )

    if (state.showSaveDialog) {
        val loaded = state.loadedRequest
        SaveRequestDialog(
            initialName = loaded?.name
                ?: state.currentRequest.name.ifBlank {
                    if (state.currentRequest.url.isNotBlank()) state.currentRequest.url.substringAfterLast("/")
                        .take(40) else "Untitled"
                },
            initialFolderId = loaded?.folder_id,
            folders = state.allFolders,
            onSave = { name, folderId ->
                viewmodel.onEvent(PlaygroundEvent.SaveRequest(name, folderId))
                viewmodel.onEvent(PlaygroundEvent.HideSaveDialog)
            },
            onCreateFolder = { name, parentId ->
                viewmodel.onEvent(PlaygroundEvent.CreateFolder(name, parentId))
            },
            onDismiss = { viewmodel.onEvent(PlaygroundEvent.HideSaveDialog) },
        )
    }

    val showPlaceholder = !state.isEditingNewRequest && state.loadedRequest == null

    if (showPlaceholder) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = stringResource(Res.string.msg_no_request_open),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = stringResource(Res.string.msg_no_request_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Button(onClick = { viewmodel.onEvent(PlaygroundEvent.StartNewRequest) }) {
                    Text(stringResource(Res.string.action_new_request))
                }
            }
        }
    } else {
        val onCopyCurl: () -> Unit = {
            clipboard.setText(AnnotatedString(viewmodel.buildCurlCommand()))
            onShowSnackbar(msgCopiedAsCurl)
        }

        PlaygroundPanel(
            modifier = modifier,
            state = state,
            viewmodel = viewmodel,
            onCopyCurl = onCopyCurl,
        )
    }
}

@Composable
private fun PlaygroundDialogs(
    state: PlaygroundState,
    viewmodel: PlaygroundScreenModel,
) {
    if (state.showDiscardAndCloseDialog) {
        DiscardWorkspaceAlertDialog(
            onConfirm = { viewmodel.onEvent(PlaygroundEvent.ConfirmClose) },
            onDismiss = { viewmodel.onEvent(PlaygroundEvent.DismissCloseDialog) },
        )
    }

    if (state.showImportCurlDialog) {
        ImportCurlDialog(
            onImport = { curlText ->
                viewmodel.importFromCurl(curlText)
                    .also { if (it) viewmodel.onEvent(PlaygroundEvent.HideImportCurlDialog) }
            },
            onDismiss = { viewmodel.onEvent(PlaygroundEvent.HideImportCurlDialog) },
        )
    }
}
