package com.fearlauncher

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.fearlauncher.ui.theme.RedBlackTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class SetupActivity : ComponentActivity() {

    private val client = OkHttpClient()
    private val requiredComponents = listOf(
        Component("Java Runtime (JRE) 17", "https://github.com/AdoptOpenJDK/openjdk17-binaries/releases/download/jdk-17.0.2%2B8/OpenJDK17U-jre_aarch64_linux_hotspot_17.0.2_8.tar.gz", "jre17.tar.gz", 80_000_000),
        Component("Minecraft Libraries", "https://launcher.mojang.com/v1/objects/.../libraries.zip", "libraries.zip", 150_000_000),
        Component("LWJGL 3.3.6", "https://build.lwjgl.org/release/3.3.6/lwjgl-3.3.6.zip", "lwjgl.zip", 30_000_000)
    )

    data class Component(val name: String, val url: String, val fileName: String, val expectedSize: Long)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RedBlackTheme {
                SetupScreen(
                    components = requiredComponents,
                    onAllComponentsReady = {
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    }
                )
            }
        }
    }
}
