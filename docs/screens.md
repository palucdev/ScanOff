# ScanOff - Application Screens

## Overview

ScanOff is an Android document scanning application that allows users to capture photos of documents, organise them in folders, and convert them to PDF format. The application uses CameraX for camera operations, a bottom navigation bar for primary navigation, and supports dark theme, folder management, and settings configuration.

---

## Screen Architecture

The application follows a **single-Activity, Jetpack Compose** pattern. `MainActivity` hosts a single `setContent` block that renders `AppNavHost`, which owns the `NavController`, the bottom `NavigationBar`, and the full `NavHost` routing graph.

Navigation uses **type-safe Navigation Compose** (`navigation-compose 2.9.7`) with `@Serializable` Kotlin objects and data classes as route tokens (defined in `Routes.kt`). There are no XML layout files, no Fragment classes, and no XML navigation graph.

### Navigation routes

| Route | Type | Bottom bar | Description |
|---|---|---|---|
| `HomeRoute` | `object` | Visible | Home dashboard (start destination) |
| `ScannerRoute` | `object` | Hidden | Full-screen camera scanner |
| `FolderListRoute` | `object` | Visible | PDFs / folder list tab |
| `SettingsRoute` | `object` | Visible | Settings tab |
| `DocumentDetailRoute(documentId)` | `data class` | Hidden | Full-screen document detail |

**Route definitions:** `app/src/main/java/com/palucdev/scanoff/navigation/Routes.kt`
**NavHost + bottom bar:** `app/src/main/java/com/palucdev/scanoff/navigation/AppNavHost.kt`

### Bottom navigation bar

The `NavigationBar` is defined in `AppNavHost.kt` (lines 102–164) and is driven by the `topLevelRoutes` list (lines 70–75):

| Label | Icon | Route |
|---|---|---|
| Home | `Icons.Outlined.Home` | `HomeRoute` |
| Scan | `Icons.Outlined.CameraAlt` | `ScannerRoute` |
| PDFs | `Icons.Outlined.Description` | `FolderListRoute` |
| Settings | `Icons.Outlined.Settings` | `SettingsRoute` |

The selected item renders a 56×56 dp pill-shaped indicator with 12 dp corner radius in `NavSelectedIndicator` colour (lines 133–141).

### Bottom bar visibility

Controlled in `AppNavHost.kt` lines 86–88:

```kotlin
val showBottomBar = currentDestination?.let { dest ->
    !dest.hasRoute<ScannerRoute>() && !dest.hasRoute<DocumentDetailRoute>()
} ?: true
```

The bar is hidden on `ScannerRoute` and `DocumentDetailRoute`; it is visible on `HomeRoute`, `FolderListRoute`, and `SettingsRoute`.

### Scan tab behaviour

Tapping the Scan tab navigates directly to `ScannerRoute` as a pushed full-screen destination (no `popUpTo`, no `saveState`), so it never becomes a persistent back-stack root (lines 115–118).

---

## Navigation Flow

```
MainActivity (Host)
│
└── AppNavHost
    ├── NavigationBar (bottom bar, visible on top-level routes)
    │   ├── [Home]     → HomeRoute
    │   ├── [Scan]     → ScannerRoute (pushed, full-screen, bar hidden)
    │   ├── [PDFs]     → FolderListRoute
    │   └── [Settings] → SettingsRoute
    │
    └── NavHost (startDestination = HomeRoute)
        ├── HomeRoute           → HomeScreen
        │     ├── Scan Doc / Create PDF card → ScannerRoute
        │     └── Recent document tap        → DocumentDetailRoute(documentId)
        ├── ScannerRoute        → ScannerScreen (bar hidden)
        ├── FolderListRoute     → FolderListScreen
        │     └── FAB                        → ScannerRoute
        ├── SettingsRoute       → SettingsScreen
        └── DocumentDetailRoute → DocumentDetailScreen (bar hidden)
```

---

## Screens

### 1. MainActivity

**Location:** `app/src/main/java/com/palucdev/scanoff/MainActivity.kt`

**Purpose:** Minimal host activity. Enables edge-to-edge display, creates the `NavController`, and renders `AppNavHost` inside `ScanOffTheme`.

**Key implementation details:**

- Extends `ComponentActivity` (not `AppCompatActivity`)
- `enableEdgeToEdge()` called before `super.onCreate()` (line 14)
- `NavController` created via `rememberNavController()` inside `setContent` (line 19)
- All navigation, bottom bar wiring, and screen routing delegated to `AppNavHost` (line 20)
- No direct `BottomNavigationView` wiring, no Scan tab interception, no visibility logic in this class

---

### 2. AppNavHost

**Location:** `app/src/main/java/com/palucdev/scanoff/navigation/AppNavHost.kt`

**Purpose:** Composable that owns the entire navigation shell: `Scaffold`, animated `NavigationBar`, and `NavHost` with all route destinations.

**Key implementation details:**

- `TopLevelRoute` data class: label resource ID, icon `ImageVector`, route object (lines 64–68)
- `topLevelRoutes` list of four tabs (lines 70–75)
- `currentBackStackEntry` observed as `State` (lines 82–83)
- Bottom bar `AnimatedVisibility` with slide-in/slide-out (lines 90–97)
- `CompositionLocalProvider` disabling ripple on nav bar items (lines 99–101)
- Scan tab special-cased to navigate without `popUpTo`/`restoreState` (lines 115–118)
- Standard tabs use `popUpTo(HomeRoute)`, `saveState = true`, `restoreState = true` (lines 120–127)
- Selected indicator drawn as a 56×56 dp `Box` with `RoundedCornerShape(12.dp)` in `NavSelectedIndicator` colour (lines 133–141)
- `NavHost` with `startDestination = HomeRoute` (lines 168–172)
- `DocumentDetailRoute` argument extracted via `toRoute<DocumentDetailRoute>().documentId` (lines 209–215)

---

### 3. HomeScreen (Home Tab)

**Location:** `app/src/main/java/com/palucdev/scanoff/ui/home/HomeScreen.kt`
**Route:** `HomeRoute` (start destination)
**Mockup:** `docs/screens_mockups/MenuFragment.png`

**Purpose:** Home dashboard. Displays a greeting, quick action cards, a horizontal folder strip, and a recent documents list.

**UI Elements:**

- **Greeting header** (lines 125–142): Time-of-day greeting (`greeting_evening`) and app name row
- **Search bar** (lines 147–175): Read-only stub `OutlinedTextField` with "Search documents..." hint
- **Action cards row** (lines 180–200): Two `ActionCard` composables side-by-side:
  - "Scan Doc" (blue, `ScanCardBlue` background) — calls `checkAndRequestPermissions()`
  - "Create PDF" (surface container) — also calls `checkAndRequestPermissions()`
- **Folders section** (lines 205–221): `SectionHeader` ("Folders" + "See all" stub) + `LazyRow` of `FolderCard`s
- **Recent section** (lines 226–260): `SectionHeader` (clock icon + "Recent" + "See all" stub) + `Column` of `RecentDocumentItem`s

**Permission flow (lines 75–116):**
1. User taps either action card or a scan-related entry
2. `checkAndRequestPermissions()` (lines 89–99) checks `Manifest.permission.CAMERA` via `ActivityResultContracts.RequestPermission`
3. If not granted, sets `showPermissionDialog = true` which renders `PermissionExplanationDialog`
4. "Continue" in dialog launches `permissionLauncher.launch(CAMERA)`
5. On grant → `onNavigateToScanner()` called; on deny → dialog dismissed

**`FolderCard` composable** (`ui/home/FolderCard.kt`, 84 lines):
- `Card` with `width(140.dp)`, `RoundedCornerShape(16.dp)` (lines 44–49)
- 44×44 dp icon box with 20%-alpha coloured background from `colorHex` (lines 55–67)
- Folder name (`bodyMedium`) + file count string from `folder_file_count` format (lines 71–81)

**`RecentDocumentItem` composable** (`ui/home/RecentDocumentItem.kt`, 151 lines):
- Clickable `Card` with `fillMaxWidth`, `RoundedCornerShape(16.dp)`, `surfaceContainerLow` (lines 52–58)
- 56×56 dp thumbnail placeholder with `Description` icon (lines 67–82)
- Title row with optional amber `Star` icon when `isStarred` (lines 87–105)
- Metadata row: page count · date via `doc_meta_format` format string (lines 109–117)
- `SuggestionChip` type badge — PDF: `BadgePdfBg`/`BadgePdfText`; IMAGE: `BadgeImageBg`/`BadgeImageText` (lines 121–140)
- `MoreVert` overflow `IconButton` stub (lines 143–148)

**Data source:** `MockDataService.getFolders()` and `MockDataService.getRecentDocuments()` (line 70–71).

---

### 4. ScannerScreen (Scan)

**Location:** `app/src/main/java/com/palucdev/scanoff/ui/scanner/ScannerScreen.kt`
**Route:** `ScannerRoute` (full-screen, bottom bar hidden)
**Mockup:** `docs/screens_mockups/ScannerFragment.png`

**Purpose:** Full-screen camera interface for capturing document scans and converting them to PDF.

**UI Controls:**

- **Top bar** (`TopAppBar`, lines 381–407):
  - Back `IconButton` → `onNavigateBack()` (navigates up)
  - `SuggestionChip` page counter — format `page_counter_format` ("Page %d"), updated on each capture
- **Camera viewfinder** (lines 352–357): `CameraXViewfinder` composable filling main area via `surfaceRequest`
- **Flash overlay** (lines 360–378): `AnimatedVisibility` (fadeIn/fadeOut) full-screen white `Box`; dismissed after 150 ms via `LaunchedEffect`
- **Bottom controls** (`Column`, lines 410–483):
  - Camera switch `IconButton` — toggles `lensFacing` between back and front; disabled when `canSwitchCamera` is false (lines 432–452)
  - Shutter `IconButton` (72 dp circle, `CameraAlt` icon) — calls `capturePhoto()` (lines 455–465)
  - PDF convert `IconButton` (`PictureAsPdf` icon) — calls `convertToPdf()`; disabled until `savedUri != null` (lines 468–482)
- **PDF loading overlay** (lines 487–496): full-screen `Box` with `CircularProgressIndicator`; shown while `isPdfLoading`

**Camera Operations:**

- Single-thread `cameraExecutor` (line 115)
- `DisplayManager.DisplayListener` tracks rotation changes → updates `imageCapture.targetRotation` and `imageAnalyzer.targetRotation` (lines 134–148)
- `LaunchedEffect(lensFacing)` triggers `ProcessCameraProvider.getInstance()` (lines 248–274)
- Lens fallback: tries back camera first, falls back to front if back unavailable (lines 263–273)
- Use cases bound: `Preview`, `ImageCapture`, `ImageAnalysis` via `provider.bindToLifecycle()` (lines 194–241)
- Camera state error observer shows `Toast` for each `CameraState` error code (lines 214–228)
- `aspectRatio()` helper returns `RATIO_4_3` or `RATIO_16_9` based on window metrics (lines 158–165)

**Image Capture (`capturePhoto()`, lines 277–319):**

- Output directory: `context.getExternalFilesDir("scans")` initialised by `initializeScansDirectory()` (lines 122–132)
- File name: `scan_{timestamp}.jpg`
- Uses `ImageCapture.OutputFileOptions.Builder(file)` (direct file target)
- On success: stores `savedUri`, increments `pageCount`, sets `showFlash = true` for visual feedback (lines 309–318)
- On failure: logs error, shows `Toast`

**PDF Conversion (`convertToPdf()`, lines 322–346):**

- Sets `isPdfLoading = true`, calls `createPdf(savedUri, "test", context)` from `PdfService.kt` on `Dispatchers.Default` via `lifecycleScope.launch`
- On success: clears loading state, shows Toast, calls `openPdf()` to open with system PDF viewer
- On failure: clears loading state, shows error Toast, re-enables button

**Permission check:** Camera permission is checked upstream in `HomeScreen.kt` before navigating here. `ScannerScreen` itself does not request permissions.

---

### 5. FolderListScreen (PDFs Tab)

**Location:** `app/src/main/java/com/palucdev/scanoff/ui/folders/FolderListScreen.kt`
**Route:** `FolderListRoute`
**Mockup:** `docs/screens_mockups/FolderListFragment.png`

**Purpose:** Top-level PDFs tab. Intended to display document folders; currently shows empty state only.

**UI Elements (from code):**

- **`TopAppBar`** (lines 53–66): "My PDFs" title + Sort `IconButton` stub
- **FAB** (lines 68–75): `FloatingActionButton` with `Add` icon → calls `onNavigateToScanner()`
- **Search bar** (lines 84–91): Read-only `OutlinedTextField` stub with "Search your PDFs..." hint
- **Filter chip row** (lines 96–109): Three `FilterChip`s driven by `selectedFilter` state — "All", "Recent", "Starred"
- **Empty state** (lines 114–138): 64 dp `FolderOpen` icon, "No documents yet" title, "Tap + to scan your first document" subtitle

**Current state:** Always shows empty state. The 2-column folder grid from the mockup is not implemented.

**Navigation:**
- FAB → `ScannerRoute` (pushed, via `onNavigateToScanner`)
- (Planned) folder tap → `FolderDetailRoute` (not yet defined)
- (Planned) document tap → `DocumentDetailRoute`

---

### 6. FolderDetailScreen -- PLANNED

**Status:** Not yet implemented. No composable, no route object, and no navigation action exists.
**Mockup:** `docs/screens_mockups/FolderFragment.png`

**Purpose:** Displays the contents of a single folder as a scrollable document list.

**UI Elements (from mockup):**

- **Top bar:** Back arrow, folder name as title (e.g. "Work"), file count subtitle (e.g. "3 files"), "Select" button for multi-select
- **Document list:** Vertical list showing thumbnail, filename, page count, file size, date, overflow menu
- **Bottom navigation:** Visible with "PDFs" tab highlighted

**Expected navigation:**
- Back → `FolderListRoute`
- Document tap → `DocumentDetailRoute(documentId)`
- Select mode → multi-select with batch actions (TBD)

**Implementation requirements:**
- New `FolderDetailRoute(folderId: String)` route object in `Routes.kt`
- New `FolderDetailScreen.kt` composable
- New `composable<FolderDetailRoute>` entry in `AppNavHost.kt`
- Navigation from `FolderListScreen` on folder tap
- Document list adapter (may reuse/extend `RecentDocumentItem` with file size field)
- `Folder` model may need a document list or a repository query

---

### 7. DocumentDetailScreen (Document Detail)

**Location:** `app/src/main/java/com/palucdev/scanoff/ui/document/DocumentDetailScreen.kt`
**Route:** `DocumentDetailRoute(documentId: String = "")`

**Purpose:** Full-screen document viewer and detail screen. Bottom bar is hidden while active.

**Navigation argument:** `documentId` (String, default `""`) — extracted via `toRoute<DocumentDetailRoute>().documentId` in `AppNavHost.kt` (line 211).

**UI Elements:**

- **`TopAppBar`** (lines 47–72): Back navigation → `onNavigateBack()`; title set to `documentId` when non-empty; `MoreVert` stub icon button
- **Content area** (lines 124–144): Centred "Document preview" label + "No preview available yet" subtitle — no PDF rendering implemented
- **`BottomAppBar`** (lines 74–121): Four icon action buttons, all stub Toasts:
  - Share (`IosShare` icon, lines 81–89)
  - Rename (`DriveFileRenameOutline` icon, lines 92–99)
  - Export (`SaveAlt` icon, lines 102–109)
  - Delete (`Delete` icon, lines 112–119)

**Reachable from:**
- `HomeScreen` via recent document tap → `onNavigateToDocument(documentId)`
- (Planned) `FolderDetailScreen` via document tap

---

### 8. SettingsScreen (Settings Tab)

**Location:** `app/src/main/java/com/palucdev/scanoff/ui/settings/SettingsScreen.kt`
**Route:** `SettingsRoute`
**Mockup:** `docs/screens_mockups/SettingsFragment.png`

**Purpose:** Application settings screen. Top-level bottom-nav destination.

**UI Sections:**

**Scan Settings** (lines 96–174):
- Default Scan Mode (`CameraAlt` icon, blue) — value: "Document", chevron stub
- Auto-Detect Edges (`ToggleOn` icon, green) — functional `Switch` (local `isAutoDetectEdges` state, no persistence)
- Image Quality (`Image` icon, orange) — value: "High", chevron stub
- Default Export Format (`FilePresent` icon, red) — value: "PDF", chevron stub
- Auto-delete originals (`FilePresent` icon, purple) — functional `Switch` (local `isAutoDelete` state, no persistence)

**Appearance** (lines 177–206):
- Dark Mode (`DarkMode` icon, primary) — functional `Switch`; calls `AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES / MODE_NIGHT_FOLLOW_SYSTEM)` (lines 188–193)
- Language (`Language` icon, green) — value: "English", chevron stub

**About** (lines 208–243):
- Rate App (`Star` icon, amber) — chevron stub
- Send Feedback (`ChatBubbleOutline` icon, primary) — chevron stub
- Version (`Info` icon, gray) — reads `versionName` from `PackageManager` (lines 75–79); currently shows "0.1.0"

**Known limitation:** `MainActivity` extends `ComponentActivity`, not `AppCompatActivity`. The `AppCompatDelegate.setDefaultNightMode()` call (line 188–193) will not trigger an activity recreation with a new theme. Dark mode toggle is effectively non-functional at runtime.

**Private helper composables:**
- `SectionHeader` (lines 253–263): section label in `titleMedium`
- `SettingsSection` (lines 266–281): `Card` with `RoundedCornerShape(12.dp)` wrapping a `Column`
- `SettingsRowWithIcon` (lines 284–359): row with leading coloured icon, label, optional value text, optional trailing `Switch` or chevron

---

### 9. PermissionExplanationDialog

**Location:** `app/src/main/java/com/palucdev/scanoff/ui/components/PermissionExplanationDialog.kt`

**Purpose:** Compose `AlertDialog` that explains why camera permission is needed before requesting it.

**Behaviour:**
- Rendered conditionally in `HomeScreen` when `showPermissionDialog == true` (lines 101–116)
- Title: `scanning_permissions_title` → "Scanning Permissions"
- Body: `scanning_permissions_body` → "Camera access is required for scanning documents."
- "Continue" (`TextButton`) → calls `onContinue()` → `permissionLauncher.launch(CAMERA)` in `HomeScreen`
- "Cancel" (`TextButton`) → calls `onDismiss()` → sets `showPermissionDialog = false`
- `onDismissRequest` also calls `onDismiss()`

**Called from:** `HomeScreen.kt` (lines 101–116).

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

**Data source:** All data is provided by `MockDataService` (`services/MockDataService.kt`) which returns hardcoded lists. There is no Room database, repository layer, or ViewModel anywhere in the codebase.

---

## Services

### MockDataService

**Location:** `app/src/main/java/com/palucdev/scanoff/services/MockDataService.kt`

Singleton `object` providing hardcoded test data:
- `getFolders()` → 3 `Folder` objects: Work (`#4FC3F7`), Personal (`#EF5350`), Receipts (`#66BB6A`)
- `getRecentDocuments()` → 4 `RecentDocument` objects: Tax Return 2025 (PDF, starred), Invoice #4821 (PDF), Meeting Notes (IMAGE, starred), Lease Agreement (PDF)

### PdfService

**Location:** `app/src/main/java/com/palucdev/scanoff/services/PdfService.kt`

Top-level function `createPdf(fileUri: Uri, filename: String, context: Context): Result<String>`:
1. Reads bitmap from file URI via `BitmapFactory.decodeStream()` (lines 29–43)
2. Reads EXIF orientation and rotates bitmap if needed (lines 47–72)
3. Creates single-page `PdfDocument` sized to bitmap dimensions (lines 76–83)
4. Writes PDF to `getExternalFilesDir("pdfs")/scan_{timestamp}.pdf` (lines 86–96)
5. Returns `Result.success(absolutePath)` or `Result.failure(exception)` (lines 106–109)

Private helper `rotateBitmap()` (lines 113–117).

---

## Theme

**Location:** `app/src/main/java/com/palucdev/scanoff/ui/theme/`

### Color.kt (46 lines)

Defines all semantic color constants used in screens. Notable values:

| Constant | Hex | Usage |
|---|---|---|
| `ScanCardBlue` | `#2979FF` | "Scan Doc" action card background |
| `NavSelectedIndicator` | `#1A3A5C` | Bottom nav pill indicator |
| `StarAmber` | `#FFCA28` | Starred document star icon |
| `DarkSurface` | `#0D1117` | Dark theme surface |
| `BadgePdfBg` / `BadgePdfText` | `#1A2196F3` / `#FF2196F3` | PDF type chip |
| `BadgeImageBg` / `BadgeImageText` | `#1A4CAF50` / `#FF4CAF50` | IMAGE type chip |

### Theme.kt (53 lines)

- `LightColorScheme`: `primary = LightBlue900` (`#01579B`), `surface = White`
- `DarkColorScheme`: `primary = LightBlue600` (`#039BE5`), `surface = DarkSurface`, `surfaceContainerLow = DarkSurfaceContainer`, `onSurfaceVariant = SecondaryBlue`
- `ScanOffTheme`: uses `isSystemInDarkTheme()` by default; sets `isAppearanceLightStatusBars = !darkTheme` via `WindowCompat`

### Type.kt (6 lines)

```kotlin
val ScanOffTypography = Typography()
```
Uses default Material 3 typography — no customisation.

---

## Key Dependencies

- **AndroidX Activity Compose** (`activity-compose 1.12.4`): `setContent`, `ActivityResultContracts`
- **Navigation Compose** (`navigation-compose 2.9.7`): type-safe `NavHost`, `composable<T>`, `rememberNavController`
- **Kotlinx Serialization JSON** (`1.10.0`): `@Serializable` route objects
- **Jetpack Compose BOM** (`2026.02.01`): manages all `androidx.compose.*` versions
- **Material 3** (`material3 1.4.0`): `Scaffold`, `NavigationBar`, `TopAppBar`, `AlertDialog`, chips, FAB, cards
- **Material Icons Extended**: all `Icons.Outlined.*` and `Icons.Default.*` icons
- **AndroidX Lifecycle** (`lifecycle-runtime-compose 2.9.4`, `lifecycle-viewmodel-compose 2.9.4`): `lifecycleScope`, lifecycle-aware state
- **CameraX** (`camera-* 1.5.3`): `ProcessCameraProvider`, `ImageCapture`, `ImageAnalysis`, `CameraXViewfinder` (Compose)
- **AndroidX ExifInterface** (`exifinterface 1.4.2`): EXIF orientation reading
- **AndroidX Window** (`window 1.5.1`): `WindowMetricsCalculator` for aspect ratio
- **AppCompat** (`appcompat 1.7.1`): `AppCompatDelegate` night mode
- **Material Components** (`material 1.12.0`): XML theme support for `themes.xml`
- **Kotlin Coroutines**: `Dispatchers.Default` for PDF conversion in `lifecycleScope.launch`

---

## Image Processing Pipeline

1. **Permission Phase** (`HomeScreen`):
   - User taps "Scan Doc" or "Create PDF" action card
   - `checkAndRequestPermissions()` checks `Manifest.permission.CAMERA`
   - If not granted: `PermissionExplanationDialog` shown; "Continue" launches `permissionLauncher`
   - On grant: `onNavigateToScanner()` navigates to `ScannerRoute`

2. **Capture Phase** (`ScannerScreen`):
   - User taps shutter button
   - `capturePhoto()` saves JPEG to `getExternalFilesDir("scans")/scan_{timestamp}.jpg`
   - `savedUri` updated, `pageCount` incremented, flash animation triggered
   - PDF convert button enabled

3. **Conversion Phase** (`ScannerScreen`):
   - User taps PDF convert button
   - `isPdfLoading = true` shows loading overlay
   - `createPdf()` runs on `Dispatchers.Default`:
     - Decodes bitmap from saved JPEG
     - Reads EXIF orientation, rotates if needed
     - Creates single-page `PdfDocument`
     - Writes to `getExternalFilesDir("pdfs")/scan_{timestamp}.pdf`
   - On success: overlay hidden, PDF opened with system viewer via `openPdf()` (FileProvider intent)
   - On failure: overlay hidden, error Toast shown

4. **Viewing Phase** (`DocumentDetailScreen`):
   - User taps a recent document on Home
   - `DocumentDetailRoute(documentId)` receives the document ID
   - Toolbar shows document ID; bottom action bar provides Share/Rename/Export/Delete (all stubs)
   - No PDF rendering implemented yet

---

## Future Enhancements

Based on code stubs, mockup gaps, and planned destinations:

- **FolderDetailScreen:** Folder detail with document list (mockup exists, no code)
- **Multi-page PDF support:** Currently single-page only
- **Real data layer:** Room database, repository pattern, ViewModels to replace `MockDataService`
- **Document preview:** PDF thumbnail/page rendering in `DocumentDetailScreen`
- **Share functionality:** Share action in `DocumentDetailScreen` is a stub Toast
- **Edge detection:** Auto-detect edges toggle exists in settings UI but is not wired to any feature
- **Gallery integration:** No gallery button in scanner UI currently
- **Search functionality:** Search bars in `HomeScreen` and `FolderListScreen` are read-only stubs
- **Filter / sort:** `FolderListScreen` filter chips and sort button are stubs
- **Settings persistence:** All settings except dark mode toggle are stubs; no `SharedPreferences`/`DataStore` usage; dark mode toggle is also non-functional (see known limitation above)
- **Language selection:** Shown in settings UI, no implementation
- **Rate App / Send Feedback:** Shown in settings UI, no implementation
- **Multi-select mode:** "Select" button shown in `FolderDetailScreen` mockup
- **Folder management:** Create, rename, delete folders

---

## Error Handling

- **Camera state errors:** `Toast` for each `CameraState` error code in `ScannerScreen` (lines 214–228)
- **Image capture errors:** Logged and shown via `Toast` with error message (`ScannerScreen`, inside `capturePhoto()`)
- **PDF conversion errors:** `Result.failure()` caught in `convertToPdf()`, Toast shown, loading overlay dismissed (`ScannerScreen`, lines 322–346)
- **Permission denial:** `permissionLauncher` result sets dialog hidden; no further navigation (`HomeScreen`, lines 75–87)
- **Bitmap cleanup:** Explicit `recycle()` calls after PDF creation to free memory (`PdfService`, lines 101–104)
- **Luminosity analysis:** Runs on `cameraExecutor` background thread to prevent UI stalls
