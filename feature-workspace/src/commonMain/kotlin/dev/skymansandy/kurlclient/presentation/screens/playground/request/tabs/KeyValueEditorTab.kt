package dev.skymansandy.kurlclient.presentation.screens.playground.request.tabs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.skymansandy.kurl.core.model.KeyValueEntry
import dev.skymansandy.kurlclient.presentation.component.InlineTextField

@Composable
internal fun KeyValueEditorTab(
    entries: List<KeyValueEntry>,
    onUpdate: (Long, String, String, Boolean) -> Unit,
    onAdd: () -> Unit,
    onRemove: (Long) -> Unit,
    keyPlaceholder: String = "Key",
    valuePlaceholder: String = "Value",
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        entries.forEach { entry ->
            KeyValueRow(
                entry = entry,
                keyPlaceholder = keyPlaceholder,
                valuePlaceholder = valuePlaceholder,
                onUpdate = { key, value, enabled -> onUpdate(entry.id, key, value, enabled) },
                onRemove = { onRemove(entry.id) },
            )
        }

        TextButton(
            onClick = onAdd,
            modifier = Modifier.align(Alignment.Start),
        ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
            Text(" Add", style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun KeyValueRow(
    entry: KeyValueEntry,
    keyPlaceholder: String,
    valuePlaceholder: String,
    onUpdate: (String, String, Boolean) -> Unit,
    onRemove: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Checkbox(
            checked = entry.enabled,
            onCheckedChange = { onUpdate(entry.key, entry.value, it) },
            modifier = Modifier.size(20.dp),
        )

        InlineTextField(
            value = entry.key,
            placeholder = keyPlaceholder,
            onValueChange = { onUpdate(it, entry.value, entry.enabled) },
            modifier = Modifier.weight(1f),
        )

        Text(":", color = MaterialTheme.colorScheme.onSurfaceVariant)

        InlineTextField(
            value = entry.value,
            placeholder = valuePlaceholder,
            onValueChange = { onUpdate(entry.key, it, entry.enabled) },
            modifier = Modifier.weight(1f),
        )

        IconButton(onClick = onRemove, modifier = Modifier.size(28.dp)) {
            Icon(
                Icons.Default.Delete,
                contentDescription = "Remove",
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
