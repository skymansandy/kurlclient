package dev.skymansandy.kurlclient.presentation.screens.playground

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.skymansandy.kurlclient.presentation.dialog.ImportCurlDialog
import dev.skymansandy.kurlclient.presentation.dialog.SaveRequestDialog
import dev.skymansandy.kurlclient.presentation.screens.playground.PlaygroundScreenContract.PlaygroundEvent
import dev.skymansandy.kurlclient.presentation.screens.playground.request.RequestPanel
import dev.skymansandy.kurlclient.presentation.screens.playground.response.ResponsePanel
import kurlclient.feature_workspace.generated.resources.Res
import kurlclient.feature_workspace.generated.resources.msg_changes_saved
import kurlclient.feature_workspace.generated.resources.msg_copied_as_curl
import kurlclient.feature_workspace.generated.resources.msg_request_saved
import kurlclient.feature_workspace.generated.resources.tab_request
import kurlclient.feature_workspace.generated.resources.tab_response
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun PlaygroundScreen(
    onShowSnackbar: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val vm: PlaygroundScreenModel = koinViewModel()
    val state by vm.state.collectAsStateWithLifecycle()
    val clipboard = LocalClipboardManager.current

    val msgRequestSaved = stringResource(Res.string.msg_request_saved)
    val msgChangesSaved = stringResource(Res.string.msg_changes_saved)
    val msgCopiedAsCurl = stringResource(Res.string.msg_copied_as_curl)

    LaunchedEffect(state.saveSuccess) {
        if (state.saveSuccess) {
            onShowSnackbar(msgRequestSaved)
            vm.onEvent(PlaygroundEvent.ClearSaveSuccess)
        }
    }

    LaunchedEffect(state.overwriteSuccess) {
        if (state.overwriteSuccess) {
            onShowSnackbar(msgChangesSaved)
            vm.onEvent(PlaygroundEvent.ClearOverwriteSuccess)
        }
    }

    if (state.showImportCurlDialog) {
        ImportCurlDialog(
            onImport = { curlText ->
                vm.importFromCurl(curlText)
                    .also { if (it) vm.onEvent(PlaygroundEvent.HideImportCurlDialog) }
            },
            onDismiss = { vm.onEvent(PlaygroundEvent.HideImportCurlDialog) },
        )
    }

    if (state.showSaveDialog) {
        val loaded = state.loadedRequest
        SaveRequestDialog(
            initialName = loaded?.name
                ?: if (state.url.isNotBlank()) state.url.substringAfterLast("/")
                    .take(40) else "Untitled",
            initialFolderId = loaded?.folder_id,
            folders = state.allFolders,
            folderPaths = state.folderPaths,
            onSave = { name, folderId ->
                vm.onEvent(PlaygroundEvent.SaveRequest(name, folderId))
                vm.onEvent(PlaygroundEvent.HideSaveDialog)
            },
            onCreateFolder = { name, parentId ->
                vm.onEvent(PlaygroundEvent.CreateFolder(name, parentId))
            },
            onDismiss = { vm.onEvent(PlaygroundEvent.HideSaveDialog) },
        )
    }

    val onCopyCurl: () -> Unit = {
        clipboard.setText(AnnotatedString(vm.buildCurlCommand()))
        onShowSnackbar(msgCopiedAsCurl)
    }

    Column(modifier = modifier) {
        UrlBar(
            method = state.method,
            url = state.url,
            isLoading = state.isLoading,
            onMethodChange = { vm.onEvent(PlaygroundEvent.SetMethod(it)) },
            onUrlChange = { vm.onEvent(PlaygroundEvent.SetUrl(it)) },
            onSend = { vm.onEvent(PlaygroundEvent.SendRequest) },
            onSave = { vm.onEvent(PlaygroundEvent.ShowSaveDialog) },
            onCopyCurl = onCopyCurl,
            onImportCurl = { vm.onEvent(PlaygroundEvent.ShowImportCurlDialog) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
        )

        TabRow(
            selectedTabIndex = state.activeTab,
        ) {
            Tab(
                selected = state.activeTab == 0,
                onClick = { vm.onEvent(PlaygroundEvent.SelectTab(0)) },
                text = { Text(stringResource(Res.string.tab_request)) },
            )

            Tab(
                selected = state.activeTab == 1,
                onClick = { vm.onEvent(PlaygroundEvent.SelectTab(1)) },
                text = { Text(stringResource(Res.string.tab_response)) },
            )
        }

        when (state.activeTab) {
            0 -> RequestPanel(vm = vm, modifier = Modifier.weight(1f).fillMaxWidth())
            else -> ResponsePanel(vm = vm, modifier = Modifier.weight(1f).fillMaxWidth())
        }
    }
}
