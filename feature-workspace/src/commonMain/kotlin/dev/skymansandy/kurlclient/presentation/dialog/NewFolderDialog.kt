package dev.skymansandy.kurlclient.presentation.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import kurlclient.feature_workspace.generated.resources.Res
import kurlclient.feature_workspace.generated.resources.cancel
import kurlclient.feature_workspace.generated.resources.create
import kurlclient.feature_workspace.generated.resources.hint_folder_name
import kurlclient.feature_workspace.generated.resources.title_new_folder
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun NewFolderDialog(
    fixedParentId: Long?,
    onDismiss: () -> Unit,
    onCreate: (name: String, parentId: Long?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var parentId by remember { mutableStateOf(fixedParentId) }

    Dialog(
        onDismissRequest = onDismiss,
    ) {
        Surface(
            shape = MaterialTheme.shapes.large,
            tonalElevation = 6.dp,
        ) {
            Column(
                modifier = Modifier.padding(24.dp).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = stringResource(Res.string.title_new_folder),
                    style = MaterialTheme.typography.titleLarge,
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(Res.string.hint_folder_name)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(
                        onClick = onDismiss,
                    ) {
                        Text(stringResource(Res.string.cancel))
                    }

                    Spacer(Modifier.width(8.dp))

                    Button(
                        enabled = name.isNotBlank(),
                        onClick = {
                            onCreate(name, parentId)
                        },
                    ) {
                        Text(stringResource(Res.string.create))
                    }
                }
            }
        }
    }
}