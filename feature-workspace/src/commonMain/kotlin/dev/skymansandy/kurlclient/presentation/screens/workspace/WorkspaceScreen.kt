package dev.skymansandy.kurlclient.presentation.screens.workspace

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
import dev.skymansandy.kurlclient.presentation.screens.workspace.request.ImportCurlDialog
import dev.skymansandy.kurlclient.presentation.screens.workspace.request.RequestPanel
import dev.skymansandy.kurlclient.presentation.screens.workspace.request.SaveRequestDialog
import dev.skymansandy.kurlclient.presentation.screens.workspace.request.UrlBar
import dev.skymansandy.kurlclient.presentation.screens.workspace.response.ResponsePanel
import dev.skymansandy.kurlstore.db.CollectionFolder
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun WorkspaceScreen(
    vm: RequestViewModel = koinViewModel(),
    allFolders: List<CollectionFolder>,
    folderPaths: Map<Long, String>,
    onSaveSuccess: () -> Unit,
    onOverwriteSuccess: () -> Unit,
    onCreateFolder: (name: String, parentId: Long?) -> Unit,
    onShowSnackbar: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showSaveDialog by remember { mutableStateOf(false) }
    var showImportCurlDialog by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(0) }
    val clipboard = LocalClipboardManager.current

    LaunchedEffect(vm.saveSuccess) {
        if (vm.saveSuccess) {
            onSaveSuccess()
            vm.clearSaveSuccess()
        }
    }

    LaunchedEffect(vm.overwriteSuccess) {
        if (vm.overwriteSuccess) {
            onOverwriteSuccess()
            vm.clearOverwriteSuccess()
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
            initialName = if (vm.url.isNotBlank()) vm.url.substringAfterLast("/").take(40) else "Untitled",
            folders = allFolders,
            folderPaths = folderPaths,
            onSave = { name, folderId ->
                vm.saveRequest(name, folderId)
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
            method = vm.method,
            url = vm.url,
            isLoading = vm.isLoading,
            onMethodChange = vm::setRequestMethod,
            onUrlChange = vm::setRequestUrl,
            onSend = { vm.sendRequest(); selectedTab = 1 },
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
                url = vm.url,
                method = vm.method,
                params = vm.params,
                headers = vm.headers,
                body = vm.body,
                isLoading = vm.isLoading,
                onUrlChange = vm::setRequestUrl,
                onMethodChange = vm::setRequestMethod,
                onParamUpdate = vm::updateParam,
                onParamAdd = vm::addParam,
                onParamRemove = vm::removeParam,
                onHeaderUpdate = vm::updateHeader,
                onHeaderAdd = vm::addHeader,
                onHeaderRemove = vm::removeHeader,
                onBodyChange = vm::setRequestBody,
                onSend = { vm.sendRequest(); selectedTab = 1 },
                onSave = { showSaveDialog = true },
                onCopyCurl = onCopyCurl,
                onImportCurl = { showImportCurlDialog = true },
                showUrlBar = false,
                modifier = Modifier.weight(1f).fillMaxWidth()
            )
            else -> ResponsePanel(
                response = vm.response,
                error = vm.error,
                modifier = Modifier.weight(1f).fillMaxWidth()
            )
        }
    }
}