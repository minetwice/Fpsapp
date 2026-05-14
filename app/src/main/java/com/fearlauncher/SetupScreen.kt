package com.fearlauncher

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

// --- Data classes ---
data class Component(val name: String, val url: String, val fileName: String, val expectedSize: Long)

sealed class DownloadState {
    object Pending : DownloadState()
    data class Downloading(val progress: Float) : DownloadState()
    object Completed : DownloadState()
    object Failed : DownloadState()
}

// --- Download helper ---
suspend fun downloadFile(client: OkHttpClient, url: String, destFile: File, onProgress: (Float) -> Unit): Boolean {
    return withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) return@withContext false
            val body = response.body ?: return@withContext false
            val contentLength = body.contentLength()
            destFile.parentFile?.mkdirs()
            FileOutputStream(destFile).use { output ->
                val input = body.byteStream()
                val buffer = ByteArray(8192)
                var bytesRead: Int
                var totalBytesRead = 0L
                while (input.read(buffer).also { bytesRead = it } != -1) {
                    output.write(buffer, 0, bytesRead)
                    totalBytesRead += bytesRead
                    if (contentLength > 0) {
                        onProgress(totalBytesRead.toFloat() / contentLength)
                    }
                }
                output.flush()
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}

// --- UI Component ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupScreen(
    components: List<Component>,
    onAllComponentsReady: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val client = remember { OkHttpClient() }

    var storagePermissionGranted by remember { mutableStateOf(false) }
    var allDownloaded by remember { mutableStateOf(false) }
    val downloadStates = remember { mutableStateMapOf<String, DownloadState>() }
    var overallProgress by remember { mutableStateOf(0f) }

    // Initialize download states
    LaunchedEffect(components) {
        components.forEach { comp ->
            if (!downloadStates.containsKey(comp.name)) {
                downloadStates[comp.name] = DownloadState.Pending
            }
        }
    }

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        storagePermissionGranted = perms.values.all { it }
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            storagePermissionGranted = true
        } else {
            val needed = listOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            if (needed.all { ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED }) {
                storagePermissionGranted = true
            } else {
                permissionLauncher.launch(needed.toTypedArray())
            }
        }
    }

    // Download process
    LaunchedEffect(storagePermissionGranted) {
        if (storagePermissionGranted && !allDownloaded) {
            val baseDir = context.getExternalFilesDir(null) ?: context.filesDir
            val totalBytes = components.sumOf { it.expectedSize }.toFloat()
            var downloadedBytes = 0f

            for (comp in components) {
                val destFile = File(baseDir, comp.fileName)
                // Check if already downloaded
                if (destFile.exists() && destFile.length() == comp.expectedSize) {
                    downloadStates[comp.name] = DownloadState.Completed
                    downloadedBytes += comp.expectedSize
                    overallProgress = downloadedBytes / totalBytes
                    continue
                }

                downloadStates[comp.name] = DownloadState.Downloading(0f)
                val success = downloadFile(client, comp.url, destFile) { progress ->
                    downloadStates[comp.name] = DownloadState.Downloading(progress)
                    val compDownloaded = destFile.length().coerceAtMost(comp.expectedSize)
                    overallProgress = (downloadedBytes + compDownloaded) / totalBytes
                }
                if (success) {
                    downloadStates[comp.name] = DownloadState.Completed
                    downloadedBytes += comp.expectedSize
                    overallProgress = downloadedBytes / totalBytes
                } else {
                    downloadStates[comp.name] = DownloadState.Failed
                    break
                }
            }

            if (downloadStates.values.all { it is DownloadState.Completed }) {
                allDownloaded = true
            }
        }
    }

    // Animated gradient background
    val infiniteTransition = rememberInfiniteTransition(label = "gradient")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(Color(0xFFB71C1C), Color(0xFF1A1A1A)),
                    radius = 600f + angle / 2
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val bubbleScale by animateFloatAsState(
                targetValue = if (allDownloaded) 1.2f else 1f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy)
            )
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Brush.horizontalGradient(listOf(Color.Red, Color.Black)))
                    .scale(bubbleScale),
                contentAlignment = Alignment.Center
            ) {
                Text("🔥", fontSize = 48.sp)
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Fear Launcher",
                fontSize = 32.sp,
                color = Color.White,
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (storagePermissionGranted) "Initializing..." else "Storage permission required",
                fontSize = 14.sp,
                color = Color.LightGray
            )

            Spacer(modifier = Modifier.height(32.dp))

            if (storagePermissionGranted && !allDownloaded) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xAA000000)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Required Components", color = Color.White, fontSize = 18.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        components.forEach { comp ->
                            val state = downloadStates[comp.name] ?: DownloadState.Pending
                            val progress = if (state is DownloadState.Downloading) state.progress else 0f
                            DownloadItemRow(name = comp.name, state = state, progress = progress)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        LinearProgressIndicator(
                            progress = overallProgress,
                            modifier = Modifier.fillMaxWidth(),
                            color = Color.Red,
                            trackColor = Color.Gray
                        )
                        Text(
                            text = "Overall: ${(overallProgress * 100).toInt()}%",
                            color = Color.White,
                            fontSize = 12.sp,
                            modifier = Modifier.align(Alignment.End)
                        )
                    }
                }
            }

            if (allDownloaded) {
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn() + scaleIn()
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("✅ All components ready!", color = Color.Green, fontSize = 18.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onAllComponentsReady,
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                        ) {
                            Text("Launch Fear Launcher")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DownloadItemRow(name: String, state: DownloadState, progress: Float) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(name, color = Color.White, fontSize = 14.sp)
            when (state) {
                is DownloadState.Downloading -> {
                    LinearProgressIndicator(
                        progress = progress,
                        modifier = Modifier.fillMaxWidth(0.7f),
                        color = Color.Red,
                        trackColor = Color.Gray
                    )
                }
                is DownloadState.Completed -> {
                    Text("✓ Downloaded", color = Color.Green, fontSize = 12.sp)
                }
                is DownloadState.Failed -> {
                    Text("❌ Failed", color = Color.Red, fontSize = 12.sp)
                }
                is DownloadState.Pending -> {
                    Text("Pending", color = Color.Gray, fontSize = 12.sp)
                }
            }
        }
        if (state is DownloadState.Downloading) {
            Text("${(progress * 100).toInt()}%", color = Color.White, fontSize = 12.sp)
        }
    }
}
