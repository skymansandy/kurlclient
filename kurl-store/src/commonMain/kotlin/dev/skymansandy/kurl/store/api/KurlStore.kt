package dev.skymansandy.kurl.store.api

import dev.skymansandy.kurlstore.db.CollectionFolder
import dev.skymansandy.kurlstore.db.SavedRequest
import kotlinx.coroutines.flow.StateFlow

interface KurlStore {

    val folders: StateFlow<List<CollectionFolder>>
    val requests: StateFlow<List<SavedRequest>>
    val folderPaths: StateFlow<Map<Long, String>>

    suspend fun createFolder(name: String, parentId: Long?): Long
    suspend fun moveFolderTo(id: Long, parentId: Long?)
    suspend fun deleteFolder(id: Long)

    suspend fun saveRequest(
        name: String, folderId: Long?, url: String,
        method: String, headers: String, params: String, body: String
    )

    suspend fun updateRequest(
        id: Long, name: String, folderId: Long?, url: String,
        method: String, headers: String, params: String, body: String
    )

    suspend fun moveRequestTo(id: Long, folderId: Long?)
    suspend fun deleteRequest(id: Long)
}