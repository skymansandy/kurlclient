package dev.skymansandy.kurlclient.presentation.screens.playground.request

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.skymansandy.kurl.core.model.KeyValueEntry
import dev.skymansandy.kurlclient.presentation.screens.playground.request.tabs.AuthTab
import dev.skymansandy.kurlclient.presentation.screens.playground.request.tabs.BodyTab
import dev.skymansandy.kurlclient.presentation.screens.playground.request.tabs.KeyValueEditorTab
import kurlclient.feature_workspace.generated.resources.Res
import kurlclient.feature_workspace.generated.resources.placeholder_header_key
import kurlclient.feature_workspace.generated.resources.placeholder_param_key
import kurlclient.feature_workspace.generated.resources.placeholder_value
import kurlclient.feature_workspace.generated.resources.tab_auth
import kurlclient.feature_workspace.generated.resources.tab_body
import kurlclient.feature_workspace.generated.resources.tab_headers
import kurlclient.feature_workspace.generated.resources.tab_params
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun RequestPanel(
    params: List<KeyValueEntry>,
    headers: List<KeyValueEntry>,
    body: String,
    onParamUpdate: (Long, String, String, Boolean) -> Unit,
    onParamAdd: () -> Unit,
    onParamRemove: (Long) -> Unit,
    onHeaderUpdate: (Long, String, String, Boolean) -> Unit,
    onHeaderAdd: () -> Unit,
    onHeaderRemove: (Long) -> Unit,
    onBodyChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    val tabParams = stringResource(Res.string.tab_params)
    val tabHeaders = stringResource(Res.string.tab_headers)
    val tabAuth = stringResource(Res.string.tab_auth)
    val tabBody = stringResource(Res.string.tab_body)
    val requestTabs = listOf(tabParams, tabHeaders, tabAuth, tabBody)

    Column(modifier = modifier) {
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            edgePadding = 12.dp,
        ) {
            val activeParamCount = params.count { it.key.isNotBlank() }
            val activeHeaderCount = headers.count { it.key.isNotBlank() }
            val hasBody = body.isNotBlank()
            requestTabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        TabLabel(
                            title = title,
                            count = when (index) {
                                1 -> activeHeaderCount; else -> 0
                            },
                            hasDot = when (index) {
                                0 -> activeParamCount > 0; 3 -> hasBody; else -> false
                            }
                        )
                    }
                )
            }
        }

        Box(modifier = Modifier.fillMaxSize().padding(12.dp)) {
            when (selectedTab) {
                0 -> KeyValueEditorTab(
                    entries = params,
                    onUpdate = onParamUpdate,
                    onAdd = onParamAdd,
                    onRemove = onParamRemove,
                    keyPlaceholder = stringResource(Res.string.placeholder_param_key),
                    valuePlaceholder = stringResource(Res.string.placeholder_value),
                )

                1 -> KeyValueEditorTab(
                    entries = headers,
                    onUpdate = onHeaderUpdate,
                    onAdd = onHeaderAdd,
                    onRemove = onHeaderRemove,
                    keyPlaceholder = stringResource(Res.string.placeholder_header_key),
                    valuePlaceholder = stringResource(Res.string.placeholder_value),
                )

                2 -> AuthTab()

                3 -> BodyTab(
                    body = body,
                    onBodyChange = onBodyChange,
                )
            }
        }
    }
}

@Composable
private fun TabLabel(
    title: String,
    count: Int = 0,
    hasDot: Boolean = false,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(if (count > 0) "$title ($count)" else title)

        if (hasDot) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape),
            )
        }
    }
}