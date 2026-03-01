package com.palucdev.scanoff

import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.palucdev.scanoff.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)

        // Settings is a top-level bottom-nav destination — no back navigation icon.
        // The toolbar has no navigationIcon set in the layout, so no listener needed.

        // ── About: populate version string ──────────────────────────────────
        val versionName = runCatching {
            requireContext().packageManager.getPackageInfo(
                requireContext().packageName, 0
            ).versionName
        }.getOrDefault("–")
        binding.textviewVersion.text = getString(R.string.version_format, versionName)

        // ── Appearance: dark theme switch ────────────────────────────────────
        // Reflect current night mode on initial load
        binding.switchDarkTheme.isChecked =
            AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES

        binding.switchDarkTheme.setOnCheckedChangeListener { _, isChecked ->
            AppCompatDelegate.setDefaultNightMode(
                if (isChecked) AppCompatDelegate.MODE_NIGHT_YES
                else AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            )
        }

        // ── Storage: auto-delete originals switch ────────────────────────────
        // Stub: persisting to SharedPreferences left for implementation phase
        binding.switchAutoDelete.setOnCheckedChangeListener { _, isChecked ->
            Toast.makeText(
                requireContext(),
                if (isChecked) "Auto-delete enabled" else "Auto-delete disabled",
                Toast.LENGTH_SHORT
            ).show()
        }

        // ── General rows (stub click handlers) ──────────────────────────────
        binding.rowOutputFormat.setOnClickListener {
            Toast.makeText(requireContext(), "Output format — coming soon", Toast.LENGTH_SHORT).show()
        }

        binding.rowImageQuality.setOnClickListener {
            Toast.makeText(requireContext(), "Image quality — coming soon", Toast.LENGTH_SHORT).show()
        }

        binding.rowScanFolder.setOnClickListener {
            Toast.makeText(requireContext(), "Scan folder — coming soon", Toast.LENGTH_SHORT).show()
        }

        // ── Storage rows (stub click handlers) ──────────────────────────────
        binding.rowStorageLocation.setOnClickListener {
            Toast.makeText(requireContext(), "Storage location — coming soon", Toast.LENGTH_SHORT).show()
        }

        // ── About rows ───────────────────────────────────────────────────────
        binding.rowLicenses.setOnClickListener {
            Toast.makeText(requireContext(), "Licenses — coming soon", Toast.LENGTH_SHORT).show()
        }

        binding.rowPrivacy.setOnClickListener {
            Toast.makeText(requireContext(), "Privacy policy — coming soon", Toast.LENGTH_SHORT).show()
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
