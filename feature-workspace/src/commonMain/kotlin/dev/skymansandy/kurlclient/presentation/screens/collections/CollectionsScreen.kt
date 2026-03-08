package dev.skymansandy.kurlclient.presentation.screens.collections

import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.skymansandy.kurlclient.presentation.dialog.NewFolderDialog
import dev.skymansandy.kurlclient.presentation.screens.collections.CollectionsScreenContract.CollectionsEvent
import dev.skymansandy.kurlclient.presentation.screens.collections.CollectionsScreenContract.CollectionsState.TreeItem
import dev.skymansandy.kurlclient.presentation.screens.collections.component.CollectionsSearchBar
import dev.skymansandy.kurlclient.presentation.screens.collections.component.tree.FolderItem
import dev.skymansandy.kurlclient.presentation.screens.collections.component.tree.RequestItem
import dev.skymansandy.kurlstore.db.SavedRequest
import kurlclient.feature_workspace.generated.resources.Res
import kurlclient.feature_workspace.generated.resources.cd_new_folder
import kurlclient.feature_workspace.generated.resources.msg_no_results
import kurlclient.feature_workspace.generated.resources.msg_no_saved_requests
import kurlclient.feature_workspace.generated.resources.title_collections
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CollectionsScreen(
    activeRequestId: Long? = null,
    onRequestSelected: (SavedRequest) -> Unit,
    onSaveChanges: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val vm: CollectionsViewModel = koinViewModel()
    val state by vm.state.collectAsStateWithLifecycle()

    var showNewFolderDialog by remember { mutableStateOf(false) }
    var newFolderParentId by remember { mutableStateOf<Long?>(null) }

    // ── Drag & drop state ─────────────────────────────────────────────────────
    var draggedKey by remember { mutableStateOf<String?>(null) }
    var dropTargetKey by remember { mutableStateOf<String?>(null) }
    val itemBounds = remember { mutableStateMapOf<String, Rect>() }

    val isSearching = state.searchQuery.isNotBlank()

    val treeItems =
        remember(state.allFolders, state.allRequests, state.expandedFolderIds, state.searchQuery) {
            if (isSearching) vm.buildFilteredTreeItems(state.searchQuery, state)
            else vm.buildTreeItems(state)
        }

    Column(modifier = modifier) {
        TopAppBar(
            title = {
                Text(
                    text = stringResource(Res.string.title_collections),
                    style = MaterialTheme.typography.titleMedium,
                )
            },
            actions = {
                IconButton(
                    onClick = {
                        newFolderParentId = null
                        showNewFolderDialog = true
                    },
                ) {
                    Icon(Icons.Default.Add, contentDescription = stringResource(Res.string.cd_new_folder))
                }
            },
        )

        HorizontalDivider()

        CollectionsSearchBar(
            query = state.searchQuery,
            onQueryChange = { vm.onEvent(CollectionsEvent.SetSearchQuery(it)) },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
        )

        HorizontalDivider()

        if (treeItems.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = if (isSearching) stringResource(Res.string.msg_no_results, state.searchQuery)
                    else stringResource(Res.string.msg_no_saved_requests),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
            ) {
                items(
                    items = treeItems,
                    key = {
                        when (it) {
                            is TreeItem.Folder -> "f_${it.folder.id}"
                            is TreeItem.Request -> "r_${it.request.id}"
                        }
                    },
                ) { item ->

                    val key = when (item) {
                        is TreeItem.Folder -> "f_${item.folder.id}"
                        is TreeItem.Request -> "r_${item.request.id}"
                    }
                    val isDragging = !isSearching && draggedKey == key
                    val isDropTarget = !isSearching && dropTargetKey == key

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .alpha(if (isDragging) 0.35f else 1f)
                            .then(
                                if (!isSearching) Modifier
                                    .onGloballyPositioned { itemBounds[key] = it.boundsInRoot() }
                                    .pointerInput(Unit) {
                                        detectDragGesturesAfterLongPress(
                                            onDragStart = { draggedKey = key },
                                            onDrag = { change, _ ->
                                                change.consume()
                                                val bounds = itemBounds[key]
                                                    ?: return@detectDragGesturesAfterLongPress
                                                val absY = bounds.top + change.position.y
                                                dropTargetKey = itemBounds.entries
                                                    .filter { it.key.startsWith("f_") && it.key != draggedKey }
                                                    .firstOrNull { absY in it.value.top..it.value.bottom }
                                                    ?.key
                                            },
                                            onDragEnd = {
                                                val src = draggedKey
                                                val dst = dropTargetKey
                                                if (src != null && dst != null) {
                                                    val targetId =
                                                        dst.removePrefix("f_").toLongOrNull()
                                                    when {
                                                        src.startsWith("f_") ->
                                                            vm.onEvent(
                                                                CollectionsEvent.MoveFolder(
                                                                    src.removePrefix("f_").toLong(),
                                                                    targetId,
                                                                ),
                                                            )

                                                        src.startsWith("r_") ->
                                                            vm.onEvent(
                                                                CollectionsEvent.MoveRequest(
                                                                    src.removePrefix("r_").toLong(),
                                                                    targetId,
                                                                ),
                                                            )
                                                    }
                                                }
                                                draggedKey = null
                                                dropTargetKey = null
                                            },
                                            onDragCancel = {
                                                draggedKey = null
                                                dropTargetKey = null
                                            },
                                        )
                                    } else Modifier,
                            ),
                    ) {
                        when (item) {
                            is TreeItem.Folder -> FolderItem(
                                item = item,
                                isDropTarget = isDropTarget,
                                onToggle = {
                                    if (!isSearching) vm.onEvent(
                                        CollectionsEvent.ToggleFolder(
                                            item.folder.id,
                                        ),
                                    )
                                },
                                onNewSubfolder = {
                                    newFolderParentId = item.folder.id
                                    showNewFolderDialog = true
                                },
                                onDelete = { vm.onEvent(CollectionsEvent.DeleteFolder(item.folder.id)) },
                            )

                            is TreeItem.Request -> RequestItem(
                                item = item,
                                isActive = item.request.id == activeRequestId,
                                highlightQuery = state.searchQuery,
                                onLoad = { onRequestSelected(item.request) },
                                onSaveChanges = onSaveChanges,
                                onDuplicate = { vm.onEvent(CollectionsEvent.DuplicateRequest(item.request.id)) },
                                onDelete = { vm.onEvent(CollectionsEvent.DeleteRequest(item.request.id)) },
                            )
                        }
                    }
                }
            }
        }
    }

    if (showNewFolderDialog) {
        NewFolderDialog(
            fixedParentId = newFolderParentId,
            onDismiss = {
                showNewFolderDialog = false
            },
            onCreate = { name, parentId ->
                vm.onEvent(CollectionsEvent.CreateFolder(name, parentId))
                showNewFolderDialog = false
            },
        )
    }
}
