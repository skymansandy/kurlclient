package dev.skymansandy.kurlclient.presentation.screens.playground.panel

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.skymansandy.kurl.core.model.HttpMethod
import dev.skymansandy.kurlclient.presentation.component.InlineTextField
import dev.skymansandy.kurlclient.presentation.screens.playground.PlaygroundScreenContract.PlaygroundEvent
import dev.skymansandy.kurlclient.presentation.screens.playground.PlaygroundScreenContract.PlaygroundState
import dev.skymansandy.kurlclient.presentation.screens.playground.PlaygroundScreenModel
import dev.skymansandy.kurlclient.presentation.screens.playground.component.PlaygroundToolbarActions
import dev.skymansandy.kurlclient.presentation.screens.playground.component.UrlBar
import dev.skymansandy.kurlclient.presentation.screens.playground.request.RequestPanel
import dev.skymansandy.kurlclient.presentation.screens.playground.response.ResponsePanel
import kurlclient.feature_workspace.generated.resources.Res
import kurlclient.feature_workspace.generated.resources.placeholder_request_name
import kurlclient.feature_workspace.generated.resources.tab_request
import kurlclient.feature_workspace.generated.resources.tab_response
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun PlaygroundPanel(
    modifier: Modifier = Modifier,
    state: PlaygroundState,
    viewmodel: PlaygroundScreenModel,
    onCopyCurl: () -> Unit,
) {
    BoxWithConstraints(modifier = modifier) {
        val isCompact = maxWidth < 480.dp

        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, end = if (isCompact) 4.dp else 12.dp, top = 8.dp, bottom = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                InlineTextField(
                    value = state.currentRequest.name,
                    placeholder = stringResource(Res.string.placeholder_request_name),
                    onValueChange = { viewmodel.onEvent(PlaygroundEvent.SetName(it)) },
                    showBorderOnlyWhenFocused = true,
                    showEditIconOnHover = true,
                    modifier = Modifier.weight(1f),
                )

                if (isCompact) {
                    PlaygroundToolbarActions(
                        showSave = state.loadedRequest == null || state.hasUnsavedChanges,
                        isLoading = state.isLoading,
                        isNewRequest = state.loadedRequest == null,
                        onSave = {
                            if (state.loadedRequest != null) {
                                viewmodel.onEvent(PlaygroundEvent.OverwriteLoadedRequest)
                            } else {
                                viewmodel.onEvent(PlaygroundEvent.ShowSaveDialog)
                            }
                        },
                        onDelete = { viewmodel.onEvent(PlaygroundEvent.DeleteLoadedRequest) },
                        onClose = { viewmodel.onEvent(PlaygroundEvent.ClosePlayground) },
                        onCopyCurl = onCopyCurl,
                        onImportCurl = { viewmodel.onEvent(PlaygroundEvent.ShowImportCurlDialog) },
                    )
                }
            }

            UrlBar(
                method = runCatching { HttpMethod.valueOf(state.currentRequest.method) }.getOrDefault(
                    HttpMethod.GET,
                ),
                url = state.currentRequest.url,
                isLoading = state.isLoading,
                isNewRequest = state.loadedRequest == null,
                showSave = state.loadedRequest == null || state.hasUnsavedChanges,
                onMethodChange = { viewmodel.onEvent(PlaygroundEvent.SetMethod(it)) },
                onUrlChange = { viewmodel.onEvent(PlaygroundEvent.SetUrl(it)) },
                onSend = { viewmodel.onEvent(PlaygroundEvent.SendRequest) },
                onSave = {
                    if (state.loadedRequest != null) {
                        viewmodel.onEvent(PlaygroundEvent.OverwriteLoadedRequest)
                    } else {
                        viewmodel.onEvent(PlaygroundEvent.ShowSaveDialog)
                    }
                },
                onDelete = { viewmodel.onEvent(PlaygroundEvent.DeleteLoadedRequest) },
                onClose = { viewmodel.onEvent(PlaygroundEvent.ClosePlayground) },
                onCopyCurl = onCopyCurl,
                onImportCurl = { viewmodel.onEvent(PlaygroundEvent.ShowImportCurlDialog) },
                showToolbarActions = !isCompact,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
            )

            TabRow(
                selectedTabIndex = state.activeTab,
            ) {
                Tab(
                    selected = state.activeTab == 0,
                    onClick = { viewmodel.onEvent(PlaygroundEvent.SelectTab(0)) },
                    text = { Text(stringResource(Res.string.tab_request)) },
                )

                Tab(
                    selected = state.activeTab == 1,
                    onClick = { viewmodel.onEvent(PlaygroundEvent.SelectTab(1)) },
                    text = { Text(stringResource(Res.string.tab_response)) },
                )
            }

            when (state.activeTab) {
                0 -> RequestPanel(vm = viewmodel, modifier = Modifier.weight(1f).fillMaxWidth())
                else -> ResponsePanel(vm = viewmodel, modifier = Modifier.weight(1f).fillMaxWidth())
            }
        }
    }
}
