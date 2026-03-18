package com.palucdev.scanoff.navigation

import kotlinx.serialization.Serializable

/** Home dashboard (bottom nav tab). */
@Serializable
object HomeRoute

/** Full-screen camera scanner (pushed destination, bottom nav hidden). */
@Serializable
object ScannerRoute

/** PDFs / folder list (bottom nav tab). */
@Serializable
object FolderListRoute

/** Settings (bottom nav tab). */
@Serializable
object SettingsRoute

/** Full-screen document detail (pushed destination, bottom nav hidden). */
@Serializable
data class DocumentDetailRoute(val documentId: String = "")
