package com.palucdev.scanoff.model

/**
 * Represents a document folder shown in the Home screen folders strip.
 *
 * @param id        Unique identifier for the folder.
 * @param name      Display name (e.g. "Work", "Personal").
 * @param fileCount Number of files contained in this folder.
 * @param colorHex  Hex colour string (e.g. "#4FC3F7") used to tint the
 *                  folder icon and its circle background.
 */
data class Folder(
    val id: Long,
    val name: String,
    val fileCount: Int,
    val colorHex: String,
)
