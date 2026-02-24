package com.palucdev.scanoff

import android.Manifest
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.palucdev.scanoff.databinding.FragmentMenuBinding
import com.palucdev.scanoff.dialogs.PermissionExplanationDialog
import com.palucdev.scanoff.services.PermissionsManager

class MenuFragment : Fragment() {

    private var _binding: FragmentMenuBinding? = null

    private val binding get() = _binding!!

    private lateinit var permissionsManager: PermissionsManager

    override fun onAttach(context: android.content.Context) {
        super.onAttach(context)
        // Initialize PermissionsManager in onAttach, before onCreate
        permissionsManager = PermissionsManager(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMenuBinding.inflate(inflater, container, false)

        binding.fab.setOnClickListener {
            checkAndRequestPermissions()
        }

        binding.fabSettings.setOnClickListener {
            findNavController().navigate(R.id.action_MenuFragment_to_SettingsFragment)
        }

        // Set the version text
        val versionName = requireContext().packageManager.getPackageInfo(
            requireContext().packageName,
            0
        ).versionName
        binding.textviewVersion.text = getString(R.string.version_format, versionName)

        return binding.root
    }

    private fun checkAndRequestPermissions() {
        val requiredPermissions = listOf(
            Manifest.permission.CAMERA,
            Manifest.permission.READ_MEDIA_IMAGES
        )

        if (permissionsManager.arePermissionsGranted(requiredPermissions)) {
            // Permissions already granted
            findNavController().navigate(R.id.action_MenuFragment_to_ScannerFragment)
        } else {
            // Show explanation dialog and request permissions
            PermissionExplanationDialog.show(
                childFragmentManager,
                permissionsManager,
                onPermissionsGranted = {
                    findNavController().navigate(R.id.action_MenuFragment_to_ScannerFragment)
                },
                onPermissionsDenied = {
                    Toast.makeText(
                        requireContext(),
                        "Application cannot work properly without permissions",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
