package com.palucdev.scanoff

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.palucdev.scanoff.databinding.FragmentMenuBinding

class MenuFragment : Fragment() {

    private var _binding: FragmentMenuBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMenuBinding.inflate(inflater, container, false)

        binding.fab.setOnClickListener {
            findNavController().navigate(R.id.action_MenuFragment_to_ScannerFragment)
        }

        binding.fabSettings.setOnClickListener {
            Toast.makeText(context, getString(R.string.action_settings), Toast.LENGTH_SHORT).show()
        }

        // Set the version text
        val versionName = requireContext().packageManager.getPackageInfo(requireContext().packageName, 0).versionName
        binding.textviewVersion.text = getString(R.string.version_format, versionName)

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}