package com.palucdev.scanoff.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

/**
 * Explains why camera permission is needed before requesting it.
 * Replaces the old [DialogFragment]-based PermissionExplanationDialog.
 */
@Composable
fun PermissionExplanationDialog(
    onContinue: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = { /* non-cancellable */ },
        title = { Text("Scanning Permissions") },
        text = { Text("Camera access is required for scanning documents.") },
        confirmButton = {
            TextButton(onClick = onContinue) {
                Text("Continue")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}
