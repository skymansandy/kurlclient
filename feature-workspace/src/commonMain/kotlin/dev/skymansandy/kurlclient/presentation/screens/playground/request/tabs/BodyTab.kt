package dev.skymansandy.kurlclient.presentation.screens.playground.request.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import kurlclient.feature_workspace.generated.resources.Res
import kurlclient.feature_workspace.generated.resources.placeholder_body
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun BodyTab(body: String, onBodyChange: (String) -> Unit) {
    BasicTextField(
        value = body,
        onValueChange = onBodyChange,
        textStyle = MaterialTheme.typography.bodySmall.copy(
            color = MaterialTheme.colorScheme.onSurface,
            fontFamily = FontFamily.Monospace
        ),
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        decorationBox = { inner ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(6.dp))
                    .padding(12.dp)
            ) {
                if (body.isEmpty()) {
                    Text(
                        text = stringResource(Res.string.placeholder_body),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                inner()
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}