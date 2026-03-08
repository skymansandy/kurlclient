package dev.skymansandy.kurlclient.presentation.screens.playground.response

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.skymansandy.kurlclient.presentation.screens.playground.PlaygroundScreenContract.PlaygroundState.ResponseState
import dev.skymansandy.kurlclient.presentation.screens.playground.response.tabs.NetworkInfoTab
import dev.skymansandy.kurlclient.presentation.screens.playground.response.tabs.ResponseBodyTab
import dev.skymansandy.kurlclient.presentation.screens.playground.response.tabs.ResponseHeadersTab
import kurlclient.feature_workspace.generated.resources.Res
import kurlclient.feature_workspace.generated.resources.msg_error_prefix
import kurlclient.feature_workspace.generated.resources.msg_send_request_hint
import kurlclient.feature_workspace.generated.resources.tab_body
import kurlclient.feature_workspace.generated.resources.tab_headers
import kurlclient.feature_workspace.generated.resources.tab_network
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt

@Composable
internal fun ResponsePanel(
    response: ResponseState?,
    error: String?,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    val tabBody = stringResource(Res.string.tab_body)
    val tabHeaders = stringResource(Res.string.tab_headers)
    val tabNetwork = stringResource(Res.string.tab_network)
    val responseTabs = listOf(tabBody, tabHeaders, tabNetwork)

    Column(
        modifier = modifier,
    ) {
        ResponseStatusBar(
            response = response,
            error = error,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp)
        )

        TabRow(selectedTabIndex = selectedTab) {
            val headerCount = response?.headers?.size ?: 0
            val hasBody = response?.body?.isNotBlank() == true
            val hasNetwork = response?.networkInfo != null
            responseTabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        TabLabel(
                            title = title,
                            count = when (index) {
                                1 -> headerCount; else -> 0
                            },
                            hasDot = when (index) {
                                0 -> hasBody; 2 -> hasNetwork; else -> false
                            }
                        )
                    }
                )
            }
        }

        Box(modifier = Modifier.fillMaxSize().padding(12.dp)) {
            when (selectedTab) {
                0 -> ResponseBodyTab(body = response?.body ?: "", error = error)
                1 -> ResponseHeadersTab(headers = response?.headers ?: emptyMap())
                2 -> NetworkInfoTab(networkInfo = response?.networkInfo)
            }
        }
    }
}

@Composable
private fun TabLabel(title: String, count: Int = 0, hasDot: Boolean = false) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(if (count > 0) "$title ($count)" else title)
        if (hasDot) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
            )
        }
    }
}

@Composable
private fun ResponseStatusBar(
    response: ResponseState?,
    error: String?,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        when {
            error != null -> Text(
                text = stringResource(Res.string.msg_error_prefix, error),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )

            response != null -> {
                StatusBadge(code = response.statusCode!!)

                Text(
                    text = response.statusText,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = "${response.timeMs} ms",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = formatSize(response.sizeBytes),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            else -> Text(
                text = stringResource(Res.string.msg_send_request_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun StatusBadge(code: Int) {
    val color = when (code) {
        in 200..299 -> MaterialTheme.colorScheme.primary
        in 300..399 -> MaterialTheme.colorScheme.tertiary
        in 400..599 -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.secondary
    }

    Surface(color = color, shape = RoundedCornerShape(4.dp)) {
        Text(
            text = code.toString(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}

private fun formatSize(bytes: Long): String = when {
    bytes < 1024 -> "$bytes B"
    bytes < 1024 * 1024 -> "${bytes / 1024} KB"
    else -> {
        val mb = bytes / (1024.0 * 1024)
        val rounded = (mb * 10).roundToInt() / 10.0
        "$rounded MB"
    }
}