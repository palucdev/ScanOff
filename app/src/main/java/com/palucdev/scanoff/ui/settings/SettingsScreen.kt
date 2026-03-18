package com.palucdev.scanoff.ui.settings

import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FilePresent
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ToggleOn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.palucdev.scanoff.R
import com.palucdev.scanoff.ui.theme.DarkSurfaceContainer
import com.palucdev.scanoff.ui.theme.FolderGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    // Read current night mode on first composition
    var isDarkTheme by remember {
        mutableStateOf(
            AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES
        )
    }
    var isAutoDetectEdges by remember { mutableStateOf(true) }
    var isDarkMode by remember { mutableStateOf(isDarkTheme) }
    var isAutoDelete by remember { mutableStateOf(false) }

    // Retrieve app version
    val versionName = remember {
        runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        }.getOrDefault("-")
    }

    Column(modifier = modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(stringResource(R.string.action_settings)) },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background,
            ),
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
        ) {
            // ── Scan Settings ────────────────────────────────────────
            SectionHeader(stringResource(R.string.settings_section_scan))

            SettingsSection {
                SettingsRowWithIcon(
                    title = stringResource(R.string.settings_default_scan_mode),
                    value = stringResource(R.string.settings_default_scan_mode_value),
                    icon = Icons.Default.CameraAlt,
                    iconTint = MaterialTheme.colorScheme.primary,
                    isSwitch = false,
                    onClick = {
                        Toast.makeText(
                            context,
                            "Default Scan Mode — coming soon",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                )

                SettingsRowWithIcon(
                    title = stringResource(R.string.settings_auto_detect_edges),
                    value = "",
                    icon = Icons.Default.ToggleOn,
                    iconTint = FolderGreen,
                    isSwitch = true,
                    checked = isAutoDetectEdges,
                    onCheckedChange = { checked ->
                        isAutoDetectEdges = checked
                        Toast.makeText(
                            context,
                            if (checked) "Auto-detect edges enabled" else "Auto-detect edges disabled",
                            Toast.LENGTH_SHORT,
                        ).show()
                    },
                )

                SettingsRowWithIcon(
                    title = stringResource(R.string.settings_image_quality),
                    value = stringResource(R.string.settings_image_quality_value),
                    icon = Icons.Default.Image,
                    iconTint = Color(0xFFFF9800),
                    isSwitch = false,
                    onClick = {
                        Toast.makeText(context, "Image Quality — coming soon", Toast.LENGTH_SHORT)
                            .show()
                    },
                )

                SettingsRowWithIcon(
                    title = stringResource(R.string.settings_default_export_format),
                    value = stringResource(R.string.settings_default_export_format_value),
                    icon = Icons.Default.FilePresent,
                    iconTint = Color(0xFFEF5350),
                    isSwitch = false,
                    onClick = {
                        Toast.makeText(
                            context,
                            "Default Export Format — coming soon",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                )

                SettingsRowWithIcon(
                    title = stringResource(R.string.settings_auto_delete),
                    value = "",
                    icon = Icons.Default.FilePresent,
                    iconTint = Color(0xFF9C27B0),
                    isSwitch = true,
                    checked = isAutoDelete,
                    onCheckedChange = { checked ->
                        isAutoDelete = checked
                        Toast.makeText(
                            context,
                            if (checked) "Auto-delete enabled" else "Auto-delete disabled",
                            Toast.LENGTH_SHORT,
                        ).show()
                    },
                )
            }

            // ── Appearance ───────────────────────────────────────────
            SectionHeader(stringResource(R.string.settings_section_appearance))

            SettingsSection {
                SettingsRowWithIcon(
                    title = stringResource(R.string.settings_dark_mode),
                    value = "",
                    icon = Icons.Default.DarkMode,
                    iconTint = MaterialTheme.colorScheme.primary,
                    isSwitch = true,
                    checked = isDarkMode,
                    onCheckedChange = { checked ->
                        isDarkMode = checked
                        AppCompatDelegate.setDefaultNightMode(
                            if (checked) AppCompatDelegate.MODE_NIGHT_YES
                            else AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                        )
                    },
                )

                SettingsRowWithIcon(
                    title = stringResource(R.string.settings_language),
                    value = stringResource(R.string.settings_language_value),
                    icon = Icons.Default.Language,
                    iconTint = FolderGreen,
                    isSwitch = false,
                    onClick = {
                        Toast.makeText(context, "Language — coming soon", Toast.LENGTH_SHORT).show()
                    },
                )
            }

            // ── About ────────────────────────────────────────────────
            SectionHeader(stringResource(R.string.settings_section_about))

            SettingsSection {
                SettingsRowWithIcon(
                    title = stringResource(R.string.settings_rate_app),
                    value = "",
                    icon = Icons.Default.Star,
                    iconTint = Color(0xFFFFCA28),
                    isSwitch = false,
                    onClick = {
                        Toast.makeText(context, "Rate App — coming soon", Toast.LENGTH_SHORT).show()
                    },
                )

                SettingsRowWithIcon(
                    title = stringResource(R.string.settings_send_feedback),
                    value = "",
                    icon = Icons.Default.ChatBubbleOutline,
                    iconTint = MaterialTheme.colorScheme.primary,
                    isSwitch = false,
                    onClick = {
                        Toast.makeText(context, "Send Feedback — coming soon", Toast.LENGTH_SHORT)
                            .show()
                    },
                )

                SettingsRowWithIcon(
                    title = stringResource(R.string.settings_version),
                    value = versionName ?: "-",
                    icon = Icons.Default.Info,
                    iconTint = Color(0xFF9E9E9E),
                    isSwitch = false,
                    onClick = {},
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

// ── Reusable setting row components ─────────────────────────────────

@Composable
private fun SectionHeader(title: String, modifier: Modifier = Modifier) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.5.sp,
        modifier = modifier.padding(top = 24.dp, bottom = 12.dp),
    )
}

@Composable
private fun SettingsSection(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                shape = RoundedCornerShape(12.dp),
            )
            .padding(vertical = 8.dp),
    ) {
        content()
    }
}

@Composable
private fun SettingsRowWithIcon(
    title: String,
    value: String,
    icon: ImageVector,
    iconTint: Color,
    isSwitch: Boolean,
    modifier: Modifier = Modifier,
    checked: Boolean = false,
    onCheckedChange: ((Boolean) -> Unit)? = null,
    onClick: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = !isSwitch && onClick != null) { onClick?.invoke() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Icon in rounded container
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(
                    color = iconTint.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(12.dp),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(24.dp),
                tint = iconTint,
            )
        }

        Spacer(Modifier.width(16.dp))

        // Title only (no secondary text)
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )

        Spacer(Modifier.width(12.dp))

        // Right side: Value + Chevron (for chevron rows) or Switch (for toggle rows)
        if (isSwitch) {
            Switch(checked = checked, onCheckedChange = onCheckedChange ?: {})
        } else {
            // Value with chevron
            if (value.isNotEmpty()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End,
                ) {
                    Text(
                        text = value,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp,
                    )
                    Spacer(Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Filled.ChevronRight,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
