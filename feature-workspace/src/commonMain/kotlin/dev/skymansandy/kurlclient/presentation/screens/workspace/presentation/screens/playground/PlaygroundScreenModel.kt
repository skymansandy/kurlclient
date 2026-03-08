package dev.skymansandy.kurlclient.presentation.screens.workspace.presentation.screens.playground

import androidx.lifecycle.viewModelScope
import dev.skymansandy.kurl.core.api.KurlEngine
import dev.skymansandy.kurl.core.model.HttpMethod
import dev.skymansandy.kurl.core.model.KeyValueEntry
import dev.skymansandy.kurl.core.model.KurlRequest
import dev.skymansandy.kurlclient.presentation.screens.workspace.util.curlparser.buildCurlCommand
import dev.skymansandy.kurl.core.utils.deserializeKeyValueEntries
import dev.skymansandy.kurlclient.presentation.screens.workspace.util.curlparser.parseCurlCommand
import dev.skymansandy.kurl.core.utils.serialize
import dev.skymansandy.kurl.store.api.KurlStore
import dev.skymansandy.kurlclient.presentation.base.MviViewModel
import dev.skymansandy.kurlstore.db.SavedRequest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
internal class PlaygroundScreenModel(
    private val engine: KurlEngine,
    private val store: KurlStore,
) : MviViewModel<PlaygroundState, PlaygroundEvent, PlaygroundEffect>() {

    private var nextId = 1L

    override fun createInitialState() = PlaygroundState()

    init {
        setState {
            copy(
                params = listOf(KeyValueEntry(id = nextId++)),
                headers = listOf(KeyValueEntry(id = nextId++))
            )
        }
        viewModelScope.launch {
            combine(store.folders, store.folderPaths) { folders, paths ->
                folders to paths
            }.collect { (folders, paths) ->
                setState { copy(allFolders = folders, folderPaths = paths) }
            }
        }
    }

    override fun onEvent(event: PlaygroundEvent) {
        when (event) {
            is PlaygroundEvent.SetUrl -> setState {
                copy(
                    url = event.value,
                    hasUnsavedChanges = true
                )
            }

            is PlaygroundEvent.SetMethod -> setState {
                copy(
                    method = event.value,
                    hasUnsavedChanges = true
                )
            }

            is PlaygroundEvent.SetBody -> setState {
                copy(
                    body = event.value,
                    hasUnsavedChanges = true
                )
            }

            is PlaygroundEvent.UpdateParam -> setState {
                copy(
                    params = params.map {
                        if (it.id == event.id) it.copy(
                            key = event.key,
                            value = event.value,
                            enabled = event.enabled
                        ) else it
                    },
                    hasUnsavedChanges = true
                )
            }

            is PlaygroundEvent.AddParam -> setState {
                copy(
                    params = params + KeyValueEntry(id = nextId++),
                    hasUnsavedChanges = true
                )
            }

            is PlaygroundEvent.RemoveParam -> setState {
                copy(
                    params = params.filter { it.id != event.id },
                    hasUnsavedChanges = true
                )
            }

            is PlaygroundEvent.UpdateHeader -> setState {
                copy(
                    headers = headers.map {
                        if (it.id == event.id) it.copy(
                            key = event.key,
                            value = event.value,
                            enabled = event.enabled
                        ) else it
                    },
                    hasUnsavedChanges = true
                )
            }

            is PlaygroundEvent.AddHeader -> setState {
                copy(
                    headers = headers + KeyValueEntry(id = nextId++),
                    hasUnsavedChanges = true
                )
            }

            is PlaygroundEvent.RemoveHeader -> setState {
                copy(
                    headers = headers.filter { it.id != event.id },
                    hasUnsavedChanges = true
                )
            }

            is PlaygroundEvent.SaveRequest -> saveRequest(event.name, event.folderId)
            is PlaygroundEvent.CreateFolder -> viewModelScope.launch { store.createFolder(event.name, event.parentId) }
            is PlaygroundEvent.ClearSaveSuccess -> setState { copy(saveSuccess = false) }
            is PlaygroundEvent.OverwriteLoadedRequest -> overwriteLoadedRequest()
            is PlaygroundEvent.ClearOverwriteSuccess -> setState { copy(overwriteSuccess = false) }
            is PlaygroundEvent.LoadSavedRequest -> loadSavedRequest(event.saved)
            is PlaygroundEvent.SendRequest -> sendRequest()
            PlaygroundEvent.ShowSaveDialog -> setState { copy(showSaveDialog = true) }
            PlaygroundEvent.HideSaveDialog -> setState { copy(showSaveDialog = false) }
            PlaygroundEvent.ShowImportCurlDialog -> setState { copy(showImportCurlDialog = true) }
            PlaygroundEvent.HideImportCurlDialog -> setState { copy(showImportCurlDialog = false) }
            is PlaygroundEvent.SelectTab -> setState { copy(activeTab = event.index) }
        }
    }

    // Pure query — not a state mutation, so kept as a regular function
    fun buildCurlCommand(): String {
        val s = state.value
        return buildCurlCommand(s.url, s.method, s.headers, s.params, s.body)
    }

    // Returns true on success, false if the command couldn't be parsed
    fun importFromCurl(curlCommand: String): Boolean {
        val parsed = parseCurlCommand(curlCommand) ?: return false
        setState {
            val h = parsed.headers
                .map { (k, v) -> KeyValueEntry(id = nextId++, key = k, value = v) }
                .ifEmpty { listOf(KeyValueEntry(id = nextId++)) }
            val p = parsed.params
                .map { (k, v) -> KeyValueEntry(id = nextId++, key = k, value = v) }
                .ifEmpty { listOf(KeyValueEntry(id = nextId++)) }
            copy(
                url = parsed.url,
                method = runCatching { HttpMethod.valueOf(parsed.method) }.getOrDefault(HttpMethod.GET),
                headers = h,
                params = p,
                body = parsed.body ?: "",
                response = null,
                error = null
            )
        }
        return true
    }

    private fun saveRequest(name: String, folderId: Long?) {
        val s = state.value
        val loaded = s.loadedRequest
        viewModelScope.launch {
            if (loaded != null) {
                store.updateRequest(
                    id = loaded.id,
                    name = name,
                    folderId = folderId,
                    url = s.url,
                    method = s.method.name,
                    headers = s.headers.serialize(),
                    params = s.params.serialize(),
                    body = s.body
                )
                setState { copy(loadedRequest = loaded.copy(name = name, folder_id = folderId), hasUnsavedChanges = false, overwriteSuccess = true) }
            } else {
                store.saveRequest(
                    name = name,
                    folderId = folderId,
                    url = s.url,
                    method = s.method.name,
                    headers = s.headers.serialize(),
                    params = s.params.serialize(),
                    body = s.body
                )
                setState { copy(hasUnsavedChanges = false, saveSuccess = true) }
            }
        }
    }

    private fun overwriteLoadedRequest() {
        val s = state.value
        val loaded = s.loadedRequest ?: return
        viewModelScope.launch {
            store.updateRequest(
                id = loaded.id,
                name = loaded.name,
                folderId = loaded.folder_id,
                url = s.url,
                method = s.method.name,
                headers = s.headers.serialize(),
                params = s.params.serialize(),
                body = s.body
            )
            setState { copy(hasUnsavedChanges = false, overwriteSuccess = true) }
        }
    }

    private fun loadSavedRequest(saved: SavedRequest) {
        val (h, idAfterHeaders) = saved.headers.deserializeKeyValueEntries(nextId)
        val (p, idAfterParams) = saved.params.deserializeKeyValueEntries(idAfterHeaders)
        nextId = idAfterParams
        setState {
            copy(
                loadedRequest = saved,
                url = saved.url,
                method = runCatching { HttpMethod.valueOf(saved.method) }.getOrDefault(HttpMethod.GET),
                headers = h.ifEmpty { listOf(KeyValueEntry(id = nextId++)) },
                params = p.ifEmpty { listOf(KeyValueEntry(id = nextId++)) },
                body = saved.body,
                response = null,
                error = null,
                hasUnsavedChanges = false
            )
        }
    }

    private fun sendRequest() {
        val s = state.value
        if (s.url.isBlank()) return
        val url = if (!s.url.startsWith("http://", ignoreCase = true) && !s.url.startsWith(
                "https://",
                ignoreCase = true
            )
        ) {
            "https://${s.url}".also { setState { copy(url = it) } }
        } else {
            s.url
        }
        viewModelScope.launch {
            setState { copy(isLoading = true, error = null, activeTab = 1) }
            try {
                val kurlResponse = engine.execute(
                    KurlRequest(
                        url = url,
                        method = s.method.name,
                        headers = s.headers
                            .filter { it.enabled && it.key.isNotBlank() }
                            .associate { it.key to it.value },
                        queryParams = s.params
                            .filter { it.enabled && it.key.isNotBlank() }
                            .associate { it.key to it.value },
                        body = s.body.ifBlank { null }
                    )
                )
                setState {
                    copy(
                        response = PlaygroundState.ResponseState(
                            statusCode = kurlResponse.statusCode,
                            statusText = kurlResponse.statusText,
                            timeMs = kurlResponse.timeMs,
                            sizeBytes = kurlResponse.sizeBytes,
                            body = kurlResponse.body,
                            headers = kurlResponse.headers,
                            networkInfo = kurlResponse.networkInfo
                        )
                    )
                }
            } catch (e: Exception) {
                setState { copy(error = e.message ?: "Request failed") }
            } finally {
                setState { copy(isLoading = false) }
            }
        }
    }
}