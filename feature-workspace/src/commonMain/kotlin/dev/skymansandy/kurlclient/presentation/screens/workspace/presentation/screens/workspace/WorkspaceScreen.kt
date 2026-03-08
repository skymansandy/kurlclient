package dev.skymansandy.kurlclient.presentation.screens.workspace.presentation.screens.workspace

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.skymansandy.kurlclient.presentation.screens.workspace.request.ImportCurlDialog
import dev.skymansandy.kurlclient.presentation.screens.workspace.request.RequestPanel
import dev.skymansandy.kurlclient.presentation.screens.workspace.request.SaveRequestDialog
import dev.skymansandy.kurlclient.presentation.screens.workspace.request.UrlBar
import dev.skymansandy.kurlclient.presentation.screens.workspace.response.ResponsePanel
import dev.skymansandy.kurlstore.db.CollectionFolder
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun WorkspaceScreen(
    allFolders: List<CollectionFolder>,
    folderPaths: Map<Long, String>,
    onSaveSuccess: () -> Unit,
    onOverwriteSuccess: () -> Unit,
    onCreateFolder: (name: String, parentId: Long?) -> Unit,
    onShowSnackbar: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val vm: WorkspaceViewModel = koinViewModel()

    val state by vm.state.collectAsStateWithLifecycle()

    var showSaveDialog by remember { mutableStateOf(false) }
    var showImportCurlDialog by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(0) }
    val clipboard = LocalClipboardManager.current

    LaunchedEffect(state.saveSuccess) {
        if (state.saveSuccess) {
            onSaveSuccess()
            vm.onEvent(WorkspaceEvent.ClearSaveSuccess)
        }
    }

    LaunchedEffect(state.overwriteSuccess) {
        if (state.overwriteSuccess) {
            onOverwriteSuccess()
            vm.onEvent(WorkspaceEvent.ClearOverwriteSuccess)
        }
    }

    if (showImportCurlDialog) {
        ImportCurlDialog(
            onImport = { curlText ->
                vm.importFromCurl(curlText).also { if (it) showImportCurlDialog = false }
            },
            onDismiss = { showImportCurlDialog = false }
        )
    }

    if (showSaveDialog) {
        SaveRequestDialog(
            initialName = if (state.url.isNotBlank()) state.url.substringAfterLast("/").take(40) else "Untitled",
            folders = allFolders,
            folderPaths = folderPaths,
            onSave = { name, folderId ->
                vm.onEvent(WorkspaceEvent.SaveRequest(name, folderId))
                showSaveDialog = false
            },
            onCreateFolder = onCreateFolder,
            onDismiss = { showSaveDialog = false }
        )
    }

    val onCopyCurl: () -> Unit = {
        clipboard.setText(AnnotatedString(vm.buildCurlCommand()))
        onShowSnackbar("Copied as cURL")
    }

    Column(modifier = modifier) {
        UrlBar(
            method = state.method,
            url = state.url,
            isLoading = state.isLoading,
            onMethodChange = { vm.onEvent(WorkspaceEvent.SetMethod(it)) },
            onUrlChange = { vm.onEvent(WorkspaceEvent.SetUrl(it)) },
            onSend = { vm.onEvent(WorkspaceEvent.SendRequest); selectedTab = 1 },
            onSave = { showSaveDialog = true },
            onCopyCurl = onCopyCurl,
            onImportCurl = { showImportCurlDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp)
        )
        TabRow(selectedTabIndex = selectedTab) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 },
                text = { Text("Request") })
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 },
                text = { Text("Response") })
        }
        when (selectedTab) {
            0 -> RequestPanel(
                url = state.url,
                method = state.method,
                params = state.params,
                headers = state.headers,
                body = state.body,
                isLoading = state.isLoading,
                onUrlChange = { vm.onEvent(WorkspaceEvent.SetUrl(it)) },
                onMethodChange = { vm.onEvent(WorkspaceEvent.SetMethod(it)) },
                onParamUpdate = { id, key, value, enabled -> vm.onEvent(WorkspaceEvent.UpdateParam(id, key, value, enabled)) },
                onParamAdd = { vm.onEvent(WorkspaceEvent.AddParam) },
                onParamRemove = { vm.onEvent(WorkspaceEvent.RemoveParam(it)) },
                onHeaderUpdate = { id, key, value, enabled -> vm.onEvent(WorkspaceEvent.UpdateHeader(id, key, value, enabled)) },
                onHeaderAdd = { vm.onEvent(WorkspaceEvent.AddHeader) },
                onHeaderRemove = { vm.onEvent(WorkspaceEvent.RemoveHeader(it)) },
                onBodyChange = { vm.onEvent(WorkspaceEvent.SetBody(it)) },
                onSend = { vm.onEvent(WorkspaceEvent.SendRequest); selectedTab = 1 },
                onSave = { showSaveDialog = true },
                onCopyCurl = onCopyCurl,
                onImportCurl = { showImportCurlDialog = true },
                showUrlBar = false,
                modifier = Modifier.weight(1f).fillMaxWidth()
            )
            else -> ResponsePanel(
                response = state.response,
                error = state.error,
                modifier = Modifier.weight(1f).fillMaxWidth()
            )
        }
    }
}