package dev.skymansandy.kurlclient.presentation.screens.playground

import androidx.lifecycle.viewModelScope
import dev.skymansandy.kurl.core.api.KurlEngine
import dev.skymansandy.kurl.core.model.HttpMethod
import dev.skymansandy.kurl.core.model.KeyValueEntry
import dev.skymansandy.kurl.core.model.KurlRequest
import dev.skymansandy.kurl.core.utils.deserializeKeyValueEntries
import dev.skymansandy.kurl.core.utils.serialize
import dev.skymansandy.kurl.store.api.KurlStore
import dev.skymansandy.kurlclient.presentation.base.MviViewModel
import dev.skymansandy.kurlclient.presentation.screens.playground.PlaygroundScreenContract.PlaygroundEffect
import dev.skymansandy.kurlclient.presentation.screens.playground.PlaygroundScreenContract.PlaygroundEvent
import dev.skymansandy.kurlclient.presentation.screens.playground.PlaygroundScreenContract.PlaygroundState
import dev.skymansandy.kurlclient.util.curlparser.buildCurlCommand
import dev.skymansandy.kurlclient.util.curlparser.parseCurlCommand
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
                headers = listOf(KeyValueEntry(id = nextId++)),
            )
        }

        viewModelScope.launch {
            combine(
                store.folders,
                store.folderPaths,
            ) { folders, paths ->
                folders to paths
            }.collect { (folders, paths) ->
                setState {
                    copy(
                        allFolders = folders,
                        folderPaths = paths,
                    )
                }
            }
        }
    }

    override fun onEvent(event: PlaygroundEvent) {
        when (event) {
            is PlaygroundEvent.SetName -> setState {
                copy(currentRequest = currentRequest.copy(name = event.value))
            }

            is PlaygroundEvent.SetUrl -> setState {
                copy(currentRequest = currentRequest.copy(url = event.value))
            }

            is PlaygroundEvent.SetMethod -> setState {
                copy(currentRequest = currentRequest.copy(method = event.value.name))
            }

            is PlaygroundEvent.SetBody -> setState {
                copy(currentRequest = currentRequest.copy(body = event.value))
            }

            is PlaygroundEvent.UpdateParam -> setState {
                val newParams = params.map {
                    if (it.id == event.id) it.copy(
                        key = event.key,
                        value = event.value,
                        enabled = event.enabled,
                    ) else it
                }
                copy(
                    params = newParams,
                    currentRequest = currentRequest.copy(params = newParams.serialize()),
                )
            }

            is PlaygroundEvent.AddParam -> setState {
                val newParams = params + KeyValueEntry(id = nextId++)
                copy(
                    params = newParams,
                    currentRequest = currentRequest.copy(params = newParams.serialize()),
                )
            }

            is PlaygroundEvent.RemoveParam -> setState {
                val newParams = params.filter { it.id != event.id }
                copy(
                    params = newParams,
                    currentRequest = currentRequest.copy(params = newParams.serialize()),
                )
            }

            is PlaygroundEvent.UpdateHeader -> setState {
                val newHeaders = headers.map {
                    if (it.id == event.id) it.copy(
                        key = event.key,
                        value = event.value,
                        enabled = event.enabled,
                    ) else it
                }
                copy(
                    headers = newHeaders,
                    currentRequest = currentRequest.copy(headers = newHeaders.serialize()),
                )
            }

            is PlaygroundEvent.AddHeader -> setState {
                val newHeaders = headers + KeyValueEntry(id = nextId++)
                copy(
                    headers = newHeaders,
                    currentRequest = currentRequest.copy(headers = newHeaders.serialize()),
                )
            }

            is PlaygroundEvent.RemoveHeader -> setState {
                val newHeaders = headers.filter { it.id != event.id }
                copy(
                    headers = newHeaders,
                    currentRequest = currentRequest.copy(headers = newHeaders.serialize()),
                )
            }

            is PlaygroundEvent.SaveRequest -> saveRequest(event.name, event.folderId)
            is PlaygroundEvent.CreateFolder -> viewModelScope.launch {
                store.createFolder(
                    event.name,
                    event.parentId,
                )
            }

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
            PlaygroundEvent.StartNewRequest -> setState { copy(isEditingNewRequest = true) }
            PlaygroundEvent.ClosePlayground -> {
                if (state.value.hasUnsavedChanges) {
                    setState { copy(showDiscardAndCloseDialog = true) }
                } else {
                    resetToPlaceholder()
                }
            }
            PlaygroundEvent.ConfirmClose -> {
                setState { copy(showDiscardAndCloseDialog = false) }
                resetToPlaceholder()
            }
            PlaygroundEvent.DismissCloseDialog -> setState { copy(showDiscardAndCloseDialog = false) }
            PlaygroundEvent.DeleteLoadedRequest -> deleteLoadedRequest()
        }
    }

    // Pure query — not a state mutation, so kept as a regular function
    fun buildCurlCommand(): String {
        val s = state.value
        val method = runCatching { HttpMethod.valueOf(s.currentRequest.method) }.getOrDefault(HttpMethod.GET)
        return buildCurlCommand(s.currentRequest.url, method, s.headers, s.params, s.currentRequest.body)
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
                currentRequest = currentRequest.copy(
                    url = parsed.url,
                    method = runCatching { HttpMethod.valueOf(parsed.method) }.getOrDefault(HttpMethod.GET).name,
                    headers = h.serialize(),
                    params = p.serialize(),
                    body = parsed.body ?: "",
                ),
                headers = h,
                params = p,
                response = null,
                error = null,
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
                    url = s.currentRequest.url,
                    method = s.currentRequest.method,
                    headers = s.currentRequest.headers,
                    params = s.currentRequest.params,
                    body = s.currentRequest.body,
                )
                setState {
                    val saved = currentRequest.copy(id = loaded.id, name = name, folder_id = folderId)
                    copy(
                        loadedRequest = saved,
                        currentRequest = saved,
                        overwriteSuccess = true,
                    )
                }
            } else {
                val newId = store.saveRequest(
                    name = name,
                    folderId = folderId,
                    url = s.currentRequest.url,
                    method = s.currentRequest.method,
                    headers = s.currentRequest.headers,
                    params = s.currentRequest.params,
                    body = s.currentRequest.body,
                )
                setState {
                    val saved = currentRequest.copy(id = newId, name = name, folder_id = folderId)
                    copy(
                        loadedRequest = saved,
                        currentRequest = saved,
                        saveSuccess = true,
                    )
                }
            }
        }
    }

    private fun overwriteLoadedRequest() {
        val s = state.value
        val loaded = s.loadedRequest ?: return
        viewModelScope.launch {
            store.updateRequest(
                id = loaded.id,
                name = s.currentRequest.name.ifBlank { loaded.name },
                folderId = loaded.folder_id,
                url = s.currentRequest.url,
                method = s.currentRequest.method,
                headers = s.currentRequest.headers,
                params = s.currentRequest.params,
                body = s.currentRequest.body,
            )
            setState {
                val saved = currentRequest.copy(id = loaded.id, name = loaded.name, folder_id = loaded.folder_id)
                copy(loadedRequest = saved, currentRequest = saved, overwriteSuccess = true)
            }
        }
    }

    private fun loadSavedRequest(saved: SavedRequest) {
        val (h, idAfterHeaders) = saved.headers.deserializeKeyValueEntries(nextId)
        val (p, idAfterParams) = saved.params.deserializeKeyValueEntries(idAfterHeaders)
        nextId = idAfterParams
        setState {
            copy(
                loadedRequest = saved,
                currentRequest = saved,
                headers = h.ifEmpty { listOf(KeyValueEntry(id = nextId++)) },
                params = p.ifEmpty { listOf(KeyValueEntry(id = nextId++)) },
                response = null,
                error = null,
                isEditingNewRequest = false,
            )
        }
    }

    private fun deleteLoadedRequest() {
        val id = state.value.loadedRequest?.id ?: return
        viewModelScope.launch {
            store.deleteRequest(id)
            resetToPlaceholder()
        }
    }

    private fun resetToPlaceholder() {
        setState {
            copy(
                loadedRequest = null,
                currentRequest = PlaygroundState().currentRequest,
                isEditingNewRequest = false,
                params = listOf(KeyValueEntry(id = nextId++)),
                headers = listOf(KeyValueEntry(id = nextId++)),
                response = null,
                error = null,
                activeTab = 0,
            )
        }
    }

    private fun sendRequest() {
        val s = state.value
        if (s.currentRequest.url.isBlank()) return
        val url = if (!s.currentRequest.url.startsWith("http://", ignoreCase = true) &&
            !s.currentRequest.url.startsWith("https://", ignoreCase = true)
        ) {
            "https://${s.currentRequest.url}".also { newUrl ->
                setState { copy(currentRequest = currentRequest.copy(url = newUrl)) }
            }
        } else {
            s.currentRequest.url
        }
        viewModelScope.launch {
            setState { copy(isLoading = true, error = null, activeTab = 1) }
            try {
                val kurlResponse = engine.execute(
                    KurlRequest(
                        url = url,
                        method = s.currentRequest.method,
                        headers = s.headers
                            .filter { it.enabled && it.key.isNotBlank() }
                            .associate { it.key to it.value },
                        queryParams = s.params
                            .filter { it.enabled && it.key.isNotBlank() }
                            .associate { it.key to it.value },
                        body = s.currentRequest.body.ifBlank { null },
                    ),
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
                            networkInfo = kurlResponse.networkInfo,
                        ),
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
