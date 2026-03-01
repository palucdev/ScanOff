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
 * Accepts an optional [documentId] argument (absolute path to the PDF file).
 * Displays metadata and provides Share / Rename / Export / Delete actions.
 */
class DocumentFragment : Fragment() {

    private var _binding: FragmentDocumentDetailBinding? = null
    private val binding get() = _binding!!

    /** Absolute path to the PDF file passed via the nav argument. */
    private var documentId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        documentId = arguments?.getString("documentId") ?: ""
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
        setupActionButtons()
    }

    private fun setupToolbar() {
        binding.toolbarDetail.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        if (documentId.isNotEmpty()) {
            binding.toolbarDetail.title = documentId
        }

        binding.btnDetailOverflow.setOnClickListener {
            Toast.makeText(requireContext(), "More options — coming soon", Toast.LENGTH_SHORT).show()
        }
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
//        if (documentId.isEmpty()) return
//        try {
//            val file = File(documentId)
//            val uri: Uri = FileProvider.getUriForFile(
//                requireContext(),
//                "com.palucdev.scanoff.fileprovider",
//                file
//            )
//            val intent = Intent(Intent.ACTION_SEND).apply {
//                type = "application/pdf"
//                putExtra(Intent.EXTRA_STREAM, uri)
//                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
//            }
//            startActivity(Intent.createChooser(intent, null))
//        } catch (e: Exception) {
//            Toast.makeText(requireContext(), "Share failed: ${e.message}", Toast.LENGTH_SHORT).show()
//        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
