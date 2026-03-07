package dev.skymansandy.kurlclient.presentation.screens.workspace.request

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun ImportCurlDialog(
    onImport: (String) -> Boolean,   // returns false if parse failed
    onDismiss: () -> Unit
) {
    var text by remember { mutableStateOf("") }
    var parseError by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = MaterialTheme.shapes.large, tonalElevation = 6.dp) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(Modifier.padding(24.dp)),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Import from cURL", style = MaterialTheme.typography.titleLarge)

                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it; parseError = false },
                    label = { Text("Paste cURL command") },
                    placeholder = { Text("curl 'https://...' -H 'Accept: ...'") },
                    isError = parseError,
                    supportingText = if (parseError) {
                        { Text("Could not parse cURL command", color = MaterialTheme.colorScheme.error) }
                    } else null,
                    minLines = 4,
                    maxLines = 12,
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.Monospace
                    )
                )

                Spacer(Modifier.height(0.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (onImport(text.trim())) onDismiss()
                            else parseError = true
                        },
                        enabled = text.isNotBlank()
                    ) {
                        Text("Import")
                    }
                }
            }
        }
    }
}