package dev.skymansandy.kurlclient.util.compose

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import dev.skymansandy.kurl.core.model.HttpMethod

@Composable
internal fun methodColor(method: HttpMethod) = when (method) {
    HttpMethod.GET -> MaterialTheme.colorScheme.primary
    HttpMethod.POST -> MaterialTheme.colorScheme.secondary
    HttpMethod.PUT -> MaterialTheme.colorScheme.tertiary
    HttpMethod.DELETE -> MaterialTheme.colorScheme.error
    HttpMethod.PATCH -> MaterialTheme.colorScheme.secondary
    HttpMethod.HEAD -> MaterialTheme.colorScheme.primary
    HttpMethod.OPTIONS -> MaterialTheme.colorScheme.tertiary
}
