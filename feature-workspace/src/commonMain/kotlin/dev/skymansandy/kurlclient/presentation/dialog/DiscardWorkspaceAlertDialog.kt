package dev.skymansandy.kurlclient.presentation.dialog

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kurlclient.feature_workspace.generated.resources.Res
import kurlclient.feature_workspace.generated.resources.cancel
import kurlclient.feature_workspace.generated.resources.discard
import kurlclient.feature_workspace.generated.resources.msg_discard_changes
import kurlclient.feature_workspace.generated.resources.title_discard_changes
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun DiscardWorkspaceAlertDialog(
    modifier: Modifier = Modifier,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        title = {
            Text(stringResource(Res.string.title_discard_changes))
        },
        text = {
            Text(stringResource(Res.string.msg_discard_changes))
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(Res.string.discard))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.cancel))
            }
        },
    )
}
