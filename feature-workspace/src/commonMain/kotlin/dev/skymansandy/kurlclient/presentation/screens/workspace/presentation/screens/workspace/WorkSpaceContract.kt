package dev.skymansandy.kurlclient.presentation.screens.workspace.presentation.screens.workspace

import dev.skymansandy.kurl.core.model.HttpMethod
import dev.skymansandy.kurl.core.model.KeyValueEntry
import dev.skymansandy.kurl.core.model.NetworkInfo
import dev.skymansandy.kurlclient.presentation.base.contract.UiEffect
import dev.skymansandy.kurlclient.presentation.base.contract.UiEvent
import dev.skymansandy.kurlclient.presentation.base.contract.UiState
import dev.skymansandy.kurlstore.db.CollectionFolder
import dev.skymansandy.kurlstore.db.SavedRequest

data class WorkspaceState(
    val url: String = "",
    val method: HttpMethod = HttpMethod.GET,
    val params: List<KeyValueEntry> = emptyList(),
    val headers: List<KeyValueEntry> = emptyList(),
    val body: String = "",
    val isLoading: Boolean = false,
    val response: ResponseState? = null,
    val error: String? = null,
    val loadedRequest: SavedRequest? = null,
    val hasUnsavedChanges: Boolean = false,
    val saveSuccess: Boolean = false,
    val overwriteSuccess: Boolean = false,
    val allFolders: List<CollectionFolder> = emptyList(),
    val folderPaths: Map<Long, String> = emptyMap()
) : UiState {

    data class ResponseState(
        val statusCode: Int? = null,
        val statusText: String = "",
        val timeMs: Long = 0,
        val sizeBytes: Long = 0,
        val body: String = "",
        val headers: Map<String, String> = emptyMap(),
        val networkInfo: NetworkInfo? = null
    )
}

sealed interface WorkspaceEvent: UiEvent {
    data class SetUrl(val value: String) : WorkspaceEvent
    data class SetMethod(val value: HttpMethod) : WorkspaceEvent
    data class SetBody(val value: String) : WorkspaceEvent
    data class UpdateParam(val id: Long, val key: String, val value: String, val enabled: Boolean) : WorkspaceEvent
    data object AddParam : WorkspaceEvent
    data class RemoveParam(val id: Long) : WorkspaceEvent
    data class UpdateHeader(val id: Long, val key: String, val value: String, val enabled: Boolean) : WorkspaceEvent
    data object AddHeader : WorkspaceEvent
    data class RemoveHeader(val id: Long) : WorkspaceEvent
    data class SaveRequest(val name: String, val folderId: Long?) : WorkspaceEvent
    data class CreateFolder(val name: String, val parentId: Long?) : WorkspaceEvent
    data object ClearSaveSuccess : WorkspaceEvent
    data object OverwriteLoadedRequest : WorkspaceEvent
    data object ClearOverwriteSuccess : WorkspaceEvent
    data class LoadSavedRequest(val saved: SavedRequest) : WorkspaceEvent
    data object SendRequest : WorkspaceEvent
}

sealed interface WorkspaceEffect : UiEffect