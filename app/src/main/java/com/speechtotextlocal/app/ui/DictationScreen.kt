package com.speechtotextlocal.app.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.speechtotextlocal.app.DictationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DictationScreen(viewModel: DictationViewModel = viewModel()) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current

    val micPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) viewModel.start() else viewModel.reportPermissionDenied()
    }

    val onMicClick: () -> Unit = {
        if (viewModel.isListening) {
            viewModel.stop()
        } else {
            val granted = ContextCompat.checkSelfPermission(
                context, Manifest.permission.RECORD_AUDIO,
            ) == PackageManager.PERMISSION_GRANTED
            if (granted) viewModel.start()
            else micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Speech To Text Local") },
                actions = {
                    Text("Clean up", style = MaterialTheme.typography.labelLarge)
                    Switch(
                        checked = viewModel.cleanUpEnabled,
                        onCheckedChange = { viewModel.cleanUpEnabled = it },
                        modifier = Modifier.padding(horizontal = 8.dp),
                    )
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                shape = RoundedCornerShape(20.dp),
                tonalElevation = 2.dp,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                ) {
                    if (viewModel.isOnDeviceAvailable) {
                        AssistChip(
                            onClick = {},
                            enabled = false,
                            label = { Text("On-device") },
                            leadingIcon = {
                                Icon(
                                    Icons.Filled.Lock,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                )
                            },
                        )
                    }
                    Text(
                        text = viewModel.displayedText.ifEmpty { "Your words will appear here…" },
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                Stat(value = viewModel.wordCount.toString(), label = "words")
                Stat(value = if (viewModel.cleanUpEnabled) "On" else "Off", label = "clean-up")
            }

            Text(
                text = if (viewModel.isListening) "Listening…" else "Tap the mic and start speaking",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Surface(
                onClick = onMicClick,
                shape = CircleShape,
                color = if (viewModel.isListening) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.primary
                },
                modifier = Modifier.size(96.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (viewModel.isListening) Icons.Filled.Stop else Icons.Filled.Mic,
                        contentDescription = if (viewModel.isListening) "Stop dictation" else "Start dictation",
                        tint = Color.White,
                        modifier = Modifier.size(40.dp),
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedButton(
                    onClick = { clipboard.setText(AnnotatedString(viewModel.displayedText)) },
                    enabled = viewModel.displayedText.isNotEmpty(),
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text("  Copy")
                }
                OutlinedButton(
                    onClick = { viewModel.clear() },
                    enabled = viewModel.displayedText.isNotEmpty(),
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(Icons.Filled.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text("  Clear")
                }
            }
        }
    }

    viewModel.errorMessage?.let { message ->
        AlertDialog(
            onDismissRequest = { viewModel.errorMessage = null },
            confirmButton = {
                TextButton(onClick = { viewModel.errorMessage = null }) { Text("OK") }
            },
            title = { Text("Notice") },
            text = { Text(message) },
        )
    }
}

@Composable
private fun Stat(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleMedium)
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
