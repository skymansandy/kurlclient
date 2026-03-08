package dev.skymansandy.kurlclient.presentation.screens.collections

import androidx.lifecycle.viewModelScope
import dev.skymansandy.kurl.store.api.KurlStore
import dev.skymansandy.kurlclient.presentation.base.MviViewModel
import dev.skymansandy.kurlclient.presentation.screens.collections.CollectionsScreenContract.CollectionsEffect
import dev.skymansandy.kurlclient.presentation.screens.collections.CollectionsScreenContract.CollectionsEvent
import dev.skymansandy.kurlclient.presentation.screens.collections.CollectionsScreenContract.CollectionsState
import dev.skymansandy.kurlclient.presentation.screens.collections.CollectionsScreenContract.CollectionsState.TreeItem
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
internal class CollectionsViewModel(private val store: KurlStore) :
    MviViewModel<CollectionsState, CollectionsEvent, CollectionsEffect>() {

    override fun createInitialState() = CollectionsState()

    init {
        viewModelScope.launch {
            combine(
                store.folders,
                store.requests,
                store.folderPaths,
            ) { folders, requests, paths ->
                Triple(folders, requests, paths)
            }.collect { (folders, requests, paths) ->
                setState {
                    copy(
                        allFolders = folders,
                        allRequests = requests,
                        folderPaths = paths,
                    )
                }
            }
        }
    }

    override fun onEvent(event: CollectionsEvent) {
        when (event) {
            is CollectionsEvent.ToggleFolder -> toggleFolder(event.id)
            is CollectionsEvent.SetSearchQuery -> setState { copy(searchQuery = event.query) }
            is CollectionsEvent.CreateFolder -> createFolder(event.name, event.parentId)
            is CollectionsEvent.MoveFolder -> moveFolder(event.id, event.newParentId)
            is CollectionsEvent.MoveRequest -> moveRequest(event.id, event.newFolderId)
            is CollectionsEvent.DeleteFolder -> deleteFolder(event.id)
            is CollectionsEvent.DeleteRequest -> deleteRequest(event.id)
            is CollectionsEvent.DuplicateRequest -> duplicateRequest(event.id)
        }
    }

    private fun toggleFolder(id: Long) {
        setState {
            val ids = expandedFolderIds
            copy(expandedFolderIds = if (id in ids) ids - id else ids + id)
        }
    }

    fun buildTreeItems(s: CollectionsState = state.value): List<TreeItem> {
        val result = mutableListOf<TreeItem>()
        appendChildren(s, parentId = null, depth = 0, result = result)
        return result
    }

    private fun appendChildren(
        s: CollectionsState,
        parentId: Long?,
        depth: Int,
        result: MutableList<TreeItem>,
    ) {
        s.allFolders
            .filter { it.parent_id == parentId }
            .sortedBy { it.name }
            .forEach { folder ->
                val expanded = folder.id in s.expandedFolderIds
                result.add(TreeItem.Folder(folder, depth, expanded))
                if (expanded) appendChildren(s, folder.id, depth + 1, result)
            }

        s.allRequests
            .filter { it.folder_id == parentId }
            .sortedBy { it.name }
            .forEach { request ->
                result.add(TreeItem.Request(request, depth))
            }
    }

    fun buildFilteredTreeItems(query: String, s: CollectionsState = state.value): List<TreeItem> {
        val q = query.trim()
        if (q.isBlank()) return buildTreeItems(s)

        val matchingIds = s.allRequests
            .filter {
                it.name.contains(q, ignoreCase = true) || it.url.contains(
                    q,
                    ignoreCase = true,
                )
            }
            .map { it.id }.toSet()

        val visibleFolderIds = mutableSetOf<Long>()
        s.allRequests.filter { it.id in matchingIds }.forEach { req ->
            var fid = req.folder_id
            while (fid != null) {
                visibleFolderIds.add(fid)
                fid = s.allFolders.find { it.id == fid }?.parent_id
            }
        }

        val result = mutableListOf<TreeItem>()
        appendFilteredChildren(s, null, 0, result, matchingIds, visibleFolderIds)
        return result
    }

    private fun appendFilteredChildren(
        s: CollectionsState, parentId: Long?, depth: Int,
        result: MutableList<TreeItem>, matchingIds: Set<Long>, visibleFolderIds: Set<Long>,
    ) {
        s.allFolders
            .filter { it.parent_id == parentId && it.id in visibleFolderIds }
            .sortedBy { it.name }
            .forEach { folder ->
                result.add(TreeItem.Folder(folder, depth, isExpanded = true))
                appendFilteredChildren(
                    s,
                    folder.id,
                    depth + 1,
                    result,
                    matchingIds,
                    visibleFolderIds,
                )
            }

        s.allRequests
            .filter { it.folder_id == parentId && it.id in matchingIds }
            .sortedBy { it.name }
            .forEach { request ->
                result.add(TreeItem.Request(request, depth))
            }
    }

    private fun createFolder(name: String, parentId: Long?) {
        viewModelScope.launch {
            val newId = store.createFolder(name, parentId)
            setState {
                copy(
                    expandedFolderIds = expandedFolderIds
                        .let { if (parentId != null) it + parentId else it } + newId)
            }
        }
    }

    private fun isDescendantOf(s: CollectionsState, ancestorId: Long, folderId: Long?): Boolean {
        if (folderId == null) return false
        if (folderId == ancestorId) return true
        return isDescendantOf(s, ancestorId, s.allFolders.find { it.id == folderId }?.parent_id)
    }

    private fun moveFolder(id: Long, newParentId: Long?) {
        val s = state.value
        if (isDescendantOf(s, id, newParentId)) return
        if (s.allFolders.find { it.id == id }?.parent_id == newParentId) return
        viewModelScope.launch { store.moveFolderTo(id, newParentId) }
    }

    private fun moveRequest(id: Long, newFolderId: Long?) {
        if (state.value.allRequests.find { it.id == id }?.folder_id == newFolderId) return
        viewModelScope.launch { store.moveRequestTo(id, newFolderId) }
    }

    private fun deleteFolder(id: Long) {
        viewModelScope.launch {
            store.deleteFolder(id)
            setState { copy(expandedFolderIds = expandedFolderIds - id) }
        }
    }

    private fun deleteRequest(id: Long) {
        viewModelScope.launch { store.deleteRequest(id) }
    }

    private fun duplicateRequest(id: Long) {
        val request = state.value.allRequests.find { it.id == id } ?: return
        viewModelScope.launch {
            store.saveRequest(
                name = "Copy of ${request.name}",
                folderId = request.folder_id,
                url = request.url,
                method = request.method,
                headers = request.headers,
                params = request.params,
                body = request.body,
            )
        }
    }
}
