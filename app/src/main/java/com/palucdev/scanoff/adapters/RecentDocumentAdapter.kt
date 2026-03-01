package com.palucdev.scanoff.adapters

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.palucdev.scanoff.R
import com.palucdev.scanoff.databinding.ItemDocumentBinding
import com.palucdev.scanoff.model.DocumentType
import com.palucdev.scanoff.model.RecentDocument

/**
 * Adapter for the vertical "Recent" documents list on the Home screen.
 */
class RecentDocumentAdapter(
    private val documents: List<RecentDocument>,
    private val onClickListener: OnClickListener,
) : RecyclerView.Adapter<RecentDocumentAdapter.ViewHolder>() {

    inner class ViewHolder(
        val binding: ItemDocumentBinding,
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDocumentBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false,
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val doc = documents[position]
        val ctx = holder.binding.root.context

        with(holder.binding) {
            itemDocTitle.text = doc.title

            // Meta line: "4 pages · Feb 25, 2026"
            itemDocMeta.text = ctx.getString(
                R.string.doc_meta_format,
                doc.pageCount,
                doc.date,
            )

            // Star visibility
            itemDocStar.visibility = if (doc.isStarred) View.VISIBLE else View.GONE

            // Type badge chip
            itemDocTypeBadge.text = doc.type.name
            when (doc.type) {
                DocumentType.PDF -> {
                    itemDocTypeBadge.setTextColor(
                        ContextCompat.getColor(ctx, R.color.badge_pdf_text),
                    )
                    itemDocTypeBadge.chipBackgroundColor = ColorStateList.valueOf(
                        ContextCompat.getColor(ctx, R.color.badge_pdf_bg),
                    )
                }

                DocumentType.IMAGE -> {
                    itemDocTypeBadge.setTextColor(
                        ContextCompat.getColor(ctx, R.color.badge_image_text),
                    )
                    itemDocTypeBadge.chipBackgroundColor = ColorStateList.valueOf(
                        ContextCompat.getColor(ctx, R.color.badge_image_bg),
                    )
                }
            }

            // Placeholder thumbnail background (no real images in mock)
            itemDocThumbnail.setImageDrawable(null)
        }

        holder.itemView.setOnClickListener {
            onClickListener.onClick(position, doc)
        }
    }

    override fun getItemCount(): Int = documents.size

    // Interface for the click listener
    interface OnClickListener {
        fun onClick(position: Int, model: RecentDocument)
    }
}
