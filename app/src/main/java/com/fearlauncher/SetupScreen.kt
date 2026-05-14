package com.fearlauncher

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat

@Composable
fun SetupScreen(onAllComponentsReady: () -> Unit) {
    val context = LocalContext.current
    var permissionGranted by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        permissionGranted = perms.values.all { it }
        if (permissionGranted) {
            onAllComponentsReady()
        }
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            permissionGranted = true
            onAllComponentsReady()
        } else {
            val needed = listOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            if (needed.all { ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED }) {
                permissionGranted = true
                onAllComponentsReady()
            } else {
                permissionLauncher.launch(needed.toTypedArray())
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A1A1A))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Color.Red),
                contentAlignment = Alignment.Center
            ) {
                Text("🔥", fontSize = 48.sp, color = Color.White)
            }
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = "Fear Launcher",
                fontSize = 32.sp,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(16.dp))
            if (!permissionGranted) {
                Text(
                    text = "Requesting storage permission...",
                    color = Color.LightGray
                )
            } else {
                Text(
                    text = "Ready!",
                    color = Color.Green
                )
            }
        }
    }
}
