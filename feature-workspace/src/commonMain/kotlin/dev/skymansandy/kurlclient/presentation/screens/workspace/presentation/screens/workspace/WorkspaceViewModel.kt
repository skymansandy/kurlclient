package dev.skymansandy.kurlclient.presentation.screens.workspace.presentation.screens.workspace

import androidx.lifecycle.viewModelScope
import dev.skymansandy.kurl.core.api.KurlEngine
import dev.skymansandy.kurl.core.model.HttpMethod
import dev.skymansandy.kurl.core.model.KeyValueEntry
import dev.skymansandy.kurl.core.model.KurlRequest
import dev.skymansandy.kurl.core.utils.buildCurlCommand
import dev.skymansandy.kurl.core.utils.deserializeKeyValueEntries
import dev.skymansandy.kurl.core.utils.parseCurlCommand
import dev.skymansandy.kurl.core.utils.serialize
import dev.skymansandy.kurl.store.CollectionStore
import kotlinx.coroutines.flow.combine
import dev.skymansandy.kurlclient.presentation.base.MviViewModel
import dev.skymansandy.kurlstore.db.SavedRequest
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
internal class WorkspaceViewModel(
    private val engine: KurlEngine,
    private val store: CollectionStore,
) : MviViewModel<WorkspaceState, WorkspaceEvent, WorkspaceEffect>() {

    private var nextId = 1L

    override fun createInitialState() = WorkspaceState()

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

    override fun onEvent(event: WorkspaceEvent) {
        when (event) {
            is WorkspaceEvent.SetUrl -> setState {
                copy(
                    url = event.value,
                    hasUnsavedChanges = true
                )
            }

            is WorkspaceEvent.SetMethod -> setState {
                copy(
                    method = event.value,
                    hasUnsavedChanges = true
                )
            }

            is WorkspaceEvent.SetBody -> setState {
                copy(
                    body = event.value,
                    hasUnsavedChanges = true
                )
            }

            is WorkspaceEvent.UpdateParam -> setState {
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

            is WorkspaceEvent.AddParam -> setState {
                copy(
                    params = params + KeyValueEntry(id = nextId++),
                    hasUnsavedChanges = true
                )
            }

            is WorkspaceEvent.RemoveParam -> setState {
                copy(
                    params = params.filter { it.id != event.id },
                    hasUnsavedChanges = true
                )
            }

            is WorkspaceEvent.UpdateHeader -> setState {
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

            is WorkspaceEvent.AddHeader -> setState {
                copy(
                    headers = headers + KeyValueEntry(id = nextId++),
                    hasUnsavedChanges = true
                )
            }

            is WorkspaceEvent.RemoveHeader -> setState {
                copy(
                    headers = headers.filter { it.id != event.id },
                    hasUnsavedChanges = true
                )
            }

            is WorkspaceEvent.SaveRequest -> saveRequest(event.name, event.folderId)
            is WorkspaceEvent.CreateFolder -> viewModelScope.launch { store.createFolder(event.name, event.parentId) }
            is WorkspaceEvent.ClearSaveSuccess -> setState { copy(saveSuccess = false) }
            is WorkspaceEvent.OverwriteLoadedRequest -> overwriteLoadedRequest()
            is WorkspaceEvent.ClearOverwriteSuccess -> setState { copy(overwriteSuccess = false) }
            is WorkspaceEvent.LoadSavedRequest -> loadSavedRequest(event.saved)
            is WorkspaceEvent.SendRequest -> sendRequest()
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
        viewModelScope.launch {
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
            setState { copy(isLoading = true, error = null) }
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
                        response = WorkspaceState.ResponseState(
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