package com.palucdev.scanoff.services

import com.palucdev.scanoff.model.DocumentType
import com.palucdev.scanoff.model.Folder
import com.palucdev.scanoff.model.RecentDocument

/**
 * Provides hardcoded mock data for the Home screen while the real
 * data layer (Room / repository) is not yet implemented.
 */
object MockDataService {

    fun getFolders(): List<Folder> = listOf(
        Folder(id = 1, name = "Work",     fileCount = 12, colorHex = "#4FC3F7"),
        Folder(id = 2, name = "Personal", fileCount = 8,  colorHex = "#EF5350"),
        Folder(id = 3, name = "Receipts", fileCount = 23, colorHex = "#66BB6A"),
    )

    fun getRecentDocuments(): List<RecentDocument> = listOf(
        RecentDocument(
            id = 1,
            title = "Tax Return 2025",
            pageCount = 4,
            date = "Feb 25, 2026",
            type = DocumentType.PDF,
            isStarred = true,
        ),
        RecentDocument(
            id = 2,
            title = "Invoice #4821",
            pageCount = 1,
            date = "Feb 24, 2026",
            type = DocumentType.PDF,
            isStarred = false,
        ),
        RecentDocument(
            id = 3,
            title = "Meeting Notes",
            pageCount = 2,
            date = "Feb 23, 2026",
            type = DocumentType.IMAGE,
            isStarred = true,
        ),
        RecentDocument(
            id = 4,
            title = "Lease Agreement",
            pageCount = 8,
            date = "Feb 20, 2026",
            type = DocumentType.PDF,
            isStarred = false,
        ),
    )
}
