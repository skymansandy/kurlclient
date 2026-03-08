package dev.skymansandy.kurlclient.presentation.screens.playground

import dev.skymansandy.kurl.core.model.HttpMethod
import dev.skymansandy.kurl.core.model.KeyValueEntry
import dev.skymansandy.kurl.core.model.NetworkInfo
import dev.skymansandy.kurlclient.presentation.base.contract.UiEffect
import dev.skymansandy.kurlclient.presentation.base.contract.UiEvent
import dev.skymansandy.kurlclient.presentation.base.contract.UiState
import dev.skymansandy.kurlstore.db.CollectionFolder
import dev.skymansandy.kurlstore.db.SavedRequest

internal class PlaygroundScreenContract {

    data class PlaygroundState(
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
        val folderPaths: Map<Long, String> = emptyMap(),
        val showSaveDialog: Boolean = false,
        val showImportCurlDialog: Boolean = false,
        val showDiscardAndCloseDialog: Boolean = false,
        val activeTab: Int = 0,
        val isEditingNewRequest: Boolean = false,
    ) : UiState {

        data class ResponseState(
            val statusCode: Int? = null,
            val statusText: String = "",
            val timeMs: Long = 0,
            val sizeBytes: Long = 0,
            val body: String = "",
            val headers: Map<String, String> = emptyMap(),
            val networkInfo: NetworkInfo? = null,
        )
    }

    sealed interface PlaygroundEvent : UiEvent {
        data class SetUrl(val value: String) : PlaygroundEvent
        data class SetMethod(val value: HttpMethod) : PlaygroundEvent
        data class SetBody(val value: String) : PlaygroundEvent
        data class UpdateParam(val id: Long, val key: String, val value: String, val enabled: Boolean) : PlaygroundEvent
        data object AddParam : PlaygroundEvent
        data class RemoveParam(val id: Long) : PlaygroundEvent
        data class UpdateHeader(val id: Long, val key: String, val value: String, val enabled: Boolean) : PlaygroundEvent
        data object AddHeader : PlaygroundEvent
        data class RemoveHeader(val id: Long) : PlaygroundEvent
        data class SaveRequest(val name: String, val folderId: Long?) : PlaygroundEvent
        data class CreateFolder(val name: String, val parentId: Long?) : PlaygroundEvent
        data object ClearSaveSuccess : PlaygroundEvent
        data object OverwriteLoadedRequest : PlaygroundEvent
        data object ClearOverwriteSuccess : PlaygroundEvent
        data class LoadSavedRequest(val saved: SavedRequest) : PlaygroundEvent
        data object SendRequest : PlaygroundEvent
        data object ShowSaveDialog : PlaygroundEvent
        data object HideSaveDialog : PlaygroundEvent
        data object ShowImportCurlDialog : PlaygroundEvent
        data object HideImportCurlDialog : PlaygroundEvent
        data class SelectTab(val index: Int) : PlaygroundEvent
        data object StartNewRequest : PlaygroundEvent
        data object ClosePlayground : PlaygroundEvent
        data object ConfirmClose : PlaygroundEvent
        data object DismissCloseDialog : PlaygroundEvent
        data object DeleteLoadedRequest : PlaygroundEvent
    }

    sealed interface PlaygroundEffect : UiEffect
}
