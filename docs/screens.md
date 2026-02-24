# ScanOff - Application Screens

## Overview

ScanOff is an Android document scanning application that allows users to capture photos of documents and convert them to PDF format. The application uses CameraX for camera operations and supports basic settings configuration.

---

## Screen Architecture

The application follows an Android Fragment-based navigation pattern with a single MainActivity that hosts all screen fragments.

---

## Screens

### 1. MainActivity

**Location:** `app/src/main/java/com/palucdev/scanoff/MainActivity.kt`

**Purpose:** Entry point of the application that hosts all fragment containers.

**Key Features:**
- Acts as the host activity for fragment navigation
- Sets up edge-to-edge window display (using `WindowCompat.setDecorFitsSystemWindows`)
- Uses view binding to manage UI layout

**Navigation:** Container for all other fragments (MenuFragment, ScannerFragment, SettingsFragment)

---

### 2. MenuFragment

**Location:** `app/src/main/java/com/palucdev/scanoff/MenuFragment.kt`

**Purpose:** Main menu screen that serves as the application's primary entry point after launch.

**Key Features:**
- **Floating Action Buttons (FABs):**
  - Primary FAB: Navigates to ScannerFragment to start document scanning
  - Settings FAB: Navigates to SettingsFragment for application configuration
- **Version Display:** Shows current application version number using PackageManager

**UI Elements:**
- `fab`: Main action button for scanning
- `fabSettings`: Settings access button
- `textviewVersion`: Displays app version

**Navigation Routes:**
- `action_MenuFragment_to_ScannerFragment` → Start scanning
- `action_MenuFragment_to_SettingsFragment` → Access settings

---

### 3. ScannerFragment

**Location:** `app/src/main/java/com/palucdev/scanoff/ScannerFragment.kt`

**Purpose:** Core scanning interface that captures photos of documents using the device camera.

**Key Features:**

#### Camera Operations:
- Uses CameraX library for camera access and management
- Supports both front and rear camera switching
- Handles camera lifecycle events and state management
- Dynamic aspect ratio detection (4:3 or 16:9) based on screen dimensions
- Real-time luminosity analysis using custom `LuminosityAnalyzer` class
- Automatic rotation handling for device orientation changes

#### Image Capture:
- Captures photos to MediaStore with timestamped filenames
- Saves images to device Pictures directory with app-specific folder
- Supports API level 16+ (with specific handling for Android P and above)
- Flash animation feedback on successful capture

#### PDF Conversion:
- Converts captured images to PDF format via `createPdf` service
- Currently supports single-page PDF (TODO: multi-page support)
- Stores URI reference of last captured image

#### UI Controls:
- **Back Button:** Returns to menu
- **Camera Capture Button:** Takes photo and stores to MediaStore
- **Camera Switch Button:** Toggles between front and rear cameras
- **PDF Convert Button:** Converts last captured image to PDF (enabled only after capture)

#### Camera State Management:
- Monitors camera state transitions (PENDING_OPEN, OPENING, OPEN, CLOSING, CLOSED)
- Handles camera errors with user feedback via Toast notifications
- Error categories include: stream config, camera in use, max cameras in use, camera disabled, fatal errors, do-not-disturb mode

**Technical Details:**
- Uses `ProcessCameraProvider` for camera initialization
- Implements `ImageAnalysis.Analyzer` interface for frame analysis
- Manages display listener for rotation changes
- Background executor for blocking camera operations
- WindowInfoTracker for responsive layout handling

**Navigation:**
- Back button: `navigateUp()` to return to MenuFragment

---

### 4. SettingsFragment

**Location:** `app/src/main/java/com/palucdev/scanoff/SettingsFragment.kt`

**Purpose:** Application settings and configuration screen.

**Key Features:**
- Material toolbar with navigation support
- Dropdown autocomplete field for settings selection
- Sample options: "Option a", "Option b", "Option c" (placeholder implementation)

**UI Elements:**
- `toolbar`: Navigation toolbar with back button
- `autoCompleteTxt`: Dropdown field for selecting settings options

**Navigation:**
- Toolbar navigation button: `navigateUp()` to return to MenuFragment

---

## Navigation Flow

```
MainActivity (Host)
    ↓
MenuFragment
    ├→ ScannerFragment
    │   └→ MenuFragment (back)
    └→ SettingsFragment
        └→ MenuFragment (back)
```

---

## Key Dependencies

- **AndroidX Fragment:** Fragment management and lifecycle
- **AndroidX Navigation:** Navigation between screens
- **CameraX (androidx.camera):** Camera operations and use cases
- **AndroidX Lifecycle:** Lifecycle-aware components
- **AndroidX Window:** Display metrics and window management
- **Material Components:** UI elements

---

## Image Processing Pipeline

1. **Capture Phase** (ScannerFragment):
   - User taps capture button
   - ImageCapture use case saves photo to MediaStore
   - URI is stored in `savedUri`
   - PDF Convert button is enabled

2. **Conversion Phase** (ScannerFragment):
   - User taps PDF Convert button
   - `createPdf` service (from PdfService.kt) processes the image
   - PDF file is generated from the captured image

---

## Future Enhancements

Based on code comments and TODOs:
- Multi-page PDF support (currently single page only)
- Gallery thumbnail display functionality
- Gallery/image browser screen implementation

---

## Error Handling

- Camera state errors are displayed via Toast notifications
- Luminosity analysis runs on background executor to prevent UI stalls
- Image capture errors are logged with appropriate context
- Safe null checks on camera resources before operations
