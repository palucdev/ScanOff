package com.palucdev.scanoff.dialogs

import android.Manifest
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.palucdev.scanoff.services.PermissionResult
import com.palucdev.scanoff.services.PermissionsManager

class PermissionExplanationDialog(
    private val permissionsManager: PermissionsManager,
    private val onPermissionsGranted: () -> Unit,
    private val onPermissionsDenied: () -> Unit
) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext())
            .setTitle("Scanning Permissions")
            .setMessage("Camera access is required for scanning documents.")
            .setPositiveButton("Continue") { _, _ ->
                requestPermissions()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
                onPermissionsDenied.invoke()
            }
            .setCancelable(false)
            .create()
    }

    private fun requestPermissions() {
        permissionsManager.requestPermissions(listOf(Manifest.permission.CAMERA)) { result ->
            when (result) {
                PermissionResult.GRANTED -> {
                    onPermissionsGranted.invoke()
                }

                PermissionResult.DENIED -> {
                    onPermissionsDenied.invoke()
                }
            }
        }
    }

    companion object {
        fun show(
            fragmentManager: FragmentManager,
            permissionsManager: PermissionsManager,
            onPermissionsGranted: () -> Unit,
            onPermissionsDenied: () -> Unit
        ) {
            PermissionExplanationDialog(
                permissionsManager,
                onPermissionsGranted,
                onPermissionsDenied
            ).show(fragmentManager, TAG)
        }

        private const val TAG = "PermissionExplanationDialog"
    }
}
