package com.palucdev.scanoff.model

/**
 * Represents a recently accessed document shown in the Home screen
 * "Recent" section.
 */
data class RecentDocument(
    val id: Long,
    val title: String,
    val pageCount: Int,
    val date: String,
    val type: DocumentType,
    val isStarred: Boolean,
)

/** File type for a scanned document. */
enum class DocumentType {
    PDF,
    IMAGE,
}
