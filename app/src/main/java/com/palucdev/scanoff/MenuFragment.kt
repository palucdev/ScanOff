package com.palucdev.scanoff

import android.Manifest
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.palucdev.scanoff.adapters.FolderAdapter
import com.palucdev.scanoff.adapters.RecentDocumentAdapter
import com.palucdev.scanoff.databinding.FragmentMenuBinding
import com.palucdev.scanoff.dialogs.PermissionExplanationDialog
import com.palucdev.scanoff.model.RecentDocument
import com.palucdev.scanoff.services.MockDataService
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

        // Primary "Scan Doc" action card — same permission flow as the old fab
        binding.cardScanDoc.setOnClickListener {
            checkAndRequestPermissions()
        }

        // Quick-scan shortcut button in the header (top-right)
        binding.btnQuickScan.setOnClickListener {
            checkAndRequestPermissions()
        }

        // "Create PDF" card — navigate to scanner with intent to convert immediately
        // (placeholder: navigates to scanner for now)
        binding.cardCreatePdf.setOnClickListener {
            checkAndRequestPermissions()
        }

        // "See all" links — stubs until gallery/folder screens are implemented
        binding.btnFoldersSeeAll.setOnClickListener {
            Toast.makeText(requireContext(), "Folders — coming soon", Toast.LENGTH_SHORT).show()
        }

        binding.btnRecentSeeAll.setOnClickListener {
            Toast.makeText(requireContext(), "Recent — coming soon", Toast.LENGTH_SHORT).show()
        }

        setupFoldersRecycler()
        setupRecentRecycler()

        return binding.root
    }

    // ── Folders horizontal strip ──────────────────────────────────

    private fun setupFoldersRecycler() {
        val folders = MockDataService.getFolders()
        val spacingPx = resources.getDimensionPixelSize(R.dimen.folder_item_spacing)

        binding.foldersRecycler.adapter = FolderAdapter(folders)
        binding.foldersRecycler.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(
                outRect: Rect,
                view: View,
                parent: RecyclerView,
                state: RecyclerView.State,
            ) {
                val position = parent.getChildAdapterPosition(view)
                if (position != state.itemCount - 1) {
                    outRect.right = spacingPx
                }
            }
        })
    }

    // ── Recent documents vertical list ────────────────────────────

    private fun setupRecentRecycler() {
        val documents = MockDataService.getRecentDocuments()
        val spacingPx = resources.getDimensionPixelSize(R.dimen.recent_item_spacing)

        val onRecentDocumentClick = object : RecentDocumentAdapter.OnClickListener {
            override fun onClick(position: Int, model: RecentDocument) {
                val action = MenuFragmentDirections.actionHomeToDocumentDetail();
                action.setDocumentId(model.id.toString())

                findNavController().navigate(action)
            }
        }

        binding.recentRecycler.adapter = RecentDocumentAdapter(documents, onRecentDocumentClick)
        binding.recentRecycler.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(
                outRect: Rect,
                view: View,
                parent: RecyclerView,
                state: RecyclerView.State,
            ) {
                val position = parent.getChildAdapterPosition(view)
                if (position != state.itemCount - 1) {
                    outRect.bottom = spacingPx
                }
            }
        })
    }

    private fun checkAndRequestPermissions() {
        val requiredPermissions = listOf(
            Manifest.permission.CAMERA,
        )

        if (permissionsManager.arePermissionsGranted(requiredPermissions)) {
            // Permissions already granted
            findNavController().navigate(R.id.ScannerFragment)
        } else {
            // Show explanation dialog and request permissions
            PermissionExplanationDialog.show(
                childFragmentManager,
                permissionsManager,
                onPermissionsGranted = {
                    findNavController().navigate(R.id.ScannerFragment)
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
