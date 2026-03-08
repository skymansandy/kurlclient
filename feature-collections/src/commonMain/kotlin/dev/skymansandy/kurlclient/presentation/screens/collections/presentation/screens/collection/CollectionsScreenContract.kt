package dev.skymansandy.kurlclient.presentation.screens.collections.presentation.screens.collection

import dev.skymansandy.kurlclient.presentation.base.contract.UiEffect
import dev.skymansandy.kurlclient.presentation.base.contract.UiEvent
import dev.skymansandy.kurlclient.presentation.base.contract.UiState
import dev.skymansandy.kurlstore.db.CollectionFolder
import dev.skymansandy.kurlstore.db.SavedRequest

data class CollectionsState(
    val allFolders: List<CollectionFolder> = emptyList(),
    val allRequests: List<SavedRequest> = emptyList(),
    val expandedFolderIds: Set<Long> = emptySet(),
    val folderPaths: Map<Long, String> = emptyMap(),
    val searchQuery: String = ""
) : UiState {

    sealed interface TreeItem {

        data class Folder(
            val folder: CollectionFolder,
            val depth: Int,
            val isExpanded: Boolean,
        ) : TreeItem

        data class Request(
            val request: SavedRequest,
            val depth: Int,
        ) : TreeItem
    }
}

sealed interface CollectionsEvent : UiEvent {
    data object Refresh : CollectionsEvent
    data class ToggleFolder(val id: Long) : CollectionsEvent
    data class SetSearchQuery(val query: String) : CollectionsEvent
    data class CreateFolder(val name: String, val parentId: Long?) : CollectionsEvent
    data class MoveFolder(val id: Long, val newParentId: Long?) : CollectionsEvent
    data class MoveRequest(val id: Long, val newFolderId: Long?) : CollectionsEvent
    data class DeleteFolder(val id: Long) : CollectionsEvent
    data class DeleteRequest(val id: Long) : CollectionsEvent
}

sealed interface CollectionsEffect : UiEffect