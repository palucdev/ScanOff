package com.palucdev.scanoff.ui.settings

import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.palucdev.scanoff.R

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
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
        ) {
            // ── General ─────────────────────────────────────────────
            SectionHeader(stringResource(R.string.settings_section_general))

            SettingsRowChevron(
                title = stringResource(R.string.settings_output_format),
                subtitle = stringResource(R.string.settings_output_format_desc),
                onClick = {
                    Toast.makeText(context, "Output format — coming soon", Toast.LENGTH_SHORT).show()
                },
            )

            SettingsRowChevron(
                title = stringResource(R.string.settings_image_quality),
                subtitle = stringResource(R.string.settings_image_quality_desc),
                onClick = {
                    Toast.makeText(context, "Image quality — coming soon", Toast.LENGTH_SHORT).show()
                },
            )

            SettingsRowChevron(
                title = stringResource(R.string.settings_scan_folder),
                subtitle = stringResource(R.string.settings_scan_folder_desc),
                onClick = {
                    Toast.makeText(context, "Scan folder — coming soon", Toast.LENGTH_SHORT).show()
                },
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // ── Appearance ──────────────────────────────────────────
            SectionHeader(stringResource(R.string.settings_section_appearance))

            SettingsRowSwitch(
                title = stringResource(R.string.settings_dark_theme),
                subtitle = stringResource(R.string.settings_dark_theme_desc),
                checked = isDarkTheme,
                onCheckedChange = { checked ->
                    isDarkTheme = checked
                    AppCompatDelegate.setDefaultNightMode(
                        if (checked) AppCompatDelegate.MODE_NIGHT_YES
                        else AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                    )
                },
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // ── Storage ─────────────────────────────────────────────
            SectionHeader(stringResource(R.string.settings_section_storage))

            SettingsRowChevron(
                title = stringResource(R.string.settings_storage_location),
                subtitle = stringResource(R.string.settings_storage_location_desc),
                onClick = {
                    Toast.makeText(context, "Storage location — coming soon", Toast.LENGTH_SHORT).show()
                },
            )

            SettingsRowSwitch(
                title = stringResource(R.string.settings_auto_delete),
                subtitle = stringResource(R.string.settings_auto_delete_desc),
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

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // ── About ───────────────────────────────────────────────
            SectionHeader(stringResource(R.string.settings_section_about))

            SettingsRowChevron(
                title = stringResource(R.string.settings_version),
                subtitle = stringResource(R.string.version_format, versionName ?: "-"),
                onClick = {},
            )

            SettingsRowChevron(
                title = stringResource(R.string.settings_licenses),
                subtitle = null,
                onClick = {
                    Toast.makeText(context, "Licenses — coming soon", Toast.LENGTH_SHORT).show()
                },
            )

            SettingsRowChevron(
                title = stringResource(R.string.settings_privacy),
                subtitle = null,
                onClick = {
                    Toast.makeText(context, "Privacy policy — coming soon", Toast.LENGTH_SHORT).show()
                },
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}

// ── Reusable setting row components ─────────────────────────────────

@Composable
private fun SectionHeader(title: String, modifier: Modifier = Modifier) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier.padding(top = 16.dp, bottom = 8.dp),
    )
}

@Composable
private fun SettingsRowChevron(
    title: String,
    subtitle: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = stringResource(R.string.settings_row_chevron_desc, title),
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SettingsRowSwitch(
    title: String,
    subtitle: String?,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Spacer(Modifier.width(16.dp))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
