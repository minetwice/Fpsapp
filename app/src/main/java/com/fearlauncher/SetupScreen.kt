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
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupScreen(onAllComponentsReady: () -> Unit) {
    val context = LocalContext.current
    val viewModel: SetupViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return SetupViewModel(context) as T
            }
        }
    )

    val storageGranted by viewModel.storagePermissionGranted.collectAsState()
    val allDownloaded by viewModel.allComponentsDownloaded.collectAsState()
    val downloadStates by viewModel.downloadStates.collectAsState()
    val overallProgress by viewModel.overallProgress.collectAsState()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        viewModel.setStoragePermissionGranted(perms.values.all { it })
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            viewModel.setStoragePermissionGranted(true)
        } else {
            val needed = listOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            if (needed.all { ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED }) {
                viewModel.setStoragePermissionGranted(true)
            } else {
                permissionLauncher.launch(needed.toTypedArray())
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
                text = if (storageGranted) "Initializing..." else "Storage permission required",
                fontSize = 14.sp,
                color = Color.LightGray
            )

            Spacer(modifier = Modifier.height(32.dp))

            if (storageGranted && !allDownloaded) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xAA000000)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Required Components", color = Color.White, fontSize = 18.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        downloadStates.forEach { (name, state) ->
                            val progress = if (state is DownloadState.Downloading) state.progress else 0f
                            DownloadItemRow(name = name, state = state, progress = progress)
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
