package dev.skymansandy.kurlclient.presentation.screens.workspace.scaffold

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import dev.skymansandy.kurlclient.presentation.screens.collections.CollectionsScreen
import dev.skymansandy.kurlclient.presentation.screens.playground.PlaygroundScreen
import dev.skymansandy.kurlstore.db.SavedRequest

private val PANEL_MIN_WIDTH = 200.dp
private val PANEL_MAX_WIDTH = 600.dp
private val PANEL_DEFAULT_WIDTH = 300.dp
private val DIVIDER_GRAB_WIDTH = 8.dp

@Composable
internal fun ExpandedScaffold(
    activeRequestId: Long?,
    snackbarHostState: SnackbarHostState,
    onShowSnackbar: (String) -> Unit,
    onRequestSelected: (SavedRequest) -> Unit,
    onSaveChanges: () -> Unit,
) {
    Scaffold(
        snackbarHost = {
            SnackbarHost(snackbarHostState) {
                Snackbar(snackbarData = it)
            }
        },
    ) { innerPadding ->

        val density = LocalDensity.current
        var totalWidthPx by remember { mutableStateOf(0) }
        var panelWidth by remember { mutableStateOf(PANEL_DEFAULT_WIDTH) }

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .onSizeChanged { totalWidthPx = it.width },
        ) {
            CollectionsScreen(
                activeRequestId = activeRequestId,
                onRequestSelected = onRequestSelected,
                onSaveChanges = onSaveChanges,
                modifier = Modifier.width(panelWidth).fillMaxHeight(),
            )

            DraggableDivider(
                onDrag = { deltaPx ->
                    val maxWidthDp = with(density) { totalWidthPx.toDp() }
                    panelWidth = (panelWidth + with(density) { deltaPx.toDp() })
                        .coerceIn(
                            PANEL_MIN_WIDTH,
                            minOf(PANEL_MAX_WIDTH, maxWidthDp - PANEL_MIN_WIDTH),
                        )
                },
            )

            PlaygroundScreen(
                onShowSnackbar = onShowSnackbar,
                modifier = Modifier.weight(1f).fillMaxHeight(),
            )
        }
    }
}

@Composable
private fun DraggableDivider(onDrag: (Float) -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .width(DIVIDER_GRAB_WIDTH)
            .fillMaxHeight()
            .resizeHorizontalCursor()
            .draggable(
                orientation = Orientation.Horizontal,
                state = rememberDraggableState { onDrag(it) },
            ),
    ) {
        VerticalDivider()
    }
}
