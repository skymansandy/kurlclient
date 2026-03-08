package dev.skymansandy.kurlclient.presentation.screens.collections.component.tree

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow

@Composable
internal fun HighlightedText(
    text: String,
    query: String,
    style: TextStyle,
    modifier: Modifier = Modifier,
) {
    val highlight = MaterialTheme.colorScheme.primary
    val annotated = remember(text, query) {
        buildAnnotatedString {
            if (query.isBlank()) {
                append(text)
            } else {
                var start = 0
                val lower = text.lowercase()
                val lowerQ = query.lowercase()
                while (start < text.length) {
                    val idx = lower.indexOf(lowerQ, start)
                    if (idx < 0) {
                        append(text.substring(start))
                        break
                    }
                    append(text.substring(start, idx))
                    pushStyle(
                        SpanStyle(
                            color = highlight,
                            fontWeight = FontWeight.Bold,
                        ),
                    )
                    append(text.substring(idx, idx + query.length))
                    pop()
                    start = idx + query.length
                }
            }
        }
    }

    Text(
        modifier = modifier,
        text = annotated,
        style = style,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}
