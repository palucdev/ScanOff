package com.palucdev.scanoff

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.palucdev.scanoff.databinding.FragmentPdfsBinding

/**
 * Top-level tab fragment that displays the user's scanned PDF documents.
 */
class FolderListFragment : Fragment() {

    private var _binding: FragmentPdfsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPdfsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupEmptyState()
        setupSortButton()
        setupFab()
        setupFilterChips()
    }

    /** Show empty state until we have real data; hide the recycler view. */
    private fun setupEmptyState() {
        binding.pdfsRecyclerView.visibility = View.GONE
        binding.pdfsEmptyState.visibility = View.VISIBLE
    }

    private fun setupSortButton() {
        binding.btnSort.setOnClickListener {
            Toast.makeText(requireContext(), "Sort — coming soon", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupFab() {
        binding.fabNewPdf.setOnClickListener {
            // Navigate to scanner to create a new PDF
            findNavController().navigate(R.id.ScannerFragment)
        }
    }

    private fun setupFilterChips() {
        binding.pdfsFilterChips.setOnCheckedStateChangeListener { _, _ ->
            // Filter logic will be wired once the adapter + data layer are in place
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun findNavController() =
        androidx.navigation.fragment.NavHostFragment.findNavController(this)
}
