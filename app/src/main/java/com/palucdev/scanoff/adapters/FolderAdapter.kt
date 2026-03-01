package com.palucdev.scanoff.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.graphics.ColorUtils
import androidx.recyclerview.widget.RecyclerView
import com.palucdev.scanoff.databinding.ItemFolderBinding
import com.palucdev.scanoff.model.Folder

/**
 * Adapter for the horizontal folders strip on the Home screen.
 *
 * Each folder card displays a coloured icon inside a tinted circle,
 * the folder name, and the file count. The colour is defined per-folder
 * in [Folder.colorHex] and applied at bind time.
 */
class FolderAdapter(
    private val folders: List<Folder>,
) : RecyclerView.Adapter<FolderAdapter.ViewHolder>() {

    inner class ViewHolder(
        val binding: ItemFolderBinding,
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemFolderBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false,
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val folder = folders[position]
        val color = Color.parseColor(folder.colorHex)

        with(holder.binding) {
            itemFolderName.text = folder.name
            itemFolderCount.text = root.context.getString(
                com.palucdev.scanoff.R.string.folder_file_count,
                folder.fileCount,
            )

            // Tint the folder icon with the folder's colour
            itemFolderIcon.setColorFilter(color)

            // Tint the circle background to a 20 % opacity variant
            val bgColor = ColorUtils.setAlphaComponent(color, 51)
            itemFolderCircle.background.mutate().setTint(bgColor)
        }
    }

    override fun getItemCount(): Int = folders.size
}
