package com.fearlauncher

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.fearlauncher.ui.theme.RedBlackTheme

class SetupActivity : ComponentActivity() {

    data class Component(val name: String, val url: String, val fileName: String, val expectedSize: Long)

    private val requiredComponents = listOf(
        Component("Java Runtime (JRE) 17", "https://github.com/AdoptOpenJDK/openjdk17-binaries/releases/download/jdk-17.0.2%2B8/OpenJDK17U-jre_aarch64_linux_hotspot_17.0.2_8.tar.gz", "jre17.tar.gz", 80_000_000),
        Component("Minecraft Libraries", "https://launcher.mojang.com/v1/objects/.../libraries.zip", "libraries.zip", 150_000_000),
        Component("LWJGL 3.3.6", "https://build.lwjgl.org/release/3.3.6/lwjgl-3.3.6.zip", "lwjgl.zip", 30_000_000)
    )

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
