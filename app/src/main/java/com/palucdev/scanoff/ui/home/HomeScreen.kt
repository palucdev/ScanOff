package com.palucdev.scanoff.ui.home

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CropFree
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.PostAdd
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.palucdev.scanoff.R
import com.palucdev.scanoff.services.MockDataService
import com.palucdev.scanoff.ui.components.PermissionExplanationDialog
import com.palucdev.scanoff.ui.theme.DarkSearchBar
import com.palucdev.scanoff.ui.theme.ScanCardBlue

@Composable
fun HomeScreen(
    onNavigateToScanner: () -> Unit,
    onNavigateToDocument: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val folders = remember { MockDataService.getFolders() }
    val recentDocuments = remember { MockDataService.getRecentDocuments() }

    val showPermissionDialog = remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            onNavigateToScanner()
        } else {
            Toast.makeText(
                context,
                "Please set the Camera permission in Settings > Apps -> ScanOff",
                Toast.LENGTH_SHORT,
            ).show()
        }
    }

    fun checkAndRequestPermissions() {
        val granted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (granted) {
            onNavigateToScanner()
        } else {
            showPermissionDialog.value = true
        }
    }

    if (showPermissionDialog.value) {
        PermissionExplanationDialog(
            onContinue = {
                permissionLauncher.launch(Manifest.permission.CAMERA)
                showPermissionDialog.value = false
            },
            onDismiss = {
                Toast.makeText(
                    context,
                    "Please set the Camera permission in Settings > Apps -> ScanOff",
                    Toast.LENGTH_SHORT,
                ).show()
                showPermissionDialog.value = false
            },
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 16.dp),
    ) {
        // ── Greeting header ─────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = stringResource(R.string.greeting_evening),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // ── Search bar (stub) ───────────────────────────────────────
        TextField(
            value = "",
            onValueChange = {},
            placeholder = {
                Text(
                    text = stringResource(R.string.search_hint),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(30)),
            singleLine = true,
            readOnly = true,
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = DarkSearchBar,
                focusedContainerColor = DarkSearchBar,
                unfocusedIndicatorColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
            ),
        )

        Spacer(Modifier.height(16.dp))

        // ── Action cards ────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ActionCard(
                title = stringResource(R.string.action_scan_doc),
                icon = Icons.Outlined.CameraAlt,
                containerColor = ScanCardBlue,
                contentColor = Color.White,
                modifier = Modifier.weight(1f),
                onClick = { checkAndRequestPermissions() },
            )
            ActionCard(
                title = stringResource(R.string.action_create_pdf),
                icon = Icons.Outlined.PostAdd,
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                contentColor = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
                onClick = { checkAndRequestPermissions() },
            )
        }

        Spacer(Modifier.height(24.dp))

        // ── Folders section ─────────────────────────────────────────
        SectionHeader(
            title = stringResource(R.string.section_folders),
            onSeeAllClick = {
                Toast.makeText(context, "Folders — coming soon", Toast.LENGTH_SHORT).show()
            },
        )

        Spacer(Modifier.height(8.dp))

        LazyRow(
            contentPadding = PaddingValues(end = 0.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(folders, key = { it.id }) { folder ->
                FolderCard(folder = folder)
            }
        }

        Spacer(Modifier.height(24.dp))

        // ── Recent section ──────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = stringResource(R.string.section_recent),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            TextButton(onClick = {
                Toast.makeText(context, "Recent — coming soon", Toast.LENGTH_SHORT).show()
            }) {
                Text(stringResource(R.string.action_see_all))
            }
        }

        Spacer(Modifier.height(8.dp))

        recentDocuments.forEach { doc ->
            RecentDocumentItem(
                document = doc,
                modifier = Modifier.padding(bottom = 8.dp),
                onClick = { onNavigateToDocument(doc.id.toString()) },
            )
        }
    }
}

// ── Reusable helpers ────────────────────────────────────────────

@Composable
private fun ActionCard(
    title: String,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(80.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(26.dp),
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                color = contentColor,
            )
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    onSeeAllClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        TextButton(onClick = onSeeAllClick) {
            Text(stringResource(R.string.action_see_all))
        }
    }
}

private typealias ImageVector = androidx.compose.ui.graphics.vector.ImageVector
