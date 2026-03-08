package dev.skymansandy.kurlclient.presentation.screens.playground.request.tabs

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kurlclient.feature_workspace.generated.resources.Res
import kurlclient.feature_workspace.generated.resources.msg_auth_config
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun AuthTab() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            stringResource(Res.string.msg_auth_config),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}