package com.palucdev.scanoff.services

import android.content.pm.PackageManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

enum class PermissionResult {
    GRANTED,
    DENIED
}

class PermissionsManager(private val fragment: Fragment) {

    private var onResult: ((PermissionResult) -> Unit)? = null

    private val permissionLauncher = fragment.registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val allGranted = results.values.all { it }

        val result = when {
            allGranted -> PermissionResult.GRANTED
            else -> PermissionResult.DENIED
        }

        onResult?.invoke(result)
    }

    fun requestPermissions(
        permissions: List<String>,
        onResult: (PermissionResult) -> Unit
    ) {
        this.onResult = onResult
        // Launch directly - the launcher was already registered in onCreate
        permissionLauncher.launch(permissions.toTypedArray())
    }

    fun arePermissionsGranted(permissions: List<String>): Boolean {
        return permissions.all { permission ->
            ContextCompat.checkSelfPermission(
                fragment.requireContext(),
                permission
            ) == PackageManager.PERMISSION_GRANTED
        }
    }
}
