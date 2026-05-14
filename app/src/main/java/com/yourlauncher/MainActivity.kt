package com.yourlauncher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.yourlauncher.ui.theme.LauncherTheme
import com.yourlauncher.viewmodel.LauncherViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LauncherTheme {
                LauncherScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LauncherScreen(
    viewModel: LauncherViewModel = viewModel()
) {
    val versionManifest by viewModel.versionManifest.collectAsState()
    val downloadProgress by viewModel.downloadProgress.collectAsState()
    val downloadingVersion by viewModel.downloadingVersion.collectAsState()
    val installedVersions by viewModel.installedVersions.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Ultimate Launcher") },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { coroutineScope.launch { viewModel.loadVersions() } },
                containerColor = MaterialTheme.colorScheme.secondary
            ) {
                Text("⟳", fontSize = MaterialTheme.typography.titleLarge.fontSize)
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (versionManifest != null) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(versionManifest?.versions?.filter { it.type == "release" }?.reversed() ?: emptyList()) { version ->
                        VersionCard(
                            version = version,
                            isInstalled = installedVersions.contains(version.id),
                            isDownloading = downloadingVersion == version.id,
                            downloadProgress = if (downloadingVersion == version.id) downloadProgress else 0f,
                            onDownloadClick = {
                                coroutineScope.launch { viewModel.downloadVersion(version.id) }
                            }
                        )
                    }
                }
            } else {
                Text(
                    "Failed to load versions. Tap FAB to retry.",
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

@Composable
fun VersionCard(
    version: com.yourlauncher.data.Version,
    isInstalled: Boolean,
    isDownloading: Boolean,
    downloadProgress: Float,
    onDownloadClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(enabled = false) { },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = version.id,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (isInstalled) "✓ Installed" else if (isDownloading) "Downloading..." else version.type,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isInstalled) Color.Green else MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (isDownloading) {
                    LinearProgressIndicator(
                        progress = downloadProgress,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Button(
                onClick = onDownloadClick,
                enabled = !isInstalled && !isDownloading
            ) {
                Text(if (isInstalled) "Play" else "Download")
            }
        }
    }
}
