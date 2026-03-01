# ScanOff - Application Screens

## Overview

ScanOff is an Android document scanning application that allows users to capture photos of documents, organise them in folders, and convert them to PDF format. The application uses CameraX for camera operations, a bottom navigation bar for primary navigation, and supports dark theme, folder management, and settings configuration.

---

## Screen Architecture

The application follows a **single-Activity, multi-Fragment** pattern using the Jetpack Navigation Component. `MainActivity` hosts a `NavHostFragment` and a `BottomNavigationView` with four tabs:

| Tab      | Menu Item ID   | Icon                    | Destination Class    |
|----------|---------------|-------------------------|----------------------|
| Home     | `nav_home`    | `ic_nav_home_24px`      | `MenuFragment`       |
| Scan     | `nav_scan`    | `camera_24px`           | `ScannerFragment`    |
| PDFs     | `nav_pdfs`    | `picture_as_pdf_24px`   | `FolderListFragment` |
| Settings | `nav_settings`| `ic_settings_24px`      | `SettingsFragment`   |

**Bottom navigation menu:** `res/menu/bottom_nav_menu.xml`
**Navigation graph:** `res/navigation/nav_graph.xml` (start destination: `nav_home`)

### Top-level vs. pushed destinations

Top-level destinations (`nav_home`, `nav_pdfs`, `nav_settings`) are persistent tab roots where the bottom navigation bar is visible. The **Scan** tab is a special case: tapping it pushes `ScannerFragment` as a full-screen destination (bottom nav hidden) rather than switching to a persistent tab view. This is achieved by registering `ScannerFragment` twice in the nav graph with separate IDs:

- `nav_scan` -- exists so the bottom nav menu item has a matching destination ID
- `ScannerFragment` -- the actual pushed target used for navigation actions

`DocumentFragment` is also a pushed, full-screen destination (bottom nav hidden).

---

## Navigation Flow

```
MainActivity (Host)
│
├── BottomNavigationView
│   ├── [Home]     → MenuFragment (nav_home) ─────────────────┐
│   ├── [Scan]     → ScannerFragment (pushed, full-screen)    │
│   ├── [PDFs]     → FolderListFragment (nav_pdfs) ──────┐    │
│   └── [Settings] → SettingsFragment (nav_settings)      │    │
│                                                         │    │
│   Pushed destinations (bottom nav hidden):              │    │
│   ├── ScannerFragment  ← Home card / PDFs FAB / Scan tab    │
│   ├── DocumentFragment ← Home recent item tap ──────────────┘
│   └── DocumentFragment ← PDFs document tap ─────────────┘
│
│   Planned:
│   └── FolderFragment   ← FolderListFragment folder tap
```

### Defined navigation actions

| Action ID                          | From              | To                  | Notes                              |
|------------------------------------|-------------------|---------------------|------------------------------------|
| `action_home_to_scanner`           | `nav_home`        | `ScannerFragment`   | Enter/exit animations              |
| `action_home_to_document_detail`   | `nav_home`        | `DocumentFragment`  | Passes `documentId` argument       |
| `action_pdfs_to_document_detail`   | `nav_pdfs`        | `DocumentFragment`  | Passes `documentId` argument       |
| `action_scanner_to_home`           | `ScannerFragment` | `nav_home`          | `popUpTo` nav_home (non-inclusive) |

---

## Screens

### 1. MainActivity

**Location:** `app/src/main/java/com/palucdev/scanoff/MainActivity.kt`
**Layout:** `res/layout/activity_main.xml`

**Purpose:** Single host activity for the entire application. Manages the `NavHostFragment` and `BottomNavigationView`.

**Key implementation details:**

- **Edge-to-edge display:** `WindowCompat.setDecorFitsSystemWindows(window, false)` (line 20)
- **NavController setup:** Obtained from `NavHostFragment` in `nav_host_fragment_content_main` (line 26-28)
- **BottomNav wiring:** `binding.bottomNavView.setupWithNavController(navController)` (line 31)
- **Scan tab interception:** `setOnItemSelectedListener` intercepts `nav_scan` and navigates to the pushed `ScannerFragment` ID instead, so the scanner opens full-screen (line 36-45)
- **Bottom nav visibility:** `OnDestinationChangedListener` hides the bottom nav (`View.GONE`) when the current destination is `ScannerFragment`, `nav_scan`, or `DocumentFragment`; shows it (`View.VISIBLE`) for all other destinations (line 48-57)

**Layout structure:**
```
CoordinatorLayout
  └── LinearLayout (vertical)
        ├── FragmentContainerView (NavHostFragment, weight=1)
        └── BottomNavigationView
```

---

### 2. MenuFragment (Home Tab)

**Location:** `app/src/main/java/com/palucdev/scanoff/MenuFragment.kt`
**Layout:** `res/layout/fragment_menu.xml`
**Nav ID:** `nav_home` (start destination)
**Mockup:** `docs/screens_mockups/MenuFragment.png`

**Purpose:** Home dashboard that serves as the application's primary entry point. Displays a greeting, quick actions, folder summaries, and recent documents.

**UI Elements (from mockup and code):**

- **Greeting header:** Time-of-day greeting ("Good evening") and app title "DocScan Pro"
- **Quick-scan button** (`btnQuickScan`): Icon button in the top-right header area (line 47-49)
- **Search bar:** "Search documents..." text field
- **Action cards:**
  - `cardScanDoc`: Blue "Scan Doc" card with camera icon -- navigates to ScannerFragment (line 42-44)
  - `cardCreatePdf`: "Create PDF" card with document icon -- currently also navigates to ScannerFragment (line 53-55)
- **Folders section:**
  - "Folders" label with `btnFoldersSeeAll` "See all" link (stub, line 58-60)
  - `foldersRecycler`: Horizontal RecyclerView using `FolderAdapter` (line 74-92)
  - Displays color-coded folder cards (e.g. Work - 12 files, Personal - 8 files, Receipts - 23 files)
  - Item layout: `res/layout/item_folder.xml`
  - Item spacing via `RecyclerView.ItemDecoration` using `R.dimen.folder_item_spacing`
- **Recent section:**
  - "Recent" label with clock icon and `btnRecentSeeAll` "See all" link (stub, line 62-64)
  - `recentRecycler`: Vertical RecyclerView using `RecentDocumentAdapter` (line 96-123)
  - Each item shows: thumbnail, document title, star indicator (if starred), page count, date, document type badge (PDF/IMAGE), overflow menu
  - Item layout: `res/layout/item_document.xml`
  - Clicking a recent document navigates to `DocumentFragment` via `action_home_to_document_detail` with `documentId` argument (line 100-106)

**Permission flow:**
All scan-related actions go through `checkAndRequestPermissions()` (line 125-150) which checks `Manifest.permission.CAMERA` via `PermissionsManager`. If not granted, shows `PermissionExplanationDialog` before requesting. On grant, navigates to `ScannerFragment`.

**Data source:** `MockDataService.getFolders()` and `MockDataService.getRecentDocuments()` (hardcoded mock data).

---

### 3. ScannerFragment (Scan)

**Location:** `app/src/main/java/com/palucdev/scanoff/ScannerFragment.kt`
**Layouts:** `res/layout/fragment_scanner.xml` (CameraX PreviewView), `res/layout/camera_ui.xml` (overlay controls)
**Nav IDs:** `nav_scan` (tab entry), `ScannerFragment` (pushed destination)
**Mockup:** `docs/screens_mockups/ScannerFragment.png`

**Purpose:** Full-screen camera interface for capturing document scans. Bottom navigation is hidden while this screen is active.

**UI Controls (from mockup and code):**

- **Top bar:**
  - `backButton`: Back arrow -- `navigateUp()` (line 199-201)
  - `pageCounterChip`: Page counter badge showing "1:1" format (line 187-188), updated on each capture (line 246-247)
  - Flash / settings toggle (top-right in mockup)
- **Camera viewfinder:**
  - `viewFinder`: CameraX `PreviewView` filling the main area
  - Document edge detection frame overlay with blue corner brackets (visible in mockup, alignment guide)
  - "Align document within the frame" hint text below the viewfinder
- **Bottom controls:**
  - Gallery button (bottom-left in mockup) -- thumbnail of last capture (commented out in code, line 191-196, 258-259)
  - `cameraCaptureButton`: Shutter button (center) -- captures photo (line 204-273)
  - Grid button (bottom-right in mockup)
  - `cameraSwitchButton`: Camera switch -- toggles front/rear lens (line 276-291)
  - `pdfConvertButton`: PDF convert -- converts last captured image to PDF (line 293-347), enabled only after a capture
- **Loading overlay:** `pdfLoadingOverlay` shown during PDF conversion (line 299, 311, 325)

**Camera Operations:**

- **CameraX initialization:** `ProcessCameraProvider.getInstance()` (line 352)
- **Lens selection:** Defaults to back camera, falls back to front (line 355-359)
- **Use cases bound:** Preview, ImageCapture, ImageAnalysis (line 455-457)
- **Aspect ratio:** Dynamic detection (4:3 or 16:9) based on window metrics (line 604-610)
- **Rotation handling:** `DisplayManager.DisplayListener` updates target rotation on orientation changes (line 94-104)

**Image Capture:**

- Saves to app-private external storage: `getExternalFilesDir("scans")/scan_{timestamp}.jpg` (line 214-218)
- Uses `ImageCapture.OutputFileOptions` with file target (line 221-223)
- Runs on `cameraExecutor` (single-thread executor) (line 227)
- Flash animation feedback on successful capture (line 266-271)
- Increments `pageCount` and updates page counter chip on success (line 245-247)

**PDF Conversion:**

- `createPdf(savedUri, "test", context)` from `PdfService.kt` (line 304-305)
- Runs on `Dispatchers.Default` via `lifecycleScope.launch` (line 303-304)
- On success: hides loading overlay, shows toast, auto-opens PDF with system viewer via `openPdf()` (line 309-318, 618-651)
- On failure: hides loading overlay, shows error toast, re-enables button (line 322-333)

**Camera State Management:**

- Observes `CameraState` transitions: PENDING_OPEN, OPENING, OPEN, CLOSING, CLOSED (line 471-519)
- Error handling via Toast for: stream config, camera in use, max cameras in use, other recoverable, camera disabled, fatal error, do-not-disturb mode (line 522-589)

**Permission validation:**
Checks `CAMERA` permission in `onViewCreated` (line 125-133) and `onResume` (line 161-171). Navigates back if not granted.

---

### 4. FolderListFragment (PDFs Tab)

**Location:** `app/src/main/java/com/palucdev/scanoff/FolderListFragment.kt`
**Layout:** `res/layout/fragment_pdfs.xml`
**Nav ID:** `nav_pdfs`
**Mockup:** `docs/screens_mockups/FolderListFragment.png`

**Purpose:** Top-level PDFs tab that displays the user's document folders in a grid layout.

**UI Elements (from mockup):**

- **Header:** "My Documents" title with back arrow
- **Folder grid:** 2-column grid of folder cards, each showing:
  - Color-coded folder icon (blue, red, green, orange)
  - Folder name (e.g. "Work", "Personal", "Receipts", "Medical")
  - File count (e.g. "12 files")
  - Chevron indicator
- **Hint text:** "Select a folder to view documents" below the grid
- **Bottom navigation:** Visible with "PDFs" tab highlighted

**UI Elements (from code):**

- `pdfsRecyclerView`: RecyclerView for document list (currently hidden, line 38)
- `pdfsEmptyState`: Empty state view (currently visible, line 39)
- `btnSort`: Sort button (stub, line 43-45)
- `pdfsFilterChips`: Chip group with filter options (All / Recent / Starred) (line 55-58)
- `fabNewPdf`: FloatingActionButton to create a new PDF -- navigates to `ScannerFragment` (line 49-52)
- Search bar for filtering documents

**Current state:** Shows empty state only. The folder grid layout from the mockup is not yet implemented -- the current layout has a search bar, filter chips, sort button, and a vertical recycler intended for a flat document list rather than the 2-column folder grid shown in the mockup.

**Navigation:**
- `action_pdfs_to_document_detail` → `DocumentFragment` (with `documentId` argument)
- FAB → `ScannerFragment` (pushed)

---

### 5. FolderFragment (Folder Detail) -- PLANNED

**Status:** Not yet implemented. No Fragment class, layout, or nav destination exists in the codebase.
**Mockup:** `docs/screens_mockups/FolderFragment.png`

**Purpose:** Displays the contents of a single folder as a scrollable document list.

**UI Elements (from mockup):**

- **Toolbar:**
  - Back arrow (navigates to FolderListFragment)
  - Folder name as title (e.g. "Work")
  - File count subtitle (e.g. "3 files")
  - "Select" action button (top-right) for multi-select mode
- **Document list:** Vertical list of document items, each showing:
  - Thumbnail preview (left)
  - Document filename (e.g. "Tax Return 2025.pdf")
  - Metadata line: page count, file size, date (e.g. "4 pages . 2.4 MB")
  - Date line (e.g. "Feb 25, 2026")
  - Overflow menu (three-dot icon, right)
- **Bottom navigation:** Visible with "PDFs" tab highlighted

**Expected navigation:**
- Back → `FolderListFragment` (`nav_pdfs`)
- Document tap → `DocumentFragment` (with document ID argument)
- Select mode → multi-select with batch actions (TBD)

**Implementation requirements:**
- New `FolderFragment.kt` class
- New `fragment_folder.xml` layout
- New nav destination and action in `nav_graph.xml` (from `nav_pdfs` to `FolderFragment`, passing folder ID)
- Document list adapter (may reuse/extend `RecentDocumentAdapter` with file size field)
- Document model may need a `fileSize` property addition

---

### 6. DocumentFragment (Document Detail)

**Location:** `app/src/main/java/com/palucdev/scanoff/DocumentFragment.kt`
**Layout:** `res/layout/fragment_document_detail.xml`
**Nav ID:** `DocumentFragment`
**Mockup:** None

**Purpose:** Full-screen document viewer and detail screen. Bottom navigation is hidden while active.

**Navigation argument:** `documentId` (String, default: `""`) -- passed via Safe Args from `nav_graph.xml` (line 78-81 in nav_graph).

**UI Elements (from code):**

- **Toolbar:**
  - `toolbarDetail`: Toolbar with navigation back arrow -- `navigateUp()` (line 51-53)
  - Title set to `documentId` value if non-empty (line 55-57)
  - `btnDetailOverflow`: Overflow menu button (stub, line 59-61)
- **Content area:** Scrollable content region (currently empty -- no PDF preview or thumbnail rendering implemented)
- **Bottom action bar:**
  - `btnShare`: Share action (line 65) -- calls `shareDocument()` which is currently commented out (line 77-95)
  - `btnRename`: Rename action (stub, line 66-68)
  - `btnExport`: Export action (stub, line 69-71)
  - `btnDelete`: Delete action (stub, line 72-74)

**Reachable from:**
- `MenuFragment` via `action_home_to_document_detail` (recent document tap)
- `FolderListFragment` via `action_pdfs_to_document_detail`

---

### 7. SettingsFragment (Settings Tab)

**Location:** `app/src/main/java/com/palucdev/scanoff/SettingsFragment.kt`
**Layout:** `res/layout/fragment_settings.xml`
**Nav ID:** `nav_settings`
**Mockup:** `docs/screens_mockups/SettingsFragment.png`

**Purpose:** Application settings and configuration screen. Top-level bottom-nav destination (no back navigation icon in toolbar).

**UI Sections (from mockup):**

- **Scan Settings:**
  - Default Scan Mode (value: "Document", chevron)
  - Auto-Detect Edges (toggle switch, on)
  - Image Quality (value: "High", chevron)
  - Default Export Format (value: "PDF", chevron)
- **Appearance:**
  - Dark Mode (toggle switch, on)
  - Language (value: "English", chevron)
- **About:**
  - Rate App (chevron)
  - Send Feedback (chevron)
  - Version (value: "2.4.1", chevron)

**UI Sections (from code -- differs from mockup):**

- **General:**
  - `rowOutputFormat`: Output format selector (stub, line 57-59)
  - `rowImageQuality`: Image quality selector (stub, line 61-63)
  - `rowScanFolder`: Scan folder selector (stub, line 65-67)
- **Appearance:**
  - `switchDarkTheme`: Dark theme toggle -- functional, uses `AppCompatDelegate.setDefaultNightMode()` (line 36-44)
- **Storage:**
  - `rowStorageLocation`: Storage location selector (stub, line 70-72)
  - `switchAutoDelete`: Auto-delete originals toggle (stub with toast feedback, line 48-54)
- **About:**
  - `textviewVersion`: Displays app version from `PackageManager` (line 27-32)
  - `rowLicenses`: Licenses viewer (stub, line 75-77)
  - `rowPrivacy`: Privacy policy viewer (stub, line 79-81)

**Discrepancies between mockup and code:**
- Mockup has "Scan Settings" section; code has "General" section with different items
- Mockup has "Auto-Detect Edges" toggle; code does not
- Mockup has "Default Scan Mode" and "Default Export Format"; code has "Output format" and "Scan folder"
- Mockup has "Language" and "Rate App" / "Send Feedback"; code has "Storage location", "Auto-delete originals", "Licenses", "Privacy policy"
- The mockup represents the target design; the code layout is a partial/interim implementation

---

### 8. PermissionExplanationDialog

**Location:** `app/src/main/java/com/palucdev/scanoff/dialogs/PermissionExplanationDialog.kt`

**Purpose:** `DialogFragment` that explains why camera permission is needed before requesting it from the user.

**Behaviour:**
- Shown via `PermissionExplanationDialog.show(fragmentManager, permissionsManager, onGranted, onDenied)` (line 48-59)
- Uses `MaterialAlertDialogBuilder` with title "Scanning Permissions" and message "Camera access is required for scanning documents." (line 19-21)
- "Continue" button triggers `PermissionsManager.requestPermissions()` for `CAMERA` (line 22-24, 33-45)
- "Cancel" button dismisses the dialog and invokes `onPermissionsDenied` callback (line 25-28)
- Non-cancellable (line 29)
- On permission result: invokes `onPermissionsGranted` or `onPermissionsDenied` callback based on `PermissionResult` enum (line 35-43)

**Called from:** `MenuFragment.checkAndRequestPermissions()` (line 135-149)

---

## Data Models

**Location:** `app/src/main/java/com/palucdev/scanoff/model/`

### Folder

```kotlin
data class Folder(
    val id: Long,
    val name: String,       // Display name (e.g. "Work", "Personal")
    val fileCount: Int,     // Number of files in the folder
    val colorHex: String,   // Hex colour for icon tinting (e.g. "#4FC3F7")
)
```

### RecentDocument

```kotlin
data class RecentDocument(
    val id: Long,
    val title: String,      // Document display name
    val pageCount: Int,     // Number of pages
    val date: String,       // Formatted date string (e.g. "Feb 25, 2026")
    val type: DocumentType, // PDF or IMAGE
    val isStarred: Boolean, // Whether the document is starred/favourited
)
```

### DocumentType

```kotlin
enum class DocumentType {
    PDF,
    IMAGE,
}
```

**Data source:** All data is currently provided by `MockDataService` (`services/MockDataService.kt`) which returns hardcoded lists. There is no Room database, repository layer, or ViewModel anywhere in the codebase.

---

## Services

### MockDataService

**Location:** `app/src/main/java/com/palucdev/scanoff/services/MockDataService.kt`

Singleton object providing hardcoded test data:
- `getFolders()`: Returns 3 folders (Work, Personal, Receipts)
- `getRecentDocuments()`: Returns 4 documents (Tax Return 2025, Invoice #4821, Meeting Notes, Lease Agreement)

### PdfService

**Location:** `app/src/main/java/com/palucdev/scanoff/services/PdfService.kt`

Top-level function `createPdf(fileUri: Uri, filename: String, context: Context): Result<String>`:
1. Reads bitmap from file URI via `BitmapFactory.decodeStream()` (line 29-43)
2. Reads EXIF orientation and rotates bitmap if needed (line 48-72)
3. Creates single-page `PdfDocument` with bitmap dimensions (line 76-83)
4. Writes PDF to `getExternalFilesDir("pdfs")/scan_{timestamp}.pdf` (line 86-96)
5. Returns `Result.success(absolutePath)` or `Result.failure(exception)` (line 106-109)

### PermissionsManager

**Location:** `app/src/main/java/com/palucdev/scanoff/services/PermissionsManager.kt`

Wraps `ActivityResultContracts` for runtime permission requests. Provides:
- `arePermissionsGranted(permissions: List<String>): Boolean`
- `requestPermissions(permissions: List<String>, callback: (PermissionResult) -> Unit)`
- `PermissionResult` enum: `GRANTED`, `DENIED`

---

## Adapters

### FolderAdapter

**Location:** `app/src/main/java/com/palucdev/scanoff/adapters/FolderAdapter.kt`
**Item layout:** `res/layout/item_folder.xml`

Binds `Folder` objects to horizontal folder cards in the Home screen folders strip. Each card displays the folder name, file count, and a color-tinted folder icon.

### RecentDocumentAdapter

**Location:** `app/src/main/java/com/palucdev/scanoff/adapters/RecentDocumentAdapter.kt`
**Item layout:** `res/layout/item_document.xml`

Binds `RecentDocument` objects to document rows in the Home screen recent section. Each row displays thumbnail, title, star indicator, page count, date, and document type badge. Exposes `OnClickListener` interface for item tap handling.

---

## Key Dependencies

- **AndroidX AppCompat:** Activity and theme support
- **AndroidX Fragment:** Fragment management and lifecycle
- **AndroidX Navigation:** Navigation Component (NavController, NavHostFragment, Safe Args)
- **AndroidX Lifecycle:** Lifecycle-aware coroutine scopes (`lifecycleScope`)
- **CameraX (androidx.camera):** Camera preview, image capture, image analysis
- **AndroidX Window:** `WindowInfoTracker`, `WindowMetricsCalculator` for display metrics
- **AndroidX ExifInterface:** EXIF orientation reading for captured images
- **Material Components:** BottomNavigationView, MaterialAlertDialogBuilder, Chips, FAB, Toolbar, Cards
- **Kotlin Coroutines:** Background PDF conversion (`Dispatchers.Default`)

---

## Image Processing Pipeline

1. **Permission Phase** (MenuFragment):
   - User taps "Scan Doc" card, "Create PDF" card, or quick-scan button
   - `PermissionsManager` checks `CAMERA` permission
   - If not granted, `PermissionExplanationDialog` explains and requests
   - On grant, navigates to `ScannerFragment`

2. **Capture Phase** (ScannerFragment):
   - User aligns document within edge-detection frame
   - Taps shutter button
   - `ImageCapture` use case saves JPEG to `getExternalFilesDir("scans")/scan_{timestamp}.jpg`
   - URI stored in `savedUri`, page counter incremented
   - PDF convert button enabled
   - Flash animation feedback on success

3. **Conversion Phase** (ScannerFragment):
   - User taps PDF convert button
   - Loading overlay shown
   - `createPdf()` runs on `Dispatchers.Default`:
     - Decodes bitmap from saved JPEG
     - Reads EXIF orientation, rotates if needed
     - Creates single-page `PdfDocument`
     - Writes to `getExternalFilesDir("pdfs")/scan_{timestamp}.pdf`
   - On success: loading overlay hidden, PDF auto-opened with system viewer
   - On failure: loading overlay hidden, error toast shown

4. **Viewing Phase** (DocumentFragment):
   - User taps a recent document on Home or a document in the PDFs tab
   - `DocumentFragment` receives `documentId` argument
   - Toolbar shows document ID, bottom action bar provides Share/Rename/Export/Delete (all stubs)

---

## Future Enhancements

Based on code TODOs, mockup gaps, and stub implementations:

- **FolderFragment implementation:** Folder detail screen with document list (mockup exists, no code yet)
- **Multi-page PDF support:** Currently single-page only (`ScannerFragment.kt` line 69 TODO)
- **Real data layer:** Room database, repository pattern, ViewModels to replace `MockDataService`
- **Document preview:** PDF thumbnail/page rendering in `DocumentFragment` content area
- **Share functionality:** `DocumentFragment.shareDocument()` is commented out (line 77-95)
- **Edge detection:** Auto-detect edges feature referenced in mockup settings but not implemented
- **Gallery integration:** Gallery thumbnail in scanner (commented out, line 191-196) and gallery browse
- **Search functionality:** Search bars exist in MenuFragment and FolderListFragment layouts but are not wired
- **Filter / sort:** FolderListFragment filter chips and sort button are stubs
- **Settings persistence:** Most settings are stubs showing "coming soon" toasts; no SharedPreferences/DataStore usage
- **Language selection:** Shown in mockup, not in code
- **Rate App / Send Feedback:** Shown in mockup, not in code
- **Multi-select mode:** "Select" button shown in FolderFragment mockup toolbar
- **Folder management:** Create, rename, delete folders

---

## Error Handling

- **Camera state errors:** Displayed via Toast notifications for each `CameraState` error code (ScannerFragment line 522-589)
- **Image capture errors:** Logged and shown via Toast with error message (ScannerFragment line 228-237)
- **PDF conversion errors:** Caught by `Result.failure()`, shown via Toast, loading overlay dismissed (ScannerFragment line 322-333)
- **Permission denial:** Toast "Application cannot work properly without permissions" (MenuFragment line 142-146)
- **Permission validation:** ScannerFragment checks permissions on both `onViewCreated` and `onResume`, navigates back if revoked (line 125-133, 161-171)
- **Luminosity analysis:** Runs on `cameraExecutor` background thread to prevent UI stalls
- **Bitmap cleanup:** Explicit `recycle()` calls after PDF creation to free memory (PdfService line 101-104)
