package com.palucdev.scanoff

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.palucdev.scanoff.databinding.FragmentDocumentDetailBinding
import java.io.File

/**
 * Full-screen document viewer / detail screen.
 *
 * Accepts an optional [documentPath] argument (absolute path to the PDF file).
 * Displays metadata and provides Share / Rename / Export / Delete actions.
 *
 * Future work:
 *  - Render a PDF page preview using PdfRenderer
 *  - Implement rename dialog
 *  - Implement export-to-downloads
 *  - Implement delete with confirmation dialog
 */
class DocumentFragment : Fragment() {

    private var _binding: FragmentDocumentDetailBinding? = null
    private val binding get() = _binding!!

    /** Absolute path to the PDF file passed via the nav argument. */
    private var documentPath: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        documentPath = arguments?.getString("documentPath") ?: ""
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDocumentDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        populateMetadata()
        setupActionButtons()
    }

    private fun setupToolbar() {
        binding.toolbarDetail.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        if (documentPath.isNotEmpty()) {
            val fileName = File(documentPath).nameWithoutExtension
            binding.toolbarDetail.title = fileName
        }

        binding.btnDetailOverflow.setOnClickListener {
            Toast.makeText(requireContext(), "More options — coming soon", Toast.LENGTH_SHORT).show()
        }
    }

    private fun populateMetadata() {
        if (documentPath.isEmpty()) return

        val file = File(documentPath)
        if (!file.exists()) return

        // File size
        val sizeKb = file.length() / 1024
        val sizeDisplay = if (sizeKb >= 1024) {
            String.format("%.1f MB", sizeKb / 1024f)
        } else {
            "$sizeKb KB"
        }
        binding.detailSizeValue.text = sizeDisplay

        // Last modified date
        val dateMs = file.lastModified()
        val sdf = java.text.SimpleDateFormat("MMM d, yyyy", java.util.Locale.getDefault())
        binding.detailDateValue.text = sdf.format(java.util.Date(dateMs))

        // Type badge
        val extension = file.extension.uppercase()
        binding.detailTypeBadge.text = extension.ifEmpty { "PDF" }

        // Page count is not available without full PdfRenderer; show placeholder
        binding.detailPagesValue.text = "—"
    }

    private fun setupActionButtons() {
        binding.btnShare.setOnClickListener { shareDocument() }
        binding.btnRename.setOnClickListener {
            Toast.makeText(requireContext(), "Rename — coming soon", Toast.LENGTH_SHORT).show()
        }
        binding.btnExport.setOnClickListener {
            Toast.makeText(requireContext(), "Export — coming soon", Toast.LENGTH_SHORT).show()
        }
        binding.btnDelete.setOnClickListener {
            Toast.makeText(requireContext(), "Delete — coming soon", Toast.LENGTH_SHORT).show()
        }
    }

    private fun shareDocument() {
        if (documentPath.isEmpty()) return
        try {
            val file = File(documentPath)
            val uri: Uri = FileProvider.getUriForFile(
                requireContext(),
                "com.palucdev.scanoff.fileprovider",
                file
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(intent, null))
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Share failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
