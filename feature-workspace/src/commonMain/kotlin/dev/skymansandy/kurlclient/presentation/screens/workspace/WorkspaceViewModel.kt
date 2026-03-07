package dev.skymansandy.kurlclient.presentation.screens.workspace

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.skymansandy.kurl.core.KurlEngine
import dev.skymansandy.kurl.core.model.HttpMethod
import dev.skymansandy.kurl.core.model.KeyValueEntry
import dev.skymansandy.kurl.core.model.KurlRequest
import dev.skymansandy.kurl.core.model.NetworkInfo
import dev.skymansandy.kurl.core.utils.deserializeKeyValueEntries
import dev.skymansandy.kurl.core.utils.parseCurlCommand
import dev.skymansandy.kurl.core.utils.serialize
import dev.skymansandy.kurl.store.AppDatabase
import dev.skymansandy.kurl.store.CollectionRepository
import dev.skymansandy.kurlstore.db.SavedRequest
import kotlinx.coroutines.launch

data class ResponseState(
    val statusCode: Int? = null,
    val statusText: String = "",
    val timeMs: Long = 0,
    val sizeBytes: Long = 0,
    val body: String = "",
    val headers: Map<String, String> = emptyMap(),
    val networkInfo: NetworkInfo? = null
)

class RequestViewModel : ViewModel() {

    private val engine = KurlEngine()
    private val repo = CollectionRepository(AppDatabase.db)
    private var nextId = 1L

    var url by mutableStateOf("")
        private set

    var method by mutableStateOf(HttpMethod.GET)
        private set

    var params by mutableStateOf(listOf(KeyValueEntry(id = nextId++)))
        private set

    var headers by mutableStateOf(listOf(KeyValueEntry(id = nextId++)))
        private set

    var body by mutableStateOf("")
        private set

    var isLoading by mutableStateOf(false)
        private set

    var response by mutableStateOf<ResponseState?>(null)
        private set

    var error by mutableStateOf<String?>(null)
        private set

    // ── Request field updates ─────────────────────────────────────────────────

    fun setRequestUrl(value: String) { url = value; hasUnsavedChanges = true }
    fun setRequestMethod(value: HttpMethod) { method = value; hasUnsavedChanges = true }
    fun setRequestBody(value: String) { body = value; hasUnsavedChanges = true }

    // ── Params ────────────────────────────────────────────────────────────────

    fun updateParam(id: Long, key: String, value: String, enabled: Boolean) {
        params = params.map { if (it.id == id) it.copy(key = key, value = value, enabled = enabled) else it }
        hasUnsavedChanges = true
    }

    fun addParam() {
        params = params + KeyValueEntry(id = nextId++)
        hasUnsavedChanges = true
    }

    fun removeParam(id: Long) {
        params = params.filter { it.id != id }
        hasUnsavedChanges = true
    }

    // ── Headers ───────────────────────────────────────────────────────────────

    fun updateHeader(id: Long, key: String, value: String, enabled: Boolean) {
        headers = headers.map { if (it.id == id) it.copy(key = key, value = value, enabled = enabled) else it }
        hasUnsavedChanges = true
    }

    fun addHeader() {
        headers = headers + KeyValueEntry(id = nextId++)
        hasUnsavedChanges = true
    }

    fun removeHeader(id: Long) {
        headers = headers.filter { it.id != id }
        hasUnsavedChanges = true
    }

    // ── Save / Load ───────────────────────────────────────────────────────────

    var loadedRequest by mutableStateOf<SavedRequest?>(null)
        private set

    var hasUnsavedChanges by mutableStateOf(false)
        private set

    var saveSuccess by mutableStateOf(false)
        private set

    fun saveRequest(name: String, folderId: Long?) {
        viewModelScope.launch {
            repo.saveRequest(
                name = name,
                folderId = folderId,
                url = url,
                method = method.name,
                headers = headers.serialize(),
                params = params.serialize(),
                body = body
            )
            hasUnsavedChanges = false
            saveSuccess = true
        }
    }

    fun clearSaveSuccess() { saveSuccess = false }

    var overwriteSuccess by mutableStateOf(false)
        private set

    fun overwriteLoadedRequest() {
        val loaded = loadedRequest ?: return
        viewModelScope.launch {
            repo.updateRequest(
                id = loaded.id,
                name = loaded.name,
                folderId = loaded.folder_id,
                url = url,
                method = method.name,
                headers = headers.serialize(),
                params = params.serialize(),
                body = body
            )
            hasUnsavedChanges = false
            overwriteSuccess = true
        }
    }

    fun clearOverwriteSuccess() { overwriteSuccess = false }

    // ── cURL export / import ──────────────────────────────────────────────────

    fun buildCurlCommand(): String =
        dev.skymansandy.kurl.core.utils.buildCurlCommand(url, method, headers, params, body)

    /** Returns true on success, false if the command couldn't be parsed. */
    fun importFromCurl(curlCommand: String): Boolean {
        val parsed = parseCurlCommand(curlCommand) ?: return false
        url = parsed.url
        method = runCatching { HttpMethod.valueOf(parsed.method) }.getOrDefault(HttpMethod.GET)
        headers = parsed.headers
            .map { (k, v) -> KeyValueEntry(id = nextId++, key = k, value = v) }
            .ifEmpty { listOf(KeyValueEntry(id = nextId++)) }
        params = parsed.params
            .map { (k, v) -> KeyValueEntry(id = nextId++, key = k, value = v) }
            .ifEmpty { listOf(KeyValueEntry(id = nextId++)) }
        body = parsed.body ?: ""
        response = null
        error = null
        return true
    }

    fun loadSavedRequest(saved: SavedRequest) {
        loadedRequest = saved
        url = saved.url
        method = runCatching { HttpMethod.valueOf(saved.method) }.getOrDefault(HttpMethod.GET)
        val (h, idAfterHeaders) = saved.headers.deserializeKeyValueEntries(nextId)
        val (p, idAfterParams) = saved.params.deserializeKeyValueEntries(idAfterHeaders)
        nextId = idAfterParams
        headers = h.ifEmpty { listOf(KeyValueEntry(id = nextId++)) }
        params = p.ifEmpty { listOf(KeyValueEntry(id = nextId++)) }
        body = saved.body
        response = null
        error = null
        hasUnsavedChanges = false
    }

    // ── Send ──────────────────────────────────────────────────────────────────

    fun sendRequest() {
        if (url.isBlank()) return
        if (!url.startsWith("http://", ignoreCase = true) && !url.startsWith("https://", ignoreCase = true)) {
            url = "https://$url"
        }
        viewModelScope.launch {
            isLoading = true
            error = null
            try {
                val kurlResponse = engine.execute(
                    KurlRequest(
                        url = url,
                        method = method.name,
                        headers = headers
                            .filter { it.enabled && it.key.isNotBlank() }
                            .associate { it.key to it.value },
                        queryParams = params
                            .filter { it.enabled && it.key.isNotBlank() }
                            .associate { it.key to it.value },
                        body = body.ifBlank { null }
                    )
                )
                response = ResponseState(
                    statusCode = kurlResponse.statusCode,
                    statusText = kurlResponse.statusText,
                    timeMs = kurlResponse.timeMs,
                    sizeBytes = kurlResponse.sizeBytes,
                    body = kurlResponse.body,
                    headers = kurlResponse.headers,
                    networkInfo = kurlResponse.networkInfo
                )
            } catch (e: Exception) {
                error = e.message ?: "Request failed"
            } finally {
                isLoading = false
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        engine.close()
    }
}