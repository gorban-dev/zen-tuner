package dev.gorban.zentuner.feature.tuner.presentation.view

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import dev.gorban.zentuner.R
import dev.gorban.zentuner.ui.theme.ZenTunerTheme

@Composable
fun PermissionSettingsDialog(
    onDismiss: () -> Unit,
    onGoToSettings: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.permission_dialog_title)) },
        text = { Text(stringResource(R.string.permission_dialog_message)) },
        confirmButton = {
            TextButton(onClick = onGoToSettings) {
                Text(stringResource(R.string.permission_dialog_settings))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.permission_dialog_cancel))
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun PermissionSettingsDialogPreview() {
    ZenTunerTheme {
        PermissionSettingsDialog(
            onDismiss = {},
            onGoToSettings = {}
        )
    }
}
