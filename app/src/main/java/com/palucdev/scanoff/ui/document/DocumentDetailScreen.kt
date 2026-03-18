package com.palucdev.scanoff.ui.document

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DriveFileRenameOutline
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.palucdev.scanoff.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentDetailScreen(
    documentId: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = documentId.ifEmpty { stringResource(R.string.title_document_detail) },
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.previous),
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        Toast.makeText(context, "More options — coming soon", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = stringResource(R.string.more_options_desc),
                        )
                    }
                },
            )
        },
        bottomBar = {
            BottomAppBar {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    // Share
                    IconButton(onClick = {
                        // shareDocument() is commented out in the original — stub
                        Toast.makeText(context, "Share — coming soon", Toast.LENGTH_SHORT).show()
                    }) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.IosShare, contentDescription = stringResource(R.string.action_share))
                            Text(stringResource(R.string.action_share), style = MaterialTheme.typography.labelSmall)
                        }
                    }

                    // Rename
                    IconButton(onClick = {
                        Toast.makeText(context, "Rename — coming soon", Toast.LENGTH_SHORT).show()
                    }) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.DriveFileRenameOutline, contentDescription = stringResource(R.string.action_rename))
                            Text(stringResource(R.string.action_rename), style = MaterialTheme.typography.labelSmall)
                        }
                    }

                    // Export
                    IconButton(onClick = {
                        Toast.makeText(context, "Export — coming soon", Toast.LENGTH_SHORT).show()
                    }) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.SaveAlt, contentDescription = stringResource(R.string.action_export))
                            Text(stringResource(R.string.action_export), style = MaterialTheme.typography.labelSmall)
                        }
                    }

                    // Delete
                    IconButton(onClick = {
                        Toast.makeText(context, "Delete — coming soon", Toast.LENGTH_SHORT).show()
                    }) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.action_delete))
                            Text(stringResource(R.string.action_delete), style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        },
    ) { innerPadding ->
        // Content area — currently empty, matching the original DocumentFragment
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(R.string.document_preview_desc),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "No preview available yet",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                )
            }
        }
    }
}
