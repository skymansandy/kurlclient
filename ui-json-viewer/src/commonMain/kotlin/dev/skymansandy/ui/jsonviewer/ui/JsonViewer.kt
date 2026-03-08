package dev.skymansandy.ui.jsonviewer.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.skymansandy.ui.jsonviewer.constants.cArrow
import dev.skymansandy.ui.jsonviewer.parser.JsonParser
import dev.skymansandy.ui.jsonviewer.util.buildLines

@Composable
fun JsonViewer(
    modifier: Modifier = Modifier,
    json: String,
) {
    val parsed = remember(json) { runCatching { JsonParser(json).parse() }.getOrNull() }

    if (parsed == null) {
        Text(
            text = json,
            style = MaterialTheme.typography.bodySmall,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = modifier,
        )
        return
    }

    var foldedIds by remember(json) { mutableStateOf(emptySet<Int>()) }
    val lines = remember(json, foldedIds) { buildLines(parsed, foldedIds) }

    LazyColumn(modifier = modifier) {
        itemsIndexed(lines, key = { i, _ -> i }) { _, line ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (line.foldId >= 0) Modifier.clickable {
                            foldedIds = if (line.foldId in foldedIds)
                                foldedIds - line.foldId
                            else
                                foldedIds + line.foldId
                        } else Modifier,
                    )
                    .padding(start = (line.indent * 16 + 4).dp, top = 1.dp, bottom = 1.dp),
            ) {
                Text(
                    text = if (line.foldId >= 0) if (line.folded) "▶ " else "▼ " else "  ",
                    fontSize = 9.sp,
                    color = cArrow,
                    fontFamily = FontFamily.Monospace,
                )
                Text(text = line.text)
            }
        }
    }
}
