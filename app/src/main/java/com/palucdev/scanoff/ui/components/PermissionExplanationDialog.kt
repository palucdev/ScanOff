package com.palucdev.scanoff.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.palucdev.scanoff.R

/**
 * Explains why camera permission is needed before requesting it.
 */
@Composable
fun PermissionExplanationDialog(
    onContinue: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = {
            onDismiss()
        },
        title = { Text(stringResource(R.string.scanning_permissions_title)) },
        text = { Text(stringResource(R.string.scanning_permissions_body)) },
        confirmButton = {
            TextButton(onClick = onContinue) {
                Text(stringResource(R.string.scanning_permissions_continue))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.scanning_permissions_cancel))
            }
        },
    )
}
