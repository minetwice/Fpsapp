package com.yourlauncher.game

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.yourlauncher.version.VersionManager
import java.io.File

class GameLauncher(private val context: Context) {

    private val versionManager = VersionManager(context)

    fun launchVersion(versionId: String) {
        val jarPath = versionManager.getVersionJarPath(versionId)
        val jarFile = File(jarPath)
        if (!jarFile.exists()) return

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                jarFile
            ), "application/java-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(intent)
    }

    fun launchWithJVM(versionId: String, jvmArgs: List<String> = emptyList()) {
        // Launch using custom JVM implementation (PojavCore/Boardwalk)
        // This requires integrating PojavLauncher core libraries
    }
}
