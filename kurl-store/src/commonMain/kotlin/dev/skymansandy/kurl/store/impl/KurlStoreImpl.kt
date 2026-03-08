package dev.skymansandy.kurl.store.impl

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import dev.skymansandy.kurl.core.utils.currentTimeMillis
import dev.skymansandy.kurl.store.api.KurlStore
import dev.skymansandy.kurlstore.db.CollectionFolder
import dev.skymansandy.kurlstore.db.KurlDatabase
import dev.skymansandy.kurlstore.db.SavedRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext

internal class KurlStoreImpl(
    private val db: KurlDatabase,
) : KurlStore {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override val folders: StateFlow<List<CollectionFolder>> =
        db.collectionsQueries.getAllFolders()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .stateIn(scope, SharingStarted.Eagerly, emptyList())

    override val requests: StateFlow<List<SavedRequest>> =
        db.collectionsQueries.getAllRequests()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .stateIn(scope, SharingStarted.Eagerly, emptyList())

    override val folderPaths: StateFlow<Map<Long, String>> =
        folders
            .map { buildFolderPathsMap(it) }
            .stateIn(scope, SharingStarted.Eagerly, emptyMap())

    // ── Folders ───────────────────────────────────────────────────────────────
    override suspend fun createFolder(name: String, parentId: Long?): Long =
        withContext(Dispatchers.IO) {
            db.collectionsQueries.insertFolder(
                name = name,
                parent_id = parentId,
                created_at = currentTimeMillis()
            )
            db.collectionsQueries.lastInsertedRowId().executeAsOne()
        }

    override suspend fun moveFolderTo(id: Long, parentId: Long?) {
        withContext(Dispatchers.IO) {
            db.collectionsQueries.moveFolderToParent(parent_id = parentId, id = id)
        }
    }

    override suspend fun deleteFolder(id: Long) {
        withContext(Dispatchers.IO) {
            db.collectionsQueries.deleteFolder(id)
        }
    }

    // ── Requests ──────────────────────────────────────────────────────────────
    override suspend fun saveRequest(
        name: String, folderId: Long?, url: String,
        method: String, headers: String, params: String, body: String
    ) {
        withContext(Dispatchers.IO) {
            db.collectionsQueries.insertRequest(
                name = name, folder_id = folderId, url = url, method = method,
                headers = headers, params = params, body = body, created_at = currentTimeMillis()
            )
        }
    }

    override suspend fun updateRequest(
        id: Long, name: String, folderId: Long?, url: String,
        method: String, headers: String, params: String, body: String
    ) {
        withContext(Dispatchers.IO) {
            db.collectionsQueries.updateRequest(
                id = id, name = name, folder_id = folderId, url = url, method = method,
                headers = headers, params = params, body = body, created_at = currentTimeMillis()
            )
        }
    }

    override suspend fun moveRequestTo(id: Long, folderId: Long?) {
        withContext(Dispatchers.IO) {
            db.collectionsQueries.moveRequestToFolder(folder_id = folderId, id = id)
        }
    }

    override suspend fun deleteRequest(id: Long) {
        withContext(Dispatchers.IO) {
            db.collectionsQueries.deleteRequest(id)
        }
    }

    // ── Private helpers ───────────────────────────────────────────────────────
    private fun buildFolderPathsMap(folders: List<CollectionFolder>): Map<Long, String> {
        val folderMap = folders.associateBy { it.id }
        return folders.associate { folder ->
            var path = folder.name
            var parentId = folder.parent_id
            while (parentId != null) {
                val parent = folderMap[parentId] ?: break
                path = "${parent.name} / $path"
                parentId = parent.parent_id
            }
            folder.id to path
        }
    }
}